package com.hotelbooking.payment_service.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.payment_service.domain.PaymentStatus;
import com.hotelbooking.payment_service.dto.PaymentResponse;
import com.hotelbooking.payment_service.model.Payment;
import com.hotelbooking.payment_service.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

@Service
@Transactional
public class PaymentService {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

   public PaymentResponse createPaymentIntent(Long amount, String orderId, String currency) throws Exception {
    Stripe.apiKey = stripeSecretKey;

    Optional<Payment> existingPayment = paymentRepository.findByOrderId(orderId);
    if (existingPayment.isPresent() && existingPayment.get().getStatus() == PaymentStatus.PAID) {
        throw new RuntimeException("Payment already completed for this order");
    }
    
    if (!Arrays.asList("usd", "vnd", "eur").contains(currency.toLowerCase())) {
        throw new IllegalArgumentException("Currency not supported: " + currency);
    }

    if (amount == null || amount <= 0) {
        throw new IllegalArgumentException("Amount must be positive: " + amount);
    }

    System.out.println("Creating payment intent - Amount: " + amount + ", Currency: " + currency + ", Order: " + orderId);

    PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amount) 
            .setCurrency(currency.toLowerCase())
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build()
            )
            .putMetadata("orderId", orderId)
            .putMetadata("country", "VN") 
            .putMetadata("payment_method_types", "card")
            .build();

    PaymentIntent paymentIntent = PaymentIntent.create(params);

    Payment payment;
    if (existingPayment.isPresent()) {
        payment = existingPayment.get();
        payment.setStripePaymentIntentId(paymentIntent.getId());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(amount); 
        payment.setCurrency(currency); 
    } else {
        payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setStripePaymentIntentId(paymentIntent.getId());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod("STRIPE");
    }
    
    paymentRepository.save(payment);

    PaymentResponse response = new PaymentResponse();
    response.setClientSecret(paymentIntent.getClientSecret());
    response.setPaymentIntentId(paymentIntent.getId());
    response.setAmount(amount);
    response.setCurrency(currency);
    response.setOrderId(orderId);
    response.setStatus(paymentIntent.getStatus());

    System.out.println("Payment intent created successfully: " + paymentIntent.getId());
    return response;
}

    public Map<String, Object> confirmPayment(String paymentIntentId) {
        try {
            Stripe.apiKey = stripeSecretKey;
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            
            Optional<Payment> paymentOpt = paymentRepository.findByStripePaymentIntentId(paymentIntentId);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                if ("succeeded".equals(paymentIntent.getStatus())) {
                    payment.setStatus(PaymentStatus.PAID);
                } else if ("canceled".equals(paymentIntent.getStatus())) {
                    payment.setStatus(PaymentStatus.FAILED);
                }
                paymentRepository.save(payment);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", paymentIntent.getStatus());
            response.put("amount", paymentIntent.getAmount());
            response.put("currency", paymentIntent.getCurrency());
            
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error confirming payment: " + e.getMessage());
        }
    }

    public Map<String, Object> getPaymentStatusByOrderId(String orderId) {
        Optional<Payment> paymentOpt = paymentRepository.findByOrderId(orderId);
        Map<String, Object> response = new HashMap<>();
        
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            response.put("status", payment.getStatus().toString());
            response.put("paymentIntentId", payment.getStripePaymentIntentId());
            response.put("amount", payment.getAmount());
            response.put("currency", payment.getCurrency());
            response.put("createdAt", payment.getCreatedAt());
        } else {
            response.put("status", "NOT_FOUND");
        }
        
        return response;
    }

    public void updatePaymentStatus(String paymentIntentId, PaymentStatus status) {
        Optional<Payment> paymentOpt = paymentRepository.findByStripePaymentIntentId(paymentIntentId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setStatus(status);
            paymentRepository.save(payment);
        }
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }


    public Map<String, Object> getRevenueOverview() {
        
        Long totalRevenue = paymentRepository.sumAmountByStatus(PaymentStatus.PAID);
        
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay();
        Long monthlyRevenue = paymentRepository.sumAmountByStatusAndDateRange(
            PaymentStatus.PAID, startOfMonth, endOfMonth);
        
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().plusDays(1).atStartOfDay();
        Long todayRevenue = paymentRepository.sumAmountByStatusAndDateRange(
            PaymentStatus.PAID, startOfToday, endOfToday);
        
        Long successfulPayments = paymentRepository.countByStatus(PaymentStatus.PAID);
        
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalRevenue", totalRevenue != null ? totalRevenue : 0L);
        overview.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : 0L);
        overview.put("todayRevenue", todayRevenue != null ? todayRevenue : 0L);
        overview.put("successfulPayments", successfulPayments);
        overview.put("currency", "USD"); 
        
        return overview;
    }

    public Map<String, Object> getRevenueByTimePeriod(String period) {
        
        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();
        
        switch (period.toLowerCase()) {
            case "week":
                startDate = endDate.minusWeeks(1);
                break;
            case "month":
                startDate = endDate.minusMonths(1);
                break;
            case "quarter":
                startDate = endDate.minusMonths(3);
                break;
            case "year":
                startDate = endDate.minusYears(1);
                break;
            default:
                startDate = endDate.minusMonths(1); 
        }
        
        List<Object[]> revenueData = paymentRepository.findDailyRevenueByDateRange(
            PaymentStatus.PAID, startDate, endDate);
        
        Map<String, Object> result = new HashMap<>();
        result.put("period", period);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("revenueData", revenueData);
        result.put("totalRevenue", calculateTotalRevenue(revenueData));
        
        return result;
    }

    public Map<String, Object> getPaymentMethodStatistics() {
        
        List<Object[]> methodStats = paymentRepository.findPaymentMethodStatistics(PaymentStatus.PAID);
        
        Map<String, Object> result = new HashMap<>();
        result.put("paymentMethods", methodStats);
        result.put("totalSuccessfulPayments", paymentRepository.countByStatus(PaymentStatus.PAID));
        
        return result;
    }

    public Map<String, Object> getRevenueByStatus() {
        
        List<Object[]> statusRevenue = paymentRepository.findRevenueGroupByStatus();
        
        Map<String, Object> result = new HashMap<>();
        result.put("revenueByStatus", statusRevenue);
        
        return result;
    }

    private Long calculateTotalRevenue(List<Object[]> revenueData) {
        return revenueData.stream()
                .mapToLong(data -> (Long) data[1])
                .sum();
    }
}
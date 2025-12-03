package com.hotelbooking.order_service.service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.hotelbooking.order_service.dto.UpdateStatusOrderRequest;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendPaymentSuccessEmail(String orderId, UpdateStatusOrderRequest request) {
        try {
            log.info("=== Preparing to send payment success email ===");
            log.info("Order ID: {}", orderId);
            log.info("User Email: {}", request.getUserEmail());
            log.info("Room Number: {}", request.getRoomNumber());
            log.info("Check In: {}", request.getCheckIn());
            log.info("Check Out: {}", request.getCheckOut());
            log.info("Total Amount: {}", request.getTotalAmount());
            log.info("Payment Status: {}", request.getStatus());
            
            String subject = "Xác nhận thanh toán thành công - Đơn hàng " + orderId;
            String htmlContent = buildPaymentSuccessEmail(request);
            
            if (request.getUserEmail() != null && !request.getUserEmail().trim().isEmpty()) {
                sendEmailHtml(request.getUserEmail(), subject, htmlContent);
                log.info("Payment success email sent for order {} to {}", orderId, request.getUserEmail());
            } else {
                log.warn("Cannot send email: user email is null or empty for order {}", orderId);
            }
            
        } catch (Exception e) {
            log.error("Failed to send payment success email for order {}: {}", 
                    orderId, e.getMessage(), e);
        }
    }

    private String buildPaymentSuccessEmail(UpdateStatusOrderRequest request) {
        try {
            String userEmail = getSafeString(request.getUserEmail(), "Khách hàng");
            String totalAmount = request.getTotalAmount() != null ? 
                request.getTotalAmount().toString() : "0";
            String roomNumber = getSafeString(request.getRoomNumber(), "Sẽ được cung cấp khi check-in");
            
            String checkInDate = formatDateString(request.getCheckIn());
            String checkOutDate = formatDateString(request.getCheckOut());
            
            totalAmount = formatCurrency(totalAmount);
            
            log.debug("Building email with - Room: {}, CheckIn: {}, CheckOut: {}, Total: {}", 
                roomNumber, checkInDate, checkOutDate, totalAmount);
            
            return buildEmailTemplate(userEmail, roomNumber, checkInDate, checkOutDate, totalAmount);
            
        } catch (Exception e) {
            log.error("Error building email content: {}", e.getMessage(), e);
            return createSimpleEmailTemplate(
                getSafeString(request.getUserEmail(), "Khách hàng"),
                getSafeString(request.getRoomNumber(), "N/A"),
                formatDateString(request.getCheckIn()),
                formatDateString(request.getCheckOut()),
                request.getTotalAmount() != null ? request.getTotalAmount().toString() : "0"
            );
        }
    }

    private String buildEmailTemplate(String userEmail, String roomNumber, 
                                     String checkInDate, String checkOutDate, 
                                     String totalAmount) {
        
        userEmail = escapeHtml(userEmail);
        roomNumber = escapeHtml(roomNumber);
        checkInDate = escapeHtml(checkInDate);
        checkOutDate = escapeHtml(checkOutDate);
        totalAmount = escapeHtml(totalAmount);
        
        StringBuilder html = new StringBuilder();
        
        html.append("""
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Xác nhận thanh toán thành công</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333333;
                        margin: 0;
                        padding: 0;
                        background-color: #f5f5f5;
                    }
                    .email-container {
                        max-width: 600px;
                        margin: 20px auto;
                        background-color: #ffffff;
                        border-radius: 12px;
                        overflow: hidden;
                        box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #4CAF50 0%, #2E7D32 100%);
                        color: white;
                        padding: 40px 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0 0 10px 0;
                        font-size: 32px;
                        font-weight: 700;
                    }
                    .header p {
                        margin: 0;
                        font-size: 16px;
                        opacity: 0.9;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .greeting {
                        font-size: 18px;
                        color: #555555;
                        margin-bottom: 30px;
                    }
                    .order-summary {
                        background: #f8f9fa;
                        border-radius: 10px;
                        padding: 25px;
                        margin: 30px 0;
                        border-left: 5px solid #4CAF50;
                    }
                    .summary-item {
                        display: flex;
                        justify-content: space-between;
                        margin-bottom: 15px;
                        padding-bottom: 15px;
                        border-bottom: 1px solid #e0e0e0;
                    }
                    .summary-item:last-child {
                        border-bottom: none;
                        margin-bottom: 0;
                        padding-bottom: 0;
                    }
                    .summary-label {
                        font-weight: 600;
                        color: #666666;
                        font-size: 16px;
                    }
                    .summary-value {
                        font-weight: 600;
                        color: #333333;
                        font-size: 16px;
                    }
                    .total-amount {
                        text-align: center;
                        font-size: 32px;
                        color: #2E7D32;
                        font-weight: bold;
                        margin: 40px 0;
                        padding: 25px;
                        background: #e8f5e9;
                        border-radius: 10px;
                        border: 2px dashed #4CAF50;
                    }
                    .note {
                        background: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 15px;
                        margin: 25px 0;
                        border-radius: 6px;
                        font-size: 14px;
                        color: #856404;
                    }
                    .footer {
                        background: #f8f9fa;
                        padding: 30px;
                        text-align: center;
                        color: #666666;
                        border-top: 1px solid #eeeeee;
                        font-size: 14px;
                    }
                    .contact-info {
                        margin-top: 15px;
                        font-size: 14px;
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="header">
                        <h1>Thanh toán thành công</h1>
                        <p>Cảm ơn bạn đã đặt phòng với chúng tôi</p>
                    </div>
                    
                    <div class="content">
                        <div class="greeting">
                            <p>Xin chào <strong>""");
        
        html.append(userEmail);
        
      html.append("""
        </strong>,</p>
        <p>Thanh toán của bạn đã được xử lý thành công. Dưới đây là chi tiết đơn hàng:</p>
    </div>
    
    <div class="order-summary">
        <div class="summary-item">
            <span class="summary-label">Số phòng:</span>
            <span class="summary-value">""");
        
        html.append(roomNumber);
        
        html.append("""
        </span>
        </div>
        <div class="summary-item">
            <span class="summary-label">Ngày nhận phòng:</span>
            <span class="summary-value">""");
        
        html.append(checkInDate);
        html.append(" (Sau 14:00)</span>");
        
        html.append("""
                            </div>
                            <div class="summary-item">
                                <span class="summary-label">Ngày trả phòng:</span>
                                <span class="summary-value">""");
        
        html.append(checkOutDate);
        html.append(" (Trước 12:00)</span>");
        
        html.append("""
                            </div>
                        </div>
                        
                        <div class="total-amount">
                            """);
        
        html.append(totalAmount);
        
        html.append("""
                        </div>
                        
                        <div class="note">
                            <p><strong>Lưu ý:</strong></p>
                            <p>• Vui lòng mang theo CMND/CCCD khi nhận phòng</p>
                            <p>• Check-in sau 14:00, check-out trước 12:00</p>
                            <p>• Hủy phòng trước 24h để được hoàn tiền</p>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p><strong>HOTEL BOOKING SYSTEM</strong></p>
                        <div class="contact-info">
                            <p>📞 Hotline hỗ trợ: 1900 1234 (24/7)</p>
                            <p>📧 Email: support@hotelbooking.com</p>
                            <p>🏨 Địa chỉ: 123 Đường ABC, Quận XYZ, TP. Hồ Chí Minh</p>
                        </div>
                        <p style="margin-top: 20px; font-size: 12px; color: #999;">
                            Đây là email tự động, vui lòng không trả lời.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """);
        
        return html.toString();
    }

    private String createSimpleEmailTemplate(String userEmail, String roomNumber, 
                                           String checkInDate, String checkOutDate, 
                                           String totalAmount) {
        
        // Escape HTML
        userEmail = escapeHtml(userEmail);
        roomNumber = escapeHtml(roomNumber);
        checkInDate = escapeHtml(checkInDate);
        checkOutDate = escapeHtml(checkOutDate);
        totalAmount = formatCurrency(totalAmount);
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #4CAF50 0%%, #2E7D32 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="margin: 0;">Thanh toán thành công</h1>
                    <p style="margin: 10px 0 0 0; opacity: 0.9;">Cảm ơn bạn đã đặt phòng với chúng tôi</p>
                </div>
                
                <div style="padding: 30px; background: white; border-radius: 0 0 10px 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Thanh toán của bạn đã được xử lý thành công.</p>
                    
                    <div style="background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #4CAF50;">
                        <p><strong>Số phòng:</strong> %s</p>
                        <p><strong>Ngày nhận phòng:</strong> %s (sau 14:00)</p>
                        <p><strong>Ngày trả phòng:</strong> %s (trước 12:00)</p>
                        <p><strong>Tổng tiền:</strong> %s</p>
                    </div>
                    
                    <p style="text-align: center; color: #666; font-size: 14px;">
                        Hotline: 1900 1234 | Email: support@hotelbooking.com
                    </p>
                </div>
            </body>
            </html>
            """, userEmail, roomNumber, checkInDate, checkOutDate, totalAmount);
    }

    private String formatDateString(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return "Đang cập nhật";
        }
        
        try {
            if (dateString.contains("/")) {
                return dateString;
            }
            
            LocalDate date = LocalDate.parse(dateString);
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            log.warn("Cannot parse date: {}, using original", dateString);
            return dateString;
        }
    }

    private String formatCurrency(String amount) {
        try {
            if (amount == null || amount.trim().isEmpty()) {
                return "0 $";
            }
            
            String cleanAmount = amount.replaceAll("[^\\d.]", "");
            
            if (cleanAmount.isEmpty()) {
                return "0 $";
            }
            
            double value = Double.parseDouble(cleanAmount);
            DecimalFormat formatter = new DecimalFormat("#,##0");
            
            return formatter.format(value) + " $";
        } catch (Exception e) {
            log.warn("Cannot format currency: {}, using original", amount);
            return amount + " $";
        }
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    private String getSafeString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        String str = value.toString().trim();
        return str.isEmpty() ? defaultValue : str;
    }

    public void sendEmailHtml(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("HTML email sent to {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
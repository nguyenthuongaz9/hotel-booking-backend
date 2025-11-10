
USE room_service;

START TRANSACTION;

INSERT INTO room (id, room_number, type, price_per_night, description, location, capacity, amenities, is_available) VALUES
('room-001', '101', 'DELUXE', 2500000.00, 'Phòng Deluxe sang trọng với view thành phố', 'Tầng 1, Khu A', 2, 
 '["WiFi", "TV", "Minibar", "Air Conditioning", "Safe"]', TRUE),

('room-002', '102', 'SUITE', 4500000.00, 'Suite cao cấp với phòng khách riêng', 'Tầng 1, Khu A', 3,
 '["WiFi", "TV", "Minibar", "Air Conditioning", "Safe", "Jacuzzi", "Living Room"]', TRUE),

('room-003', '201', 'DOUBLE', 1800000.00, 'Phòng đôi tiện nghi', 'Tầng 2, Khu B', 2,
 '["WiFi", "TV", "Air Conditioning"]', TRUE),

('room-004', '301', 'FAMILY', 3200000.00, 'Phòng gia đình rộng rãi', 'Tầng 3, Khu C', 4,
 '["WiFi", "TV", "Air Conditioning", "Kitchenette", "Balcony"]', TRUE),

('room-005', '202', 'SINGLE', 1200000.00, 'Phòng đơn tiết kiệm', 'Tầng 2, Khu B', 1,
 '["WiFi", "TV", "Air Conditioning"]', TRUE);

INSERT INTO image (id, name, image, room_id) VALUES
(UUID(), 'Phòng 101 View 1', 'https://example.com/images/room-001-1.jpg', 'room-001'),
(UUID(), 'Phòng 101 View 2', 'https://example.com/images/room-001-2.jpg', 'room-001'),
(UUID(), 'Phòng 102 Living Room', 'https://example.com/images/room-002-1.jpg', 'room-002'),
(UUID(), 'Phòng 102 Bedroom', 'https://example.com/images/room-002-2.jpg', 'room-002'),
(UUID(), 'Phòng 201', 'https://example.com/images/room-003-1.jpg', 'room-003'),
(UUID(), 'Phòng 301 Family', 'https://example.com/images/room-004-1.jpg', 'room-004'),
(UUID(), 'Phòng 202 Single', 'https://example.com/images/room-005-1.jpg', 'room-005');

INSERT INTO review (user_id, room_id, rating, comment) VALUES
('651b12345678901234567892', 'room-001', 5, 'Phòng rất đẹp và sạch sẽ, view thành phố tuyệt vời!'),
('651b12345678901234567893', 'room-001', 4, 'Nhân viên thân thiện, phòng tiện nghi. Giá hơi cao.'),
('651b12345678901234567892', 'room-002', 5, 'Suite sang trọng, đầy đủ tiện nghi. Rất đáng giá!'),
('651b12345678901234567895', 'room-003', 3, 'Phòng ổn, đúng với mức giá. Vị trí hơi xa trung tâm.'),
('651b12345678901234567896', 'room-004', 4, 'Phòng gia đình rộng rãi, trẻ con rất thích.');

COMMIT;

USE order_service;

START TRANSACTION;

INSERT INTO orders (id, user_id, room_id, check_in, check_out, total_price, status, payment_status) VALUES
('order-001', '651b12345678901234567892', 'room-001', '2024-02-01', '2024-02-03', 5000000.00, 'COMPLETED', 'PAID'),
('order-002', '651b12345678901234567893', 'room-002', '2024-02-05', '2024-02-08', 13500000.00, 'CONFIRMED', 'PAID'),
('order-003', '651b12345678901234567892', 'room-003', '2024-02-10', '2024-02-12', 3600000.00, 'PENDING', 'PENDING'),
('order-004', '651b12345678901234567895', 'room-004', '2024-02-15', '2024-02-17', 6400000.00, 'CONFIRMED', 'PAID'),
('order-005', '651b12345678901234567896', 'room-001', '2024-02-20', '2024-02-22', 5000000.00, 'CANCELLED', 'FAILED');

COMMIT;

USE payment_service;

START TRANSACTION;

INSERT INTO payments (order_id, amount, currency, stripe_payment_intent_id, status, payment_method) VALUES
('order-001', 5000000, 'VND', 'pi_3Og2d2Lkd4r4g6Se1t2vU5J7', 'SUCCEEDED', 'card'),
('order-002', 13500000, 'VND', 'pi_3Og2d3Lkd4r4g6Se1ABC1234', 'SUCCEEDED', 'card'),
('order-003', 3600000, 'VND', NULL, 'PENDING', NULL),
('order-004', 6400000, 'VND', 'pi_3Og2d4Lkd4r4g6Se1XYZ5678', 'SUCCEEDED', 'bank_transfer'),
('order-005', 5000000, 'VND', 'pi_3Og2d5Lkd4r4g6Se1FAIL999', 'FAILED', 'card');

COMMIT;

SELECT '✅ Đã chèn xong dữ liệu mẫu' as 'STATUS';
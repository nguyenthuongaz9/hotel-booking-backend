-- File: init-mysql/01-init-databases.sql
-- Khởi tạo databases và seed data cho Hotel Booking System (MySQL cho các service)

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET COLLATION_CONNECTION = 'utf8mb4_unicode_ci';

-- Tạo databases
CREATE DATABASE IF NOT EXISTS hotel_booking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS order_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS payment_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS room_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Sử dụng database room_service
USE room_service;

-- Đảm bảo database sử dụng utf8mb4
ALTER DATABASE room_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Tạo bảng rooms
CREATE TABLE IF NOT EXISTS rooms (
    id VARCHAR(36) PRIMARY KEY,
    room_number VARCHAR(50) NOT NULL UNIQUE,
    type ENUM('SINGLE', 'DOUBLE', 'SUITE', 'FAMILY') NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    capacity INT NOT NULL,
    amenities JSON,
    is_available BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Tạo bảng images
CREATE TABLE IF NOT EXISTS images (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255),
    image VARCHAR(500) NOT NULL,
    room_id VARCHAR(36) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Tạo bảng reviews
CREATE TABLE IF NOT EXISTS reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    room_id VARCHAR(36) NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Seed data cho rooms (sử dụng tiếng Việt chuẩn)
INSERT IGNORE INTO rooms (id, room_number, type, price_per_night, description, location, capacity, amenities, is_available) VALUES
('room-001', '101', 'SINGLE', 89.99, 'Phòng Standard tiện nghi với đầy đủ tiện nghi cơ bản, phù hợp cho khách du lịch cá nhân.', 'Tầng 1', 2, '["WIFI", "AIR_CONDITIONING", "TV"]', TRUE),
('room-002', '102', 'SINGLE', 89.99, 'Phòng Standard view thành phố, không gian ấm cúng và thoải mái.', 'Tầng 1', 2, '["WIFI", "AIR_CONDITIONING", "TV", "SAFE"]', TRUE),
('room-003', '201', 'DOUBLE', 129.99, 'Phòng Deluxe rộng rãi với view tuyệt đẹp, trang bị nội thất cao cấp.', 'Tầng 2', 3, '["WIFI", "AIR_CONDITIONING", "TV", "MINIBAR", "SAFE", "BALCONY"]', TRUE),
('room-004', '202', 'DOUBLE', 139.99, 'Phòng Deluxe hướng biển, không gian sang trọng và riêng tư.', 'Tầng 2', 3, '["WIFI", "AIR_CONDITIONING", "TV", "MINIBAR", "SAFE", "BALCONY", "BATHTUB"]', TRUE),
('room-005', '301', 'SUITE', 199.99, 'Suite cao cấp với phòng khách riêng, view toàn cảnh thành phố.', 'Tầng 3', 4, '["WIFI", "AIR_CONDITIONING", "TV", "MINIBAR", "SAFE", "BALCONY", "BATHTUB", "KITCHENETTE"]', TRUE),
('room-006', '302', 'FAMILY', 159.99, 'Phòng gia đình rộng rãi, phù hợp cho gia đình 4 người với đầy đủ tiện nghi.', 'Tầng 3', 4, '["WIFI", "AIR_CONDITIONING", "TV", "MINIBAR", "SAFE", "BALCONY", "EXTRA_BED"]', TRUE),
('room-007', '401', 'SINGLE', 229.99, 'Executive suite sang trọng với không gian làm việc và nghỉ ngơi riêng biệt.', 'Tầng 4', 2, '["WIFI", "AIR_CONDITIONING", "TV", "MINIBAR", "SAFE", "BALCONY", "BATHTUB", "WORKSPACE", "PREMIUM_AMENITIES"]', TRUE),
('room-008', '402', 'SUITE', 209.99, 'Honeymoon suite lãng mạn với không gian riêng tư và view tuyệt đẹp.', 'Tầng 4', 2, '["WIFI", "AIR_CONDITIONING", "TV", "MINIBAR", "SAFE", "BALCONY", "BATHTUB", "ROMANTIC_DECOR"]', TRUE);

-- Seed data cho images
INSERT IGNORE INTO images (id, name, image, room_id) VALUES
('img-001', 'Room 101 Main', 'image1.avif', 'room-001'),
('img-002', 'Room 101 Bathroom', 'image2.avif', 'room-001'),
('img-003', 'Room 102 Main', 'image3.avif', 'room-002'),
('img-004', 'Room 102 View', 'image4.avif', 'room-002'),
('img-005', 'Room 201 Main', 'image5.avif', 'room-003'),
('img-006', 'Room 201 Living', 'image6.avif', 'room-003'),
('img-007', 'Room 202 Main', 'image7.avif', 'room-004'),
('img-008', 'Room 202 Balcony', 'image8.avif', 'room-004'),
('img-009', 'Room 301 Suite', 'image9.avif', 'room-005'),
('img-010', 'Room 301 Living', 'image10.jpg', 'room-005'),
('img-011', 'Room 302 Family', 'image11.jpg', 'room-006'),
('img-012', 'Room 302 Kids', 'image12.jpg', 'room-006'),
('img-013', 'Room 401 Executive', 'image13.jpg', 'room-007'),
('img-014', 'Room 401 Workspace', 'image14.jpg', 'room-007'),
('img-015', 'Room 402 Honeymoon', 'image15.jpg', 'room-008'),
('img-016', 'Room 402 Romantic', 'image16.jpg', 'room-008');

-- Seed data cho reviews (sử dụng tiếng Việt chuẩn)
INSERT IGNORE INTO reviews (user_id, room_id, rating, comment, created_at) VALUES
('user-001', 'room-001', 5, 'Phòng rất sạch sẽ và thoải mái. Nhân viên phục vụ rất chuyên nghiệp!', '2024-01-15 10:30:00'),
('user-002', 'room-001', 4, 'View đẹp, giá cả hợp lý. Sẽ quay lại lần sau.', '2024-01-20 14:20:00'),
('user-003', 'room-003', 5, 'Phòng Deluxe tuyệt vời! Không gian rộng rãi và view rất đẹp.', '2024-02-01 09:15:00'),
('user-004', 'room-004', 4, 'Phòng tốt, đầy đủ tiện nghi. Chỉ hơi ồn một chút.', '2024-02-10 16:45:00'),
('user-005', 'room-005', 5, 'Suite sang trọng, xứng đáng với giá tiền. Rất hài lòng!', '2024-02-15 11:20:00'),
('user-006', 'room-006', 5, 'Phòng gia đình hoàn hảo cho 4 người. Con cái rất thích!', '2024-02-20 13:10:00'),
('user-007', 'room-007', 4, 'Executive suite đẹp, không gian làm việc rất tiện nghi.', '2024-03-01 08:30:00'),
('user-008', 'room-008', 5, 'Honeymoon suite thật lãng mạn! Kỷ niệm đáng nhớ.', '2024-03-05 17:00:00'),
('user-009', 'room-002', 4, 'Phòng sạch sẽ, giá cả phải chăng. Đáng để trải nghiệm.', '2024-03-10 12:25:00'),
('user-010', 'room-003', 5, 'Lần thứ 2 quay lại và vẫn rất hài lòng. Dịch vụ tuyệt vời!', '2024-03-15 15:40:00');

-- Sử dụng database order_service
USE order_service;

-- Đảm bảo database sử dụng utf8mb4
ALTER DATABASE order_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Tạo bảng orders
CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    room_id VARCHAR(36) NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    payment_status ENUM('PENDING', 'PAID', 'FAILED', 'REFUNDED', 'UNPAID') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Seed data cho orders
INSERT IGNORE INTO orders (id, user_id, room_id, check_in, check_out, total_price, status, payment_status) VALUES
('order-001', 'user-001', 'room-001', '2024-04-01', '2024-04-03', 179.98, 'COMPLETED', 'PAID'),
('order-002', 'user-002', 'room-003', '2024-04-05', '2024-04-07', 259.98, 'COMPLETED', 'PAID'),
('order-003', 'user-003', 'room-005', '2024-04-10', '2024-04-12', 399.98, 'CONFIRMED', 'PAID'),
('order-004', 'user-004', 'room-002', '2024-04-15', '2024-04-17', 179.98, 'PENDING', 'PENDING'),
('order-005', 'user-005', 'room-007', '2024-04-20', '2024-04-22', 459.98, 'CONFIRMED', 'PAID'),
('order-006', 'user-006', 'room-006', '2024-04-25', '2024-04-28', 479.97, 'PENDING', 'PENDING'),
('order-007', 'user-007', 'room-004', '2024-05-01', '2024-05-03', 279.98, 'CANCELLED', 'REFUNDED'),
('order-008', 'user-008', 'room-008', '2024-05-05', '2024-05-07', 419.98, 'CONFIRMED', 'PAID');

-- Sử dụng database payment_service
USE payment_service;

-- Đảm bảo database sử dụng utf8mb4
ALTER DATABASE payment_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Tạo bảng payments
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL UNIQUE,
    amount BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    stripe_payment_intent_id VARCHAR(255) UNIQUE,
    status ENUM('PENDING', 'SUCCEEDED', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Seed data cho payments
INSERT IGNORE INTO payments (order_id, amount, currency, stripe_payment_intent_id, status, payment_method) VALUES
('order-001', 17998, 'USD', 'pi_3OEeJtLkd4n8xJZV1s9m7K2c', 'SUCCEEDED', 'card'),
('order-002', 25998, 'USD', 'pi_3OEeKtLkd4n8xJZV1G5m8L3d', 'SUCCEEDED', 'card'),
('order-003', 39998, 'USD', 'pi_3OEeLtLkd4n8xJZV2H6n9M4e', 'SUCCEEDED', 'card'),
('order-005', 45998, 'USD', 'pi_3OEeMtLkd4n8xJZV3I7o0N5f', 'SUCCEEDED', 'card'),
('order-007', 27998, 'USD', 'pi_3OEeNtLkd4n8xJZV4J8p1O6g', 'REFUNDED', 'card'),
('order-008', 41998, 'USD', 'pi_3OEeOtLkd4n8xJZV5K9q2P7h', 'SUCCEEDED', 'card');

-- Tạo indexes để tối ưu performance
USE room_service;
CREATE INDEX idx_rooms_type ON rooms(type);
CREATE INDEX idx_rooms_price ON rooms(price_per_night);
CREATE INDEX idx_rooms_available ON rooms(is_available);
CREATE INDEX idx_images_room ON images(room_id);
CREATE INDEX idx_reviews_room ON reviews(room_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);

USE order_service;
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_room ON orders(room_id);
CREATE INDEX idx_orders_dates ON orders(check_in, check_out);
CREATE INDEX idx_orders_status ON orders(status);

USE payment_service;
CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);

SET FOREIGN_KEY_CHECKS = 1;

SELECT '✅ Đã tạo thành công databases và seed data cho MySQL services';
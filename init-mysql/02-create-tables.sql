-- File: init-mysql/02-create-tables.sql
-- Tạo tables cho các services

-- Database cho hotel service (room_service)
USE room_service;

SET FOREIGN_KEY_CHECKS = 0;

-- Bảng rooms (Hotel Service)
CREATE TABLE IF NOT EXISTS room (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    room_number VARCHAR(50) NOT NULL,
    type ENUM('SINGLE', 'DOUBLE', 'SUITE', 'DELUXE', 'FAMILY') NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    capacity INT NOT NULL,
    amenities JSON,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_room_number (room_number),
    INDEX idx_type (type),
    INDEX idx_price (price_per_night),
    INDEX idx_availability (is_available),
    INDEX idx_created_at (created_at)
);

-- Bảng images (Hotel Service)
CREATE TABLE IF NOT EXISTS image (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(255) NOT NULL,
    image TEXT NOT NULL, -- Lưu URL hoặc base64
    room_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE,
    INDEX idx_room_id (room_id),
    INDEX idx_created_at (created_at)
);

-- Bảng reviews (Hotel Service)
CREATE TABLE IF NOT EXISTS review (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id VARCHAR(36) NOT NULL,
    room_id VARCHAR(36) NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_room_id (room_id),
    INDEX idx_rating (rating),
    INDEX idx_created_at (created_at)
);

-- Database cho order service
USE order_service;

CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id VARCHAR(36) NOT NULL,
    room_id VARCHAR(36) NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    -- Sửa ENUM values để khớp với Java enum
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') DEFAULT 'PENDING',
    payment_status ENUM('UNPAID', 'PENDING', 'PAID', 'FAILED', 'REFUNDED') DEFAULT 'UNPAID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_room_id (room_id),
    INDEX idx_check_in (check_in),
    INDEX idx_status (status),
    INDEX idx_payment_status (payment_status),
    INDEX idx_created_at (created_at)
);

-- Database cho payment service
USE payment_service;

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    amount BIGINT NOT NULL,
    currency VARCHAR(3) DEFAULT 'VND',
    stripe_payment_intent_id VARCHAR(255) UNIQUE,
    status ENUM('PENDING', 'SUCCEEDED', 'FAILED', 'CANCELED') DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_status (status),
    INDEX idx_stripe_id (stripe_payment_intent_id),
    INDEX idx_created_at (created_at)
);

SET FOREIGN_KEY_CHECKS = 1;

SELECT '✅ Đã tạo xong tables' as 'STATUS';
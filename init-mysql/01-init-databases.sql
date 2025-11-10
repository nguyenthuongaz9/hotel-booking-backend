-- File: init-mysql/01-init-databases.sql
-- Khởi tạo databases cho Hotel Booking System

SET FOREIGN_KEY_CHECKS = 0;

-- Tạo databases
CREATE DATABASE IF NOT EXISTS hotel_booking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS order_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS payment_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS room_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Sử dụng database chính
USE room_service;

SET FOREIGN_KEY_CHECKS = 1;

SELECT '✅ Đã tạo xong databases' as 'STATUS';
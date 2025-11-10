
USE room_service;

CREATE OR REPLACE VIEW room_details AS
SELECT 
    r.id,
    r.room_number,
    r.type,
    r.price_per_night,
    r.description,
    r.location,
    r.capacity,
    r.amenities,
    r.is_available,
    r.created_at,
    r.updated_at,
    (SELECT JSON_ARRAYAGG(JSON_OBJECT('id', i.id, 'name', i.name, 'image', i.image))
     FROM image i WHERE i.room_id = r.id) as images,
    (SELECT AVG(rv.rating) FROM review rv WHERE rv.room_id = r.id) as average_rating,
    (SELECT COUNT(rv.id) FROM review rv WHERE rv.room_id = r.id) as total_reviews
FROM room r;

CREATE OR REPLACE VIEW room_review_stats AS
SELECT 
    r.id as room_id,
    r.room_number,
    COUNT(rv.id) as total_reviews,
    AVG(rv.rating) as average_rating,
    MIN(rv.rating) as min_rating,
    MAX(rv.rating) as max_rating
FROM room r
LEFT JOIN review rv ON r.id = rv.room_id
GROUP BY r.id, r.room_number;

USE order_service;

CREATE OR REPLACE VIEW order_details AS
SELECT 
    o.id,
    o.user_id,
    o.room_id,
    o.check_in,
    o.check_out,
    o.total_price,
    o.status,
    o.payment_status,
    o.created_at,
    o.updated_at,
    DATEDIFF(o.check_out, o.check_in) as nights
FROM orders o;

USE payment_service;

CREATE OR REPLACE VIEW payment_details AS
SELECT 
    p.id,
    p.order_id,
    p.amount,
    p.currency,
    p.stripe_payment_intent_id,
    p.status,
    p.payment_method,
    p.created_at,
    p.updated_at
FROM payments p;

USE room_service;

DELIMITER //

CREATE PROCEDURE FindAvailableRooms(
    IN p_check_in DATE,
    IN p_check_out DATE,
    IN p_room_type VARCHAR(20),
    IN p_max_price DECIMAL(10,2)
)
BEGIN
    SELECT 
        rd.*
    FROM room_details rd
    WHERE rd.is_available = TRUE
        AND (p_room_type IS NULL OR rd.type = p_room_type)
        AND (p_max_price IS NULL OR rd.price_per_night <= p_max_price)
        AND rd.id NOT IN (
            SELECT room_id 
            FROM order_service.orders 
            WHERE status IN ('CONFIRMED', 'PENDING')
                AND (
                    (check_in BETWEEN p_check_in AND p_check_out) OR
                    (check_out BETWEEN p_check_in AND p_check_out) OR
                    (check_in <= p_check_in AND check_out >= p_check_out)
                )
        )
    ORDER BY rd.average_rating DESC, rd.price_per_night ASC;
END //

CREATE PROCEDURE UpdateRoomAvailability(IN p_room_id VARCHAR(36), IN p_available BOOLEAN)
BEGIN
    UPDATE room 
    SET is_available = p_available, 
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_room_id;
END //

DELIMITER ;

SELECT '✅ Đã tạo xong views và stored procedures' as 'STATUS';
-- ============================================================================
-- Smart Amusement Park Management System (v2) - Supabase Database Schema
-- Run this script in the Supabase SQL Editor.
-- ============================================================================

-- 1. Create Rides Table
CREATE TABLE IF NOT EXISTS rides (
    ride_id INT PRIMARY KEY,
    ride_name VARCHAR(50) NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0),
    unit_price INT NOT NULL CHECK (unit_price >= 0),
    min_age INT NOT NULL CHECK (min_age >= 0)
);

-- Seed Fixed Ride Catalogue
INSERT INTO rides (ride_id, ride_name, capacity, unit_price, min_age) VALUES
(101, 'Ferris Wheel', 20, 100, 5),
(102, 'Roller Coaster', 15, 200, 10),
(103, 'Water Ride', 12, 150, 6),
(104, 'Go-Kart', 10, 120, 12)
ON CONFLICT (ride_id) DO UPDATE SET
    capacity = EXCLUDED.capacity,
    unit_price = EXCLUDED.unit_price,
    min_age = EXCLUDED.min_age;

-- 2. Create Bookings Table
CREATE TABLE IF NOT EXISTS bookings (
    booking_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    visitor_name VARCHAR(100) NOT NULL,
    age INT NOT NULL CHECK (age BETWEEN 1 AND 100),
    ride_id INT NOT NULL REFERENCES rides(ride_id),
    group_size INT NOT NULL CHECK (group_size BETWEEN 1 AND 10),
    unit_price INT NOT NULL,
    discount_pct INT NOT NULL DEFAULT 0,
    total_amount INT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('CONFIRMED', 'WAITLISTED', 'CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. Availability and Revenue Views
CREATE OR REPLACE VIEW ride_availability AS
SELECT 
    r.ride_id,
    r.ride_name,
    r.capacity,
    COALESCE(SUM(b.group_size) FILTER (WHERE b.status = 'CONFIRMED'), 0) AS booked_seats,
    r.capacity - COALESCE(SUM(b.group_size) FILTER (WHERE b.status = 'CONFIRMED'), 0) AS available_seats
FROM rides r
LEFT JOIN bookings b ON r.ride_id = b.ride_id
GROUP BY r.ride_id, r.ride_name, r.capacity;

CREATE OR REPLACE VIEW revenue_summary AS
SELECT 
    r.ride_id,
    r.ride_name,
    COUNT(b.booking_id) FILTER (WHERE b.status = 'CONFIRMED') AS confirmed_bookings,
    COALESCE(SUM(b.total_amount) FILTER (WHERE b.status = 'CONFIRMED'), 0) AS total_revenue
FROM rides r
LEFT JOIN bookings b ON r.ride_id = b.ride_id
GROUP BY r.ride_id, r.ride_name;

-- 4. Transactional Booking Procedure with Row-Level Lock
CREATE OR REPLACE FUNCTION book_ticket(
    p_visitor_name VARCHAR,
    p_age INT,
    p_ride_id INT,
    p_group_size INT
) RETURNS JSONB
LANGUAGE plpgsql
AS $$
DECLARE
    v_ride rides%ROWTYPE;
    v_booked INT;
    v_avail INT;
    v_disc INT := 0;
    v_total INT;
    v_status VARCHAR;
    v_booking_id BIGINT;
BEGIN
    -- Lock ride row to avoid race condition on last remaining seats
    SELECT * INTO v_ride FROM rides WHERE ride_id = p_ride_id FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Ride % does not exist', p_ride_id;
    END IF;

    IF p_age < v_ride.min_age THEN
        RAISE EXCEPTION 'Visitor age % is below minimum age %', p_age, v_ride.min_age;
    END IF;

    SELECT COALESCE(SUM(group_size), 0) INTO v_booked
    FROM bookings
    WHERE ride_id = p_ride_id AND status = 'CONFIRMED';

    v_avail := v_ride.capacity - v_booked;

    IF p_age < 12 THEN
        v_disc := 50;
    END IF;
    v_total := (v_ride.unit_price * (100 - v_disc) / 100) * p_group_size;

    IF p_group_size <= v_avail THEN
        v_status := 'CONFIRMED';
    ELSE
        v_status := 'WAITLISTED';
    END IF;

    INSERT INTO bookings (visitor_name, age, ride_id, group_size, unit_price, discount_pct, total_amount, status)
    VALUES (p_visitor_name, p_age, p_ride_id, p_group_size, v_ride.unit_price, v_disc, v_total, v_status)
    RETURNING booking_id INTO v_booking_id;

    RETURN jsonb_build_object(
        'booking_id', v_booking_id,
        'status', v_status,
        'available_seats', v_avail - CASE WHEN v_status = 'CONFIRMED' THEN p_group_size ELSE 0 END,
        'total_amount', v_total
    );
END;
$$;

-- 5. Cancel Booking & Waitlist Promotion Procedure
CREATE OR REPLACE FUNCTION cancel_booking(p_booking_id BIGINT)
RETURNS JSONB
LANGUAGE plpgsql
AS $$
DECLARE
    v_b bookings%ROWTYPE;
    v_promoted bookings%ROWTYPE;
    v_avail INT;
BEGIN
    SELECT * INTO v_b FROM bookings WHERE booking_id = p_booking_id FOR UPDATE;
    IF NOT FOUND OR v_b.status != 'CONFIRMED' THEN
        RETURN jsonb_build_object('success', false, 'message', 'Booking not eligible for cancellation');
    END IF;

    UPDATE bookings SET status = 'CANCELLED' WHERE booking_id = p_booking_id;

    -- Calculate newly available seats
    SELECT r.capacity - COALESCE(SUM(b.group_size), 0) INTO v_avail
    FROM rides r
    LEFT JOIN bookings b ON r.ride_id = b.ride_id AND b.status = 'CONFIRMED'
    WHERE r.ride_id = v_b.ride_id
    GROUP BY r.capacity;

    -- Find earliest waitlisted booking that fits
    SELECT * INTO v_promoted FROM bookings
    WHERE ride_id = v_b.ride_id AND status = 'WAITLISTED' AND group_size <= v_avail
    ORDER BY created_at ASC
    LIMIT 1
    FOR UPDATE;

    IF FOUND THEN
        UPDATE bookings SET status = 'CONFIRMED' WHERE booking_id = v_promoted.booking_id;
    END IF;

    RETURN jsonb_build_object(
        'success', true,
        'cancelled_id', p_booking_id,
        'promoted_id', v_promoted.booking_id
    );
END;
$$;

-- 6. Row Level Security (RLS) Setup
ALTER TABLE rides ENABLE ROW LEVEL SECURITY;
ALTER TABLE bookings ENABLE ROW LEVEL SECURITY;

-- Allow anonymous read on rides and bookings
DROP POLICY IF EXISTS "Allow anon read rides" ON rides;
CREATE POLICY "Allow anon read rides" ON rides FOR SELECT TO anon USING (true);

DROP POLICY IF EXISTS "Allow anon read bookings" ON bookings;
CREATE POLICY "Allow anon read bookings" ON bookings FOR SELECT TO anon USING (true);

-- Financial record immutability: NO DELETE policy exists. Records are permanent.

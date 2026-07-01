SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE bookings;
TRUNCATE TABLE courts;
TRUNCATE TABLE time_slots;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO users (id, username, email, password, role, is_active, created_at) VALUES 
(1, 'customer', 'customer@test.com', '$2a$10$NIx8HUIuobMVLHne/XydgeKnFPqEfMeml0ldsTZzp0tm.LMAcCyu6', 'CUSTOMER', 1, NOW()),
(2, 'manager', 'manager@test.com', '$2a$10$uELAJ/mFibBovlR2GzzG/.yeFZmS0zMgJtOZtWDPqt26dJX9EXUni', 'MANAGER', 1, NOW()),
(3, 'admin', 'admin@test.com', '$2a$10$9MaHuy14pGJA0wgD32iVNepQbgycv21CG45085SvK0/iLzzJ6FKYm', 'ADMIN', 1, NOW());

INSERT INTO time_slots (id, start_time, end_time, label) VALUES 
(1, '07:00:00', '08:00:00', '07:00 - 08:00'),
(2, '08:00:00', '09:00:00', '08:00 - 09:00'),
(3, '09:00:00', '10:00:00', '09:00 - 10:00'),
(4, '10:00:00', '11:00:00', '10:00 - 11:00');

INSERT INTO courts (id, name, address, price_per_hour, manager_id, created_at) VALUES 
(1, 'Sân số 1', '76 Lê Xuân Hoa', 80000.0, 2, NOW()),
(2, 'Sân số 2', '97 Man Thiện', 85000.0, 2, NOW());

INSERT INTO bookings (id, customer_id, court_id, time_slot_id, booking_date, status, created_at) VALUES 
(1, 1, 1, 1, '2026-07-10', 'PENDING', NOW()),
(2, 1, 2, 2, '2026-07-10', 'CONFIRMED', NOW());
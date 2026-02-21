-- ============================================================================
-- FLEET FLOW — Mock Data SQL Script
-- ============================================================================
-- Purpose  : Seed database with realistic test data for Postman API testing
-- Database : Fleet_Flow_DB (MySQL 8+)
-- Password : All users share password  →  Password@123
-- Order    : FK-safe insertion order
-- ============================================================================
-- IMPORTANT: Run the Spring Boot app at least once BEFORE executing this script
--            so that Hibernate creates all tables (ddl-auto=update).
-- ============================================================================

USE Fleet_Flow_DB;

-- ────────────────────────────────────────────────────────────────────────────
-- 0. Clean slate (optional — uncomment to reset)
-- ────────────────────────────────────────────────────────────────────────────
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE fuel_logs;
-- TRUNCATE TABLE expenses;
-- TRUNCATE TABLE maintenance_logs;
-- TRUNCATE TABLE trips;
-- TRUNCATE TABLE drivers;
-- TRUNCATE TABLE vehicles;
-- TRUNCATE TABLE verification_tokens;
-- TRUNCATE TABLE password_reset_tokens;
-- TRUNCATE TABLE monthly_financial_summaries;
-- TRUNCATE TABLE users;
-- SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 1. USERS  (8 users — 2 per RBAC role)
-- ============================================================================
-- BCrypt hash of "Password@123" (cost 10):
-- $2a$10$XSU8AnYKDs2CkNGDlb3mDuwxL4NlSIFfz20kbOrX0Mk4EOKDUF.4C

INSERT INTO users (id, name, email, password_hash, employee_id, phone, role, is_verified, is_active, created_at, updated_at) VALUES
-- FLEET_MANAGER (2)
(UUID_TO_BIN('11111111-1111-1111-1111-111111111101'), 'Rajesh Kumar',   'rajesh.kumar@fleetflow.com',   '$2a$10$XSU8AnYKDs2CkNGDlb3mDuwxL4NlSIFfz20kbOrX0Mk4EOKDUF.4C', 'EMP-FM-001', '+919876543210', 'FLEET_MANAGER',    TRUE, TRUE, NOW(), NOW()),
(UUID_TO_BIN('11111111-1111-1111-1111-111111111102'), 'Priya Sharma',   'priya.sharma@fleetflow.com',   '$2a$10$XSU8AnYKDs2CkNGDlb3mDuwxL4NlSIFfz20kbOrX0Mk4EOKDUF.4C', 'EMP-FM-002', '+919876543211', 'FLEET_MANAGER',    TRUE, TRUE, NOW(), NOW()),

-- DISPATCHER (2)
(UUID_TO_BIN('22222222-2222-2222-2222-222222222201'), 'Amit Patel',     'amit.patel@fleetflow.com',     '$2a$10$XSU8AnYKDs2CkNGDlb3mDuwxL4NlSIFfz20kbOrX0Mk4EOKDUF.4C', 'EMP-DP-001', '+919876543212', 'DISPATCHER',       TRUE, TRUE, NOW(), NOW()),
(UUID_TO_BIN('22222222-2222-2222-2222-222222222202'), 'Sneha Reddy',    'sneha.reddy@fleetflow.com',    '$2a$10$XSU8AnYKDs2CkNGDlb3mDuwxL4NlSIFfz20kbOrX0Mk4EOKDUF.4C', 'EMP-DP-002', '+919876543213', 'DISPATCHER',       TRUE, TRUE, NOW(), NOW()),

-- SAFETY_OFFICER (2)
(UUID_TO_BIN('33333333-3333-3333-3333-333333333301'), 'Vikram Singh',   'vikram.singh@fleetflow.com',   '$2a$10$XSU8AnYKDs2CkNGDlb3mDuwxL4NlSIFfz20kbOrX0Mk4EOKDUF.4C', 'EMP-SO-001', '+919876543214', 'SAFETY_OFFICER',   TRUE, TRUE, NOW(), NOW()),
(UUID_TO_BIN('33333333-3333-3333-3333-333333333302'), 'Ananya Gupta',   'ananya.gupta@fleetflow.com',   '$2a$10$XSU8AnYKDs2CkNGDlb3mDuwxL4NlSIFfz20kbOrX0Mk4EOKDUF.4C', 'EMP-SO-002', '+919876543215', 'SAFETY_OFFICER',   TRUE, TRUE, NOW(), NOW()),

-- FINANCIAL_ANALYST (2)
(UUID_TO_BIN('44444444-4444-4444-4444-444444444401'), 'Karan Mehta',    'karan.mehta@fleetflow.com',    '$2a$10$XSU8AnYKDs2CkNGDlb3mDuwxL4NlSIFfz20kbOrX0Mk4EOKDUF.4C', 'EMP-FA-001', '+919876543216', 'FINANCIAL_ANALYST', TRUE, TRUE, NOW(), NOW()),
(UUID_TO_BIN('44444444-4444-4444-4444-444444444402'), 'Deepa Nair',     'deepa.nair@fleetflow.com',     '$2a$10$XSU8AnYKDs2CkNGDlb3mDuwxL4NlSIFfz20kbOrX0Mk4EOKDUF.4C', 'EMP-FA-002', '+919876543217', 'FINANCIAL_ANALYST', TRUE, TRUE, NOW(), NOW());


-- ============================================================================
-- 2. VEHICLES  (6 vehicles — various types & statuses)
-- ============================================================================
-- created_by → FLEET_MANAGER users (FM1 & FM2)
-- Statuses must be CONSISTENT with trips & maintenance data below

INSERT INTO vehicles (id, license_plate, name, model, type, max_load_capacity, odometer, status, acquisition_cost, created_by, created_at, updated_at) VALUES
-- V1: TRUCK, AVAILABLE (completed trips 1, 6; cancelled trip 5; draft trip 4)
(UUID_TO_BIN('55555555-5555-5555-5555-555555555501'), 'MH-12-AB-1234', 'Tata Prima',      'Prima 4928.S',    'TRUCK',         10000.0,  13680.0, 'AVAILABLE', 3500000.00, UUID_TO_BIN('11111111-1111-1111-1111-111111111101'), NOW(), NOW()),

-- V2: VAN, AVAILABLE (completed trips 2, 8; draft trip 7)
(UUID_TO_BIN('55555555-5555-5555-5555-555555555502'), 'KA-01-CD-5678', 'Mahindra Supro',  'Supro Cargo Van',  'VAN',           3000.0,   6465.0,  'AVAILABLE', 850000.00,  UUID_TO_BIN('11111111-1111-1111-1111-111111111101'), NOW(), NOW()),

-- V3: BIKE, AVAILABLE (draft trip 7 uses this — actually let me use V3 for a standalone)
(UUID_TO_BIN('55555555-5555-5555-5555-555555555503'), 'DL-05-EF-9012', 'Bajaj Maxima',    'Maxima Cargo',     'BIKE',          50.0,     2300.0,  'AVAILABLE', 120000.00,  UUID_TO_BIN('11111111-1111-1111-1111-111111111102'), NOW(), NOW()),

-- V4: MINI, ON_TRIP (active trip 3 — IN_TRANSIT)
(UUID_TO_BIN('55555555-5555-5555-5555-555555555504'), 'TN-09-GH-3456', 'Tata Ace Gold',   'Ace Gold Diesel',  'MINI',          1500.0,   3000.0,  'ON_TRIP',   550000.00,  UUID_TO_BIN('11111111-1111-1111-1111-111111111102'), NOW(), NOW()),

-- V5: TRAILER_TRUCK, IN_SHOP (has open maintenance logs)
(UUID_TO_BIN('55555555-5555-5555-5555-555555555505'), 'GJ-06-IJ-7890', 'Ashok Leyland',   'Captain 4923',     'TRAILER_TRUCK', 25000.0,  45000.0, 'IN_SHOP',   5200000.00, UUID_TO_BIN('11111111-1111-1111-1111-111111111101'), NOW(), NOW()),

-- V6: TRUCK, RETIRED (decommissioned)
(UUID_TO_BIN('55555555-5555-5555-5555-555555555506'), 'RJ-14-KL-2345', 'BharatBenz',      '1617R',            'TRUCK',         12000.0,  120000.0,'RETIRED',   2800000.00, UUID_TO_BIN('11111111-1111-1111-1111-111111111102'), NOW(), NOW());


-- ============================================================================
-- 3. DRIVERS  (6 drivers — various statuses & stats)
-- ============================================================================
-- created_by → SAFETY_OFFICER users (SO1 & SO2)
-- License expiry dates must be in the FUTURE for active drivers
-- Stats must match the trips below

INSERT INTO drivers (id, name, license_number, license_expiry_date, license_category, phone, completion_rate, safety_score, complaints, total_trips_assigned, total_trips_completed, status, created_by, created_at, updated_at) VALUES
-- D1: ON_DUTY — assigned to trip 2 (completed) & trip 5 (cancelled) → assigned=2, completed=1, rate=50%
(UUID_TO_BIN('66666666-6666-6666-6666-666666666601'), 'Suresh Yadav',     'DL-2023-001234', '2027-03-15', 'HMV',  '+919812345601', 50.0,  100.0, 0, 2, 1, 'ON_DUTY',   UUID_TO_BIN('33333333-3333-3333-3333-333333333301'), NOW(), NOW()),

-- D2: ON_DUTY — assigned to trip 6 (completed) → assigned=1, completed=1, rate=100%
(UUID_TO_BIN('66666666-6666-6666-6666-666666666602'), 'Mohammed Irfan',   'KA-2022-005678', '2026-11-20', 'HMV',  '+919812345602', 100.0, 95.0,  1, 1, 1, 'ON_DUTY',   UUID_TO_BIN('33333333-3333-3333-3333-333333333301'), NOW(), NOW()),

-- D3: ON_TRIP — assigned to trip 3 (IN_TRANSIT) → assigned=1, completed=0, rate=0%
(UUID_TO_BIN('66666666-6666-6666-6666-666666666603'), 'Ravi Shankar',     'TN-2024-009012', '2027-06-30', 'HMV',  '+919812345603', 0.0,   100.0, 0, 1, 0, 'ON_TRIP',   UUID_TO_BIN('33333333-3333-3333-3333-333333333302'), NOW(), NOW()),

-- D4: OFF_DUTY — no trips assigned yet
(UUID_TO_BIN('66666666-6666-6666-6666-666666666604'), 'Prakash Dubey',    'MH-2023-003456', '2026-08-15', 'LMV',  '+919812345604', 0.0,   100.0, 0, 0, 0, 'OFF_DUTY',  UUID_TO_BIN('33333333-3333-3333-3333-333333333302'), NOW(), NOW()),

-- D5: ON_DUTY — assigned to trips 1 & 8 (both completed) → assigned=2, completed=2, rate=100%
(UUID_TO_BIN('66666666-6666-6666-6666-666666666605'), 'Arjun Nair',       'KL-2024-007890', '2028-01-10', 'HMV',  '+919812345605', 100.0, 100.0, 0, 2, 2, 'ON_DUTY',   UUID_TO_BIN('33333333-3333-3333-3333-333333333301'), NOW(), NOW()),

-- D6: SUSPENDED — has complaints, no active trips
(UUID_TO_BIN('66666666-6666-6666-6666-666666666606'), 'Deepak Chauhan',   'GJ-2021-002345', '2026-12-31', 'HMV',  '+919812345606', 0.0,   85.0,  3, 0, 0, 'SUSPENDED', UUID_TO_BIN('33333333-3333-3333-3333-333333333302'), NOW(), NOW());


-- ============================================================================
-- 4. TRIPS  (8 trips — various statuses across the lifecycle)
-- ============================================================================
-- dispatched_by → DISPATCHER users (DP1 & DP2)
-- trip_number is auto-sequential
--
-- BUSINESS RULES SATISFIED:
--   ✔ cargoWeight ≤ vehicle.maxLoadCapacity
--   ✔ COMPLETED trips have actualDistance = endOdometer − startOdometer
--   ✔ Vehicle/Driver statuses match current active trips
--   ✔ Only AVAILABLE vehicles & ON_DUTY drivers can be assigned at creation

INSERT INTO trips (id, trip_number, vehicle_id, driver_id, cargo_weight, origin, destination, estimated_fuel_cost, actual_distance, start_odometer, end_odometer, revenue, status, dispatched_by, dispatched_at, completed_at, cancelled_at, cancellation_reason, created_at, updated_at) VALUES

-- Trip 1: COMPLETED — V1 (TRUCK 10000kg), D5, cargo 5000kg ≤ 10000kg ✔
(UUID_TO_BIN('77777777-7777-7777-7777-777777777701'), 1,
 UUID_TO_BIN('55555555-5555-5555-5555-555555555501'),  -- V1
 UUID_TO_BIN('66666666-6666-6666-6666-666666666605'),  -- D5
 5000.0, 'Mumbai, Maharashtra', 'Delhi, NCR',
 12500.00,          -- estimatedFuelCost
 1400.0,            -- actualDistance (endOdometer − startOdometer = 13400 − 12000)
 12000.0,           -- startOdometer
 13400.0,           -- endOdometer
 45000.00,          -- revenue
 'COMPLETED',
 UUID_TO_BIN('22222222-2222-2222-2222-222222222201'),   -- DP1
 '2025-01-03 08:00:00', '2025-01-05 18:30:00', NULL, NULL,
 '2025-01-02 10:00:00', '2025-01-05 18:30:00'),

-- Trip 2: COMPLETED — V2 (VAN 3000kg), D1, cargo 1500kg ≤ 3000kg ✔
(UUID_TO_BIN('77777777-7777-7777-7777-777777777702'), 2,
 UUID_TO_BIN('55555555-5555-5555-5555-555555555502'),  -- V2
 UUID_TO_BIN('66666666-6666-6666-6666-666666666601'),  -- D1
 1500.0, 'Pune, Maharashtra', 'Bangalore, Karnataka',
 6800.00,
 840.0,             -- actualDistance = 5840 − 5000
 5000.0,
 5840.0,
 28000.00,
 'COMPLETED',
 UUID_TO_BIN('22222222-2222-2222-2222-222222222201'),   -- DP1
 '2025-01-06 07:30:00', '2025-01-08 16:00:00', NULL, NULL,
 '2025-01-05 20:00:00', '2025-01-08 16:00:00'),

-- Trip 3: IN_TRANSIT — V4 (MINI 1500kg, ON_TRIP), D3 (ON_TRIP), cargo 1000kg ≤ 1500kg ✔
(UUID_TO_BIN('77777777-7777-7777-7777-777777777703'), 3,
 UUID_TO_BIN('55555555-5555-5555-5555-555555555504'),  -- V4
 UUID_TO_BIN('66666666-6666-6666-6666-666666666603'),  -- D3
 1000.0, 'Chennai, Tamil Nadu', 'Hyderabad, Telangana',
 3600.00,
 NULL,              -- not completed yet
 3000.0,            -- startOdometer (current V4 odometer at dispatch)
 NULL,              -- endOdometer (not completed)
 NULL,              -- revenue (not completed)
 'IN_TRANSIT',
 UUID_TO_BIN('22222222-2222-2222-2222-222222222202'),   -- DP2
 '2025-01-20 06:00:00', NULL, NULL, NULL,
 '2025-01-19 14:00:00', '2025-01-20 09:00:00'),

-- Trip 4: DRAFT — V1 (AVAILABLE), D1 (ON_DUTY), cargo 8000kg ≤ 10000kg ✔
(UUID_TO_BIN('77777777-7777-7777-7777-777777777704'), 4,
 UUID_TO_BIN('55555555-5555-5555-5555-555555555501'),  -- V1
 UUID_TO_BIN('66666666-6666-6666-6666-666666666601'),  -- D1
 8000.0, 'Delhi, NCR', 'Kolkata, West Bengal',
 15000.00,
 NULL, NULL, NULL, NULL,
 'DRAFT',
 NULL, NULL, NULL, NULL, NULL,
 '2025-01-22 11:00:00', '2025-01-22 11:00:00'),

-- Trip 5: CANCELLED — V1, D1, was DISPATCHED then cancelled, statuses reset
(UUID_TO_BIN('77777777-7777-7777-7777-777777777705'), 5,
 UUID_TO_BIN('55555555-5555-5555-5555-555555555501'),  -- V1
 UUID_TO_BIN('66666666-6666-6666-6666-666666666601'),  -- D1
 6000.0, 'Mumbai, Maharashtra', 'Goa',
 5500.00,
 NULL,
 13400.0,           -- startOdometer (set at dispatch, before trip 6)
 NULL, NULL,
 'CANCELLED',
 UUID_TO_BIN('22222222-2222-2222-2222-222222222202'),   -- DP2
 '2025-01-10 09:00:00', NULL, '2025-01-10 14:00:00', 'Client cancelled the shipment order',
 '2025-01-09 17:00:00', '2025-01-10 14:00:00'),

-- Trip 6: COMPLETED — V1 (TRUCK), D2, cargo 7500kg ≤ 10000kg ✔
(UUID_TO_BIN('77777777-7777-7777-7777-777777777706'), 6,
 UUID_TO_BIN('55555555-5555-5555-5555-555555555501'),  -- V1
 UUID_TO_BIN('66666666-6666-6666-6666-666666666602'),  -- D2
 7500.0, 'Delhi, NCR', 'Jaipur, Rajasthan',
 3500.00,
 280.0,             -- actualDistance = 13680 − 13400
 13400.0,
 13680.0,
 18000.00,
 'COMPLETED',
 UUID_TO_BIN('22222222-2222-2222-2222-222222222201'),   -- DP1
 '2025-01-12 07:00:00', '2025-01-12 18:00:00', NULL, NULL,
 '2025-01-11 20:00:00', '2025-01-12 18:00:00'),

-- Trip 7: DRAFT — V3 (BIKE 50kg, AVAILABLE), D2 (ON_DUTY), cargo 30kg ≤ 50kg ✔
(UUID_TO_BIN('77777777-7777-7777-7777-777777777707'), 7,
 UUID_TO_BIN('55555555-5555-5555-5555-555555555503'),  -- V3
 UUID_TO_BIN('66666666-6666-6666-6666-666666666602'),  -- D2
 30.0, 'Connaught Place, Delhi', 'Saket, Delhi',
 200.00,
 NULL, NULL, NULL, NULL,
 'DRAFT',
 NULL, NULL, NULL, NULL, NULL,
 '2025-01-23 09:00:00', '2025-01-23 09:00:00'),

-- Trip 8: COMPLETED — V2 (VAN 3000kg), D5, cargo 2000kg ≤ 3000kg ✔
(UUID_TO_BIN('77777777-7777-7777-7777-777777777708'), 8,
 UUID_TO_BIN('55555555-5555-5555-5555-555555555502'),  -- V2
 UUID_TO_BIN('66666666-6666-6666-6666-666666666605'),  -- D5
 2000.0, 'Hyderabad, Telangana', 'Chennai, Tamil Nadu',
 5200.00,
 625.0,             -- actualDistance = 6465 − 5840
 5840.0,
 6465.0,
 22000.00,
 'COMPLETED',
 UUID_TO_BIN('22222222-2222-2222-2222-222222222202'),   -- DP2
 '2025-01-14 06:30:00', '2025-01-15 17:00:00', NULL, NULL,
 '2025-01-13 19:00:00', '2025-01-15 17:00:00');


-- ============================================================================
-- 5. MAINTENANCE LOGS  (4 logs — various statuses)
-- ============================================================================
-- created_by → FLEET_MANAGER users (FM1 & FM2)
-- Vehicle with status IN_SHOP (V5) must have open (NEW/IN_PROGRESS) logs

INSERT INTO maintenance_logs (id, vehicle_id, service_name, issue_description, service_date, cost, status, created_by, created_at, updated_at) VALUES

-- ML1: V5 — NEW maintenance (keeps V5 IN_SHOP)
(UUID_TO_BIN('88888888-8888-8888-8888-888888888801'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555505'),  -- V5
 'Engine Overhaul', 'Engine overheating detected during long-haul trip. Full engine inspection and overhaul required.',
 '2025-01-18', 75000.00, 'NEW',
 UUID_TO_BIN('11111111-1111-1111-1111-111111111101'),   -- FM1
 '2025-01-18 10:00:00', '2025-01-18 10:00:00'),

-- ML2: V5 — IN_PROGRESS maintenance (another open log for V5)
(UUID_TO_BIN('88888888-8888-8888-8888-888888888802'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555505'),  -- V5
 'Brake Pad Replacement', 'Worn brake pads identified during routine inspection. Front and rear pads need replacement.',
 '2025-01-19', 12000.00, 'IN_PROGRESS',
 UUID_TO_BIN('11111111-1111-1111-1111-111111111101'),   -- FM1
 '2025-01-19 09:00:00', '2025-01-20 14:00:00'),

-- ML3: V1 — RESOLVED (historical, V1 is back to AVAILABLE)
(UUID_TO_BIN('88888888-8888-8888-8888-888888888803'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555501'),  -- V1
 'Oil Change & Filter', 'Scheduled 10,000 km service. Oil change, oil filter, and air filter replaced.',
 '2024-12-05', 4500.00, 'RESOLVED',
 UUID_TO_BIN('11111111-1111-1111-1111-111111111102'),   -- FM2
 '2024-12-05 08:00:00', '2024-12-06 16:00:00'),

-- ML4: V2 — RESOLVED (historical)
(UUID_TO_BIN('88888888-8888-8888-8888-888888888804'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555502'),  -- V2
 'Tyre Rotation & Alignment', 'Uneven tyre wear noticed. Full rotation and wheel alignment performed.',
 '2024-11-20', 6500.00, 'RESOLVED',
 UUID_TO_BIN('11111111-1111-1111-1111-111111111102'),   -- FM2
 '2024-11-20 11:00:00', '2024-11-21 15:00:00');


-- ============================================================================
-- 6. EXPENSES  (4 expenses — only for COMPLETED trips, 1 per trip)
-- ============================================================================
-- created_by → FINANCIAL_ANALYST users (FA1 & FA2)
-- total_cost = fuel_cost + misc_expense  (auto-calculated by @PrePersist)
-- Each expense links to the trip's vehicle and driver

INSERT INTO expenses (id, trip_id, vehicle_id, driver_id, distance, fuel_cost, misc_expense, total_cost, status, created_by, created_at, updated_at) VALUES

-- E1: Trip 1 (COMPLETED — Mumbai→Delhi), V1, D5
(UUID_TO_BIN('99999999-9999-9999-9999-999999999901'),
 UUID_TO_BIN('77777777-7777-7777-7777-777777777701'),  -- Trip 1
 UUID_TO_BIN('55555555-5555-5555-5555-555555555501'),  -- V1
 UUID_TO_BIN('66666666-6666-6666-6666-666666666605'),  -- D5
 1400.0,            -- distance km
 12500.00,          -- fuelCost
 2500.00,           -- miscExpense (tolls, food, parking)
 15000.00,          -- totalCost = 12500 + 2500
 'APPROVED',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444401'),   -- FA1
 '2025-01-06 10:00:00', '2025-01-07 09:00:00'),

-- E2: Trip 2 (COMPLETED — Pune→Bangalore), V2, D1
(UUID_TO_BIN('99999999-9999-9999-9999-999999999902'),
 UUID_TO_BIN('77777777-7777-7777-7777-777777777702'),  -- Trip 2
 UUID_TO_BIN('55555555-5555-5555-5555-555555555502'),  -- V2
 UUID_TO_BIN('66666666-6666-6666-6666-666666666601'),  -- D1
 840.0,
 6800.00,
 1200.00,
 8000.00,           -- totalCost = 6800 + 1200
 'DONE',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444401'),   -- FA1
 '2025-01-09 11:00:00', '2025-01-10 10:00:00'),

-- E3: Trip 6 (COMPLETED — Delhi→Jaipur), V1, D2
(UUID_TO_BIN('99999999-9999-9999-9999-999999999903'),
 UUID_TO_BIN('77777777-7777-7777-7777-777777777706'),  -- Trip 6
 UUID_TO_BIN('55555555-5555-5555-5555-555555555501'),  -- V1
 UUID_TO_BIN('66666666-6666-6666-6666-666666666602'),  -- D2
 280.0,
 3500.00,
 500.00,
 4000.00,           -- totalCost = 3500 + 500
 'PENDING',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444402'),   -- FA2
 '2025-01-13 09:00:00', '2025-01-13 09:00:00'),

-- E4: Trip 8 (COMPLETED — Hyderabad→Chennai), V2, D5
(UUID_TO_BIN('99999999-9999-9999-9999-999999999904'),
 UUID_TO_BIN('77777777-7777-7777-7777-777777777708'),  -- Trip 8
 UUID_TO_BIN('55555555-5555-5555-5555-555555555502'),  -- V2
 UUID_TO_BIN('66666666-6666-6666-6666-666666666605'),  -- D5
 625.0,
 5200.00,
 800.00,
 6000.00,           -- totalCost = 5200 + 800
 'APPROVED',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444402'),   -- FA2
 '2025-01-16 10:00:00', '2025-01-17 09:00:00');


-- ============================================================================
-- 7. FUEL LOGS  (6 records — some trip-linked, some standalone)
-- ============================================================================
-- recorded_by → FINANCIAL_ANALYST users (FA1 & FA2)

INSERT INTO fuel_logs (id, vehicle_id, trip_id, liters, cost, odometer_at_fill, fill_date, recorded_by, created_at, updated_at) VALUES

-- FL1: V1, Trip 1 (en-route Mumbai→Delhi)
(UUID_TO_BIN('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa001'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555501'),  -- V1
 UUID_TO_BIN('77777777-7777-7777-7777-777777777701'),  -- Trip 1
 120.0, 10200.00, 13000.0,
 '2025-01-04 14:30:00',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444401'),   -- FA1
 '2025-01-04 15:00:00', '2025-01-04 15:00:00'),

-- FL2: V2, Trip 2 (en-route Pune→Bangalore)
(UUID_TO_BIN('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa002'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555502'),  -- V2
 UUID_TO_BIN('77777777-7777-7777-7777-777777777702'),  -- Trip 2
 65.0, 5850.00, 5500.0,
 '2025-01-07 11:00:00',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444401'),   -- FA1
 '2025-01-07 11:30:00', '2025-01-07 11:30:00'),

-- FL3: V4, Trip 3 (en-route Chennai→Hyderabad — currently IN_TRANSIT)
(UUID_TO_BIN('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa003'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555504'),  -- V4
 UUID_TO_BIN('77777777-7777-7777-7777-777777777703'),  -- Trip 3
 40.0, 3600.00, 3050.0,
 '2025-01-20 12:00:00',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444402'),   -- FA2
 '2025-01-20 12:30:00', '2025-01-20 12:30:00'),

-- FL4: V1, standalone refuel (no trip — depot fill)
(UUID_TO_BIN('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa004'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555501'),  -- V1
 NULL,                                                  -- no trip
 130.0, 11050.00, 13680.0,
 '2025-01-25 09:00:00',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444401'),   -- FA1
 '2025-01-25 09:30:00', '2025-01-25 09:30:00'),

-- FL5: V2, Trip 8 (en-route Hyderabad→Chennai)
(UUID_TO_BIN('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa005'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555502'),  -- V2
 UUID_TO_BIN('77777777-7777-7777-7777-777777777708'),  -- Trip 8
 55.0, 4950.00, 6200.0,
 '2025-01-14 15:00:00',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444402'),   -- FA2
 '2025-01-14 15:30:00', '2025-01-14 15:30:00'),

-- FL6: V5, standalone refuel (before it went to shop)
(UUID_TO_BIN('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa006'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555505'),  -- V5
 NULL,
 200.0, 18000.00, 45000.0,
 '2024-12-20 10:00:00',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444401'),   -- FA1
 '2024-12-20 10:30:00', '2024-12-20 10:30:00');


-- ============================================================================
-- 8. MONTHLY FINANCIAL SUMMARIES  (3 months)
-- ============================================================================
-- netProfit = revenue − fuelCost − maintenanceCost

INSERT INTO monthly_financial_summaries (id, year, month, revenue, fuel_cost, maintenance_cost, net_profit, total_trips, total_distance, total_fuel_liters, created_at, updated_at) VALUES

-- November 2024
(UUID_TO_BIN('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb001'),
 2024, 11,
 85000.00,   -- revenue
 25000.00,   -- fuelCost
 15000.00,   -- maintenanceCost
 45000.00,   -- netProfit = 85000 − 25000 − 15000
 5, 3200.0, 350.0,
 '2024-12-01 00:00:00', '2024-12-01 00:00:00'),

-- December 2024
(UUID_TO_BIN('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb002'),
 2024, 12,
 120000.00,
 35000.00,
 20000.00,
 65000.00,   -- netProfit = 120000 − 35000 − 20000
 8, 4500.0, 480.0,
 '2025-01-01 00:00:00', '2025-01-01 00:00:00'),

-- January 2025
(UUID_TO_BIN('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb003'),
 2025, 1,
 113000.00,  -- revenue from trips 1(45000)+2(28000)+6(18000)+8(22000) = 113000
 35650.00,   -- fuelCost from fuel logs: 10200+5850+3600+11050+4950 = 35650
 98000.00,   -- maintenanceCost: sum ML1(75000)+ML2(12000)+ML3(4500)+ML4(6500) = 98000
 -20650.00,  -- netProfit = 113000 − 35650 − 98000 (negative due to heavy maintenance)
 8, 3145.0, 610.0,
 '2025-02-01 00:00:00', '2025-02-01 00:00:00');


-- ============================================================================
-- VERIFICATION — Quick sanity checks (run after insert)
-- ============================================================================

-- Check user counts per role
SELECT role, COUNT(*) AS count FROM users GROUP BY role;

-- Check vehicle status distribution
SELECT status, COUNT(*) AS count FROM vehicles GROUP BY status;

-- Check driver status distribution
SELECT status, COUNT(*) AS count FROM drivers GROUP BY status;

-- Check trip status distribution
SELECT status, COUNT(*) AS count FROM trips GROUP BY status;

-- Check expense status distribution
SELECT status, COUNT(*) AS count FROM expenses GROUP BY status;

-- ============================================================================
-- POSTMAN TESTING — Login credentials
-- ============================================================================
-- POST /api/auth/login
-- {
--   "email": "rajesh.kumar@fleetflow.com",   ← FLEET_MANAGER
--   "password": "Password@123"
-- }
--
-- Use the JWT token from the login response as:
--   Authorization: Bearer <token>
--
-- User Credentials Quick Reference:
-- ┌──────────────────────┬─────────────────────────────────┬───────────────────┐
-- │ Role                 │ Email                           │ Password          │
-- ├──────────────────────┼─────────────────────────────────┼───────────────────┤
-- │ FLEET_MANAGER        │ rajesh.kumar@fleetflow.com      │ Password@123      │
-- │ FLEET_MANAGER        │ priya.sharma@fleetflow.com      │ Password@123      │
-- │ DISPATCHER           │ amit.patel@fleetflow.com        │ Password@123      │
-- │ DISPATCHER           │ sneha.reddy@fleetflow.com       │ Password@123      │
-- │ SAFETY_OFFICER       │ vikram.singh@fleetflow.com      │ Password@123      │
-- │ SAFETY_OFFICER       │ ananya.gupta@fleetflow.com      │ Password@123      │
-- │ FINANCIAL_ANALYST    │ karan.mehta@fleetflow.com       │ Password@123      │
-- │ FINANCIAL_ANALYST    │ deepa.nair@fleetflow.com        │ Password@123      │
-- └──────────────────────┴─────────────────────────────────┴───────────────────┘
--
-- UUID Quick Reference (use BIN_TO_UUID() to read from DB):
-- ┌──────────┬────────────────────────────────────────┬─────────────────┐
-- │ Alias    │ UUID                                   │ Description     │
-- ├──────────┼────────────────────────────────────────┼─────────────────┤
-- │ FM1      │ 11111111-1111-1111-1111-111111111101   │ Rajesh Kumar    │
-- │ FM2      │ 11111111-1111-1111-1111-111111111102   │ Priya Sharma    │
-- │ DP1      │ 22222222-2222-2222-2222-222222222201   │ Amit Patel      │
-- │ DP2      │ 22222222-2222-2222-2222-222222222202   │ Sneha Reddy     │
-- │ SO1      │ 33333333-3333-3333-3333-333333333301   │ Vikram Singh    │
-- │ SO2      │ 33333333-3333-3333-3333-333333333302   │ Ananya Gupta    │
-- │ FA1      │ 44444444-4444-4444-4444-444444444401   │ Karan Mehta     │
-- │ FA2      │ 44444444-4444-4444-4444-444444444402   │ Deepa Nair      │
-- │ V1-V6   │ 55555555-...-5555555555501 to 506      │ Vehicles        │
-- │ D1-D6   │ 66666666-...-6666666666601 to 606      │ Drivers         │
-- │ T1-T8   │ 77777777-...-7777777777701 to 708      │ Trips           │
-- │ ML1-ML4 │ 88888888-...-8888888888801 to 804      │ Maintenance     │
-- │ E1-E4   │ 99999999-...-9999999999901 to 904      │ Expenses        │
-- │ FL1-FL6 │ aaaaaaaa-...-aaaaaaaaaa001 to 006      │ Fuel Logs       │
-- │ MFS1-3  │ bbbbbbbb-...-bbbbbbbbbb001 to 003      │ Fin Summaries   │
-- └──────────┴────────────────────────────────────────┴─────────────────┘

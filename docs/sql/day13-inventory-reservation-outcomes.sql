-- Existing pre-Day-13 database ke liye one-time migration.
-- Inventory-service stop karke run karein.
-- Already migrated database par dobara run na karein.

USE inventory_db;

-- Purani successful reservations ko RESERVED rakhenge.
ALTER TABLE inventory_reservations
    ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'RESERVED',
    ADD COLUMN rejection_reason VARCHAR(64) NULL;

-- Nayi reservations PENDING se start hongi.
ALTER TABLE inventory_reservations
    ALTER COLUMN status SET DEFAULT 'PENDING';
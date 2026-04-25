-- Room migration: add captures table
-- Wire as a Migration(OLD, NEW) in AppDatabase.

CREATE TABLE IF NOT EXISTS `captures` (
    `id`                    INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `capturedAtEpochMs`     INTEGER NOT NULL,
    `mode`                  TEXT NOT NULL,
    `rawImagePath`          TEXT NOT NULL,
    `latitude`              REAL,
    `longitude`             REAL,
    `parsedFieldsJson`      TEXT NOT NULL,
    `status`                TEXT NOT NULL,
    `userEditedAtEpochMs`   INTEGER,
    `linkedEntitiesJson`    TEXT NOT NULL DEFAULT '{}',
    `farmId`                TEXT,
    `syncedAt`              INTEGER
);

CREATE INDEX IF NOT EXISTS `idx_captures_capturedAt` ON `captures` (`capturedAtEpochMs`);
CREATE INDEX IF NOT EXISTS `idx_captures_farmId`     ON `captures` (`farmId`);
CREATE INDEX IF NOT EXISTS `idx_captures_status`     ON `captures` (`status`);
-- Room migration: visual-ingestion full vertical
-- Wire as Migration(N, N+1) in AppDatabase.

-- 1. Fuel logs
CREATE TABLE IF NOT EXISTS `fuel_logs` (
    `id`                INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `capturedAtEpochMs` INTEGER NOT NULL,
    `farmId`            TEXT,
    `totalCost`         REAL,
    `gallons`           REAL,
    `pricePerGallon`    REAL,
    `odometerMiles`     INTEGER,
    `sourceCaptureId`   INTEGER,
    `notes`             TEXT
);
CREATE INDEX IF NOT EXISTS `idx_fuel_capturedAt`  ON `fuel_logs` (`capturedAtEpochMs`);
CREATE INDEX IF NOT EXISTS `idx_fuel_farmId`      ON `fuel_logs` (`farmId`);
CREATE INDEX IF NOT EXISTS `idx_fuel_capture`     ON `fuel_logs` (`sourceCaptureId`);

-- 2. Mileage entries
CREATE TABLE IF NOT EXISTS `mileage_entries` (
    `id`                INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `capturedAtEpochMs` INTEGER NOT NULL,
    `odometerMiles`     INTEGER NOT NULL,
    `farmId`            TEXT,
    `sourceCaptureId`   INTEGER,
    `notes`             TEXT
);
CREATE INDEX IF NOT EXISTS `idx_mileage_capturedAt` ON `mileage_entries` (`capturedAtEpochMs`);
CREATE INDEX IF NOT EXISTS `idx_mileage_farmId`     ON `mileage_entries` (`farmId`);
CREATE INDEX IF NOT EXISTS `idx_mileage_capture`    ON `mileage_entries` (`sourceCaptureId`);

-- 3. Chicken house logs
CREATE TABLE IF NOT EXISTS `chicken_house_logs` (
    `id`                INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `dateIso`           TEXT,
    `farmId`            TEXT,
    `metrics`           TEXT NOT NULL DEFAULT '{}',
    `sourceCaptureId`   INTEGER,
    `notes`             TEXT,
    `createdAtEpochMs`  INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS `idx_ch_date`     ON `chicken_house_logs` (`dateIso`);
CREATE INDEX IF NOT EXISTS `idx_ch_farmId`   ON `chicken_house_logs` (`farmId`);
CREATE INDEX IF NOT EXISTS `idx_ch_capture`  ON `chicken_house_logs` (`sourceCaptureId`);

-- 4. Gauge calibrations
CREATE TABLE IF NOT EXISTS `gauge_calibrations` (
    `gaugeId`            TEXT PRIMARY KEY NOT NULL,
    `gaugeLabel`         TEXT NOT NULL,
    `centerXNorm`        REAL NOT NULL,
    `centerYNorm`        REAL NOT NULL,
    `radiusNorm`         REAL NOT NULL,
    `minAngleDeg`        REAL NOT NULL,
    `minValue`           REAL NOT NULL,
    `maxAngleDeg`        REAL NOT NULL,
    `maxValue`           REAL NOT NULL,
    `unit`               TEXT NOT NULL,
    `counterclockwise`   INTEGER NOT NULL,
    `createdAtEpochMs`   INTEGER NOT NULL,
    `updatedAtEpochMs`   INTEGER NOT NULL
);

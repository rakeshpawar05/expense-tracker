-- 1. Add new columns (nullable initially to avoid breaking existing data)
ALTER TABLE month
ADD COLUMN month_num INT,
ADD COLUMN year_num INT;

-- 2. Backfill existing data
UPDATE month
SET
    month_num = CASE LOWER(name)
        WHEN 'january' THEN 1
        WHEN 'february' THEN 2
        WHEN 'march' THEN 3
        WHEN 'april' THEN 4
        WHEN 'may' THEN 5
        WHEN 'june' THEN 6
        WHEN 'july' THEN 7
        WHEN 'august' THEN 8
        WHEN 'september' THEN 9
        WHEN 'october' THEN 10
        WHEN 'november' THEN 11
        WHEN 'december' THEN 12
        ELSE NULL
    END,
    year_num = year;

-- 3. Optional: add NOT NULL constraint AFTER backfill (safe)
-- (Only if you're confident all rows are populated)
ALTER TABLE month
ALTER COLUMN month_num SET NOT NULL,
ALTER COLUMN year_num SET NOT NULL;

-- 4. Optional but recommended: add index for faster queries
CREATE INDEX idx_month_user_year_month
ON month(user_id, year_num, month_num);

-- 5. Optional: enforce uniqueness (replace old constraint later)
-- CREATE UNIQUE INDEX uq_user_year_month
-- ON month(user_id, year_num, month_num);
-- Safe alteration script that handles both existing and newly initialized environments

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND lower(table_name) = 'month'
    ) THEN
        -- 1. Add new columns if they do not already exist
        ALTER TABLE month
        ADD COLUMN IF NOT EXISTS month_num INT,
        ADD COLUMN IF NOT EXISTS year_num INT;

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
            year_num = year
        WHERE month_num IS NULL;

        -- 3. Add index for faster queries if not exists
        CREATE INDEX IF NOT EXISTS idx_month_user_year_month
        ON month(user_id, year_num, month_num);
    END IF;
END $$;

-- Performance optimization indexes for high-frequency queries

DO $$
BEGIN
    -- 1. Index on expense for user feed, date sorting, and month-level aggregations
    IF EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'public' AND lower(table_name) = 'expense'
    ) THEN
        CREATE INDEX IF NOT EXISTS idx_expense_user_month_date
        ON expense(user_id, month_id, date DESC);

        CREATE INDEX IF NOT EXISTS idx_expense_user_category
        ON expense(user_id, category_id);
    END IF;

    -- 2. Index on saving for user and month queries
    IF EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'public' AND lower(table_name) = 'saving'
    ) THEN
        CREATE INDEX IF NOT EXISTS idx_saving_user_month
        ON saving(user_id, month_id);
    END IF;

    -- 3. Index on category for month-level category lookups
    IF EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'public' AND lower(table_name) = 'category'
    ) THEN
        CREATE INDEX IF NOT EXISTS idx_category_user_month
        ON category(user_id, month_id);
    END IF;
END $$;

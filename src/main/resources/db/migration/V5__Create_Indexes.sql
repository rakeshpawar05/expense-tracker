-- Create indexes for performance optimization

-- Index on expenses table for user_id, month_id, and date (DESC for reverse sorting)
CREATE INDEX idx_expense_user_month_date ON expenses(user_id, month_id, date DESC);

-- Index on categories table for user_id
CREATE INDEX idx_category_user ON category(user_id);

-- Index on savings table for user_id and month_id
CREATE INDEX idx_savings_user_month ON savings(user_id, month_id);


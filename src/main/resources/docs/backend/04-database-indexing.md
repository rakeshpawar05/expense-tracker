# Database Indexing

Indexes improve query performance.

---

# Expense Table

CREATE INDEX idx_expense_user_month_date
ON expenses(user_id, month_id, date DESC);

---

# Category Table

CREATE INDEX idx_category_user
ON categories(user_id);

---

# Savings Table

CREATE INDEX idx_savings_user_month
ON savings(user_id, month_id);

---

# Tasks

- [x] Create migration script
- [x] Apply indexes
- [x] Test query performance

-- Create subscription table for recurring bills and subscriptions
CREATE TABLE IF NOT EXISTS subscription (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    amount INTEGER NOT NULL,
    billing_cycle VARCHAR(50) NOT NULL DEFAULT 'MONTHLY',
    billing_day INTEGER NOT NULL DEFAULT 1,
    category_name VARCHAR(100),
    payment_method VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    last_billed_date VARCHAR(50),
    next_due_date VARCHAR(50),
    notes TEXT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscription_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_subscription_user_status ON subscription(user_id, status);
CREATE INDEX IF NOT EXISTS idx_subscription_user_billing_day ON subscription(user_id, billing_day);

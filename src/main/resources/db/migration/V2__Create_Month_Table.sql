CREATE TABLE months (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    year INT NOT NULL,
    total_earning DOUBLE DEFAULT 0.0,
    total_expenses DOUBLE DEFAULT 0.0,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    CONSTRAINT unique_month_year_user UNIQUE (name, year, user_id)
);
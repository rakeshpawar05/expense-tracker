CREATE TABLE expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    amount DOUBLE NOT NULL,
    date DATE DEFAULT NULL,
    user_id BIGINT NOT NULL,
    category_id BIGINT DEFAULT NULL,
    month_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category (id) ON DELETE SET NULL,
    FOREIGN KEY (month_id) REFERENCES month (id) ON DELETE CASCADE
);
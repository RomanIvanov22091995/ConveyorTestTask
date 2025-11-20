CREATE TABLE IF NOT EXISTS employees (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS rewards (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    reward_id BIGINT NOT NULL,
    reward_name VARCHAR(255) NOT NULL,
    received_date TIMESTAMP NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE INDEX IF NOT EXISTS idx_rewards_employee_id ON rewards(employee_id);


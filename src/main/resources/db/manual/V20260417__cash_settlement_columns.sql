-- Manual migration for cash settlement flow.
-- Apply this on MySQL before running production profile (ddl-auto=validate).

ALTER TABLE payments
    ADD COLUMN settlement_status VARCHAR(50) NULL,
    ADD COLUMN settlement_reference VARCHAR(255) NULL,
    ADD COLUMN settled_by_user_id BIGINT NULL,
    ADD COLUMN settled_at DATETIME NULL;

UPDATE payments
SET settlement_status = CASE
    WHEN UPPER(COALESCE(payment_mode, '')) = 'CASH' THEN 'COLLECTED_UNSETTLED'
    ELSE 'SETTLED'
END
WHERE settlement_status IS NULL;

ALTER TABLE payments
    MODIFY COLUMN settlement_status VARCHAR(50) NOT NULL;

ALTER TABLE payments
    ADD CONSTRAINT fk_payments_settled_by_user
        FOREIGN KEY (settled_by_user_id) REFERENCES users(id);

CREATE INDEX idx_payments_settlement_status ON payments(settlement_status);
CREATE INDEX idx_payments_settled_by_user_id ON payments(settled_by_user_id);


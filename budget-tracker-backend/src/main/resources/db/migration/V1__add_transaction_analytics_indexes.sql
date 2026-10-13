DO $$
BEGIN
    IF to_regclass('public.transactions') IS NOT NULL THEN
        CREATE INDEX IF NOT EXISTS idx_transactions_tenant_transaction_date
            ON transactions (tenant_id, transaction_date);

        CREATE INDEX IF NOT EXISTS idx_transactions_tenant_category_transaction_date
            ON transactions (tenant_id, category_id, transaction_date);
    END IF;
END $$;

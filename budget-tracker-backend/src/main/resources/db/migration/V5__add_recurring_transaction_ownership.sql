ALTER TABLE recurring_transactions
    ADD COLUMN IF NOT EXISTS created_by_id UUID REFERENCES app_users(id);

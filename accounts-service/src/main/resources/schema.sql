-- Drop table if exists (for development purposes)
DROP TABLE IF EXISTS accounts CASCADE;

-- Create accounts table
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    birthday TIMESTAMP WITH TIME ZONE,
    sum BIGINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_accounts_login ON accounts(login);
CREATE INDEX idx_accounts_name ON accounts(name);
CREATE INDEX idx_accounts_birthday ON accounts(birthday);

-- Add constraints
ALTER TABLE accounts 
    ADD CONSTRAINT chk_sum_non_negative CHECK (sum >= 0),
    ADD CONSTRAINT chk_login_not_empty CHECK (length(trim(login)) > 0),
    ADD CONSTRAINT chk_name_not_empty CHECK (length(trim(name)) > 0);

-- Create trigger to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_accounts_updated_at 
    BEFORE UPDATE ON accounts 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Create sequence for account numbers (if needed for business logic)
CREATE SEQUENCE IF NOT EXISTS account_number_seq
    START WITH 1000000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

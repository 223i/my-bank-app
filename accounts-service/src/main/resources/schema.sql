-- Drop table if exists (for development purposes)
DROP TABLE IF EXISTS accounts CASCADE;

-- Create accounts table
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    birthday DATE,
    balance DECIMAL(19, 2) DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_accounts_login ON accounts(login);
CREATE INDEX idx_accounts_first_name ON accounts(first_name);
CREATE INDEX idx_accounts_birthday ON accounts(birthday);

-- Add constraints
ALTER TABLE accounts
    ADD CONSTRAINT chk_balance_non_negative CHECK (balance >= 0),
    ADD CONSTRAINT chk_login_not_empty CHECK (length(trim(login)) > 0),
    ADD CONSTRAINT chk_first_name_not_empty CHECK (length(trim(first_name)) > 0);

-- Create sequence for account numbers
CREATE SEQUENCE IF NOT EXISTS account_number_seq
    START WITH 1000000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE otp_codes (

    id UUID PRIMARY KEY,

    account_id UUID NOT NULL,

    code VARCHAR(10) NOT NULL,

    expires_at TIMESTAMP NOT NULL,

    used BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_otp_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id)
        ON DELETE CASCADE
);
CREATE TABLE account_roles (
    account_id UUID NOT NULL,
    role_id UUID NOT NULL,

    PRIMARY KEY (account_id, role_id),

    CONSTRAINT fk_account_roles_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_account_roles_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE CASCADE
);
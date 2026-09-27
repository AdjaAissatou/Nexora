DROP TABLE IF EXISTS transaction CASCADE;

CREATE TABLE transaction (

    id_transaction BIGSERIAL PRIMARY KEY,

    id_paiement BIGINT NOT NULL,

    reference VARCHAR(100),

    montant NUMERIC(12,2),

    date_transaction TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transaction_paiement
        FOREIGN KEY(id_paiement)
        REFERENCES paiement(id_paiement)
        ON DELETE CASCADE

);
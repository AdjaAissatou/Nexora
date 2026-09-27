DROP TABLE IF EXISTS journal_action CASCADE;

CREATE TABLE journal_action (
    id_journal_action BIGSERIAL PRIMARY KEY,

    id_utilisateur BIGINT,
    module VARCHAR(100) NOT NULL,
    action VARCHAR(150) NOT NULL,
    entite VARCHAR(100),
    id_entite BIGINT,
    description TEXT,

    adresse_ip VARCHAR(50),
    user_agent TEXT,

    date_action TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_journal_action_utilisateur
        FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateurs(id_utilisateur)
        ON DELETE SET NULL
);
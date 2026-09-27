DROP TABLE IF EXISTS litige CASCADE;

CREATE TABLE litige (

    id_litige BIGSERIAL PRIMARY KEY,

    id_commande BIGINT,

    id_reservation BIGINT,

    id_utilisateur BIGINT NOT NULL,

    motif VARCHAR(255),

    description TEXT,

    statut statut_litige DEFAULT 'OUVERT',

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_litige_commande
        FOREIGN KEY(id_commande)
        REFERENCES commande(id_commande),

    CONSTRAINT fk_litige_reservation
        FOREIGN KEY(id_reservation)
        REFERENCES reservation(id_reservation),

    CONSTRAINT fk_litige_utilisateur
        FOREIGN KEY(id_utilisateur)
        REFERENCES utilisateurs(id_utilisateur)

);
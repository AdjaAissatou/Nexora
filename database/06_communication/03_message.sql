DROP TABLE IF EXISTS message CASCADE;

CREATE TABLE message (

    id_message BIGSERIAL PRIMARY KEY,

    id_conversation BIGINT NOT NULL,

    id_expediteur BIGINT NOT NULL,

    contenu TEXT NOT NULL,

    lu BOOLEAN DEFAULT FALSE,

    date_lecture TIMESTAMP,

    date_envoi TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_message_conversation
        FOREIGN KEY(id_conversation)
        REFERENCES conversation(id_conversation)
        ON DELETE CASCADE,

    CONSTRAINT fk_message_utilisateur
        FOREIGN KEY(id_expediteur)
        REFERENCES utilisateurs(id_utilisateur)
        ON DELETE CASCADE

);
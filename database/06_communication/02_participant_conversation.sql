DROP TABLE IF EXISTS participant_conversation CASCADE;

CREATE TABLE participant_conversation (

    id_conversation BIGINT NOT NULL,

    id_utilisateur BIGINT NOT NULL,

    date_ajout TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY(id_conversation,id_utilisateur),

    CONSTRAINT fk_participant_conversation
        FOREIGN KEY(id_conversation)
        REFERENCES conversation(id_conversation)
        ON DELETE CASCADE,

    CONSTRAINT fk_participant_utilisateur
        FOREIGN KEY(id_utilisateur)
        REFERENCES utilisateurs(id_utilisateur)
        ON DELETE CASCADE

);
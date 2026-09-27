DROP TABLE IF EXISTS notification CASCADE;

CREATE TABLE notification (

    id_notification BIGSERIAL PRIMARY KEY,

    id_utilisateur BIGINT NOT NULL,

    titre VARCHAR(200) NOT NULL,

    message TEXT NOT NULL,

    type type_notification NOT NULL,

    lu BOOLEAN DEFAULT FALSE,

    url_action VARCHAR(500),

    date_lecture TIMESTAMP,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_user
        FOREIGN KEY(id_utilisateur)
        REFERENCES utilisateurs(id_utilisateur)
        ON DELETE CASCADE

);
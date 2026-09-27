DROP TABLE IF EXISTS piece_jointe CASCADE;

CREATE TABLE piece_jointe (

    id_piece_jointe BIGSERIAL PRIMARY KEY,

    id_message BIGINT NOT NULL,

    id_media BIGINT NOT NULL,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_piece_message
        FOREIGN KEY(id_message)
        REFERENCES message(id_message)
        ON DELETE CASCADE,

    CONSTRAINT fk_piece_media
        FOREIGN KEY(id_media)
        REFERENCES media(id_media)
        ON DELETE CASCADE

);
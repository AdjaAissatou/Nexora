DROP TABLE IF EXISTS conversation CASCADE;

CREATE TABLE conversation (

    id_conversation BIGSERIAL PRIMARY KEY,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    actif BOOLEAN DEFAULT TRUE

);
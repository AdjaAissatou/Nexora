DROP TABLE IF EXISTS jour_ferie CASCADE;

CREATE TABLE jour_ferie (

    id_jour_ferie BIGSERIAL PRIMARY KEY,

    pays VARCHAR(100),

    nom VARCHAR(150),

    date_ferie DATE,

    recurrent BOOLEAN DEFAULT TRUE

);
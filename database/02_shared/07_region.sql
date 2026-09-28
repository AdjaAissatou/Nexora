DROP TABLE IF EXISTS region CASCADE;

-- Référence fermée des 14 régions du Sénégal — sélectionnée en cascade avec
-- departement et commune lors de la création/modification d'un espace, pour
-- que la localisation soit toujours choisie et jamais saisie librement.
CREATE TABLE region (

    id_region BIGSERIAL PRIMARY KEY,

    nom VARCHAR(100) NOT NULL UNIQUE,

    ordre_affichage INTEGER DEFAULT 0

);

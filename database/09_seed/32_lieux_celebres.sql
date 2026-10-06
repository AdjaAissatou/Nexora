-- ============================================================================
-- Lieux célèbres et repères stratégiques de Dakar et des environs : sites touristiques,
-- culture, sport, plages, gares du TER. Visibles dans « Lieux et repères » avec l'itinéraire
-- et les commerces autour. Coordonnées approximatives, à affiner si besoin. Rejouable.
-- ============================================================================

SET client_encoding = 'UTF8';

INSERT INTO lieu_public (nom, type_lieu, region, departement, commune, adresse_complete, latitude, longitude, description)
SELECT l.* FROM (VALUES
    ('Île de Gorée', 'TOURISME', 'Dakar', 'Dakar', 'Gorée', 'Île de Gorée (chaloupe depuis le port de Dakar)', 14.6672, -17.3980, 'Île historique classée au patrimoine mondial de l''UNESCO'),
    ('Maison des Esclaves', 'MUSEE', 'Dakar', 'Dakar', 'Gorée', 'Rue Saint-Germain, Gorée', 14.6670, -17.3990, 'Musée et mémorial de la traite négrière'),
    ('Musée des Civilisations noires', 'MUSEE', 'Dakar', 'Dakar', 'Plateau', 'Place de la Gare, Dakar', 14.6847, -17.4316, 'Grand musée consacré aux civilisations africaines'),
    ('Grand Théâtre national', 'CULTURE', 'Dakar', 'Dakar', 'Plateau', 'Avenue Malick Sy, Dakar', 14.6870, -17.4340, 'Salle de spectacles et de concerts'),
    ('Place de l''Obélisque', 'MONUMENT', 'Dakar', 'Dakar', 'Colobane/Fass/Gueule Tapée', 'Boulevard Dial Diop, Dakar', 14.6930, -17.4430, 'Place de la Nation, lieu de rassemblement'),
    ('Place du Souvenir Africain', 'MONUMENT', 'Dakar', 'Dakar', 'Fann-Point E-Amitié', 'Corniche Ouest, Dakar', 14.6950, -17.4770, 'Esplanade sur la Corniche Ouest'),
    ('Corniche Ouest', 'TOURISME', 'Dakar', 'Dakar', 'Fann-Point E-Amitié', 'Corniche Ouest, Dakar', 14.6950, -17.4740, 'Promenade en bord de mer, sport et couchers de soleil'),
    ('Phare des Mamelles', 'TOURISME', 'Dakar', 'Dakar', 'Ouakam', 'Mamelles, Dakar', 14.7230, -17.5000, 'Phare et point de vue sur la presqu''île'),
    ('Mosquée de la Divinité', 'MOSQUEE', 'Dakar', 'Dakar', 'Ouakam', 'Corniche, Ouakam', 14.7210, -17.4970, 'Mosquée en bord de mer à Ouakam'),
    ('Pointe des Almadies', 'TOURISME', 'Dakar', 'Dakar', 'Ngor', 'Almadies, Dakar', 14.7440, -17.5300, 'Point le plus occidental du continent africain'),
    ('Île de Ngor', 'TOURISME', 'Dakar', 'Dakar', 'Ngor', 'Ngor (pirogue depuis la plage)', 14.7570, -17.5160, 'Petite île, plages et surf'),
    ('Plage de Ngor', 'PLAGE', 'Dakar', 'Dakar', 'Ngor', 'Ngor, Dakar', 14.7530, -17.5150, 'Plage populaire face à l''île de Ngor'),
    ('Plage de Yoff', 'PLAGE', 'Dakar', 'Dakar', 'Yoff', 'Yoff, Dakar', 14.7600, -17.4700, 'Grande plage de pêcheurs'),
    ('Plage de l''Anse Bernard', 'PLAGE', 'Dakar', 'Dakar', 'Plateau', 'Plateau, Dakar', 14.6640, -17.4300, 'Plage du centre-ville'),
    ('Village artisanal de Soumbédioune', 'CENTRE_COMMERCIAL', 'Dakar', 'Dakar', 'Médina', 'Corniche, Soumbédioune', 14.6880, -17.4660, 'Artisanat : bijoux, sculptures, maroquinerie, tissus'),
    ('Marché aux poissons de Soumbédioune', 'MARCHE', 'Dakar', 'Dakar', 'Médina', 'Plage de Soumbédioune', 14.6875, -17.4655, 'Retour des pirogues et vente de poisson frais en fin de journée'),
    ('Village des Arts', 'CULTURE', 'Dakar', 'Dakar', 'Grand Yoff', 'Route de l''aéroport, Dakar', 14.7290, -17.4520, 'Ateliers d''artistes et expositions'),
    ('Parc forestier et zoologique de Hann', 'PARC', 'Dakar', 'Dakar', 'Hann Bel-Air', 'Hann, Dakar', 14.7210, -17.4300, 'Parc, zoo et espaces verts'),
    ('Stade Léopold Sédar Senghor', 'STADE', 'Dakar', 'Dakar', 'Grand Yoff', 'Route de l''aéroport, Dakar', 14.7470, -17.4520, 'Grand stade de Dakar'),
    ('Stade Abdoulaye Wade', 'STADE', 'Dakar', 'Rufisque', 'Diamniadio', 'Diamniadio', 14.7330, -17.1860, 'Stade olympique de Diamniadio'),
    ('Dakar Arena', 'STADE', 'Dakar', 'Rufisque', 'Diamniadio', 'Diamniadio', 14.7340, -17.1970, 'Salle omnisports et de spectacles'),
    ('Gare TER de Dakar', 'GARE', 'Dakar', 'Dakar', 'Plateau', 'Plateau, Dakar', 14.6730, -17.4320, 'Train express régional vers Diamniadio et l''AIBD'),
    ('Gare TER de Diamniadio', 'GARE', 'Dakar', 'Rufisque', 'Diamniadio', 'Diamniadio', 14.7290, -17.1870, 'Train express régional'),
    ('Lac Rose', 'TOURISME', 'Dakar', 'Rufisque', 'Sangalkam', 'Lac Retba', 14.8390, -17.2340, 'Lac aux eaux roses, récolte du sel')
) AS l(nom, type_lieu, region, departement, commune, adresse_complete, latitude, longitude, description)
WHERE NOT EXISTS (SELECT 1 FROM lieu_public p WHERE p.nom = l.nom);

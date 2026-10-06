-- ============================================================================
-- Lieux publics : marchés et quartiers de Dakar et de sa banlieue qui manquaient
-- (« colobane » ne trouvait rien). Un quartier sert de repère comme un marché : la
-- recherche affiche les commerces autour, avec l'itinéraire.
-- Coordonnées approximatives (centre du marché ou du quartier), à affiner si besoin.
-- Rejouable : un lieu déjà présent (même nom) n'est pas ajouté deux fois.
-- ============================================================================

SET client_encoding = 'UTF8';

INSERT INTO lieu_public (nom, type_lieu, region, departement, commune, adresse_complete, latitude, longitude, description)
SELECT l.* FROM (VALUES
    -- Marchés
    ('Marché Colobane', 'MARCHE', 'Dakar', 'Dakar', 'Colobane/Fass/Gueule Tapée', 'Colobane, Dakar', 14.6878, -17.4465, 'Grand marché aux vêtements, chaussures, friperie et électronique'),
    ('Marché Gueule Tapée', 'MARCHE', 'Dakar', 'Dakar', 'Colobane/Fass/Gueule Tapée', 'Gueule Tapée, Dakar', 14.6903, -17.4567, 'Marché de quartier, Gueule Tapée'),
    ('Marché Fass', 'MARCHE', 'Dakar', 'Dakar', 'Colobane/Fass/Gueule Tapée', 'Fass, Dakar', 14.6858, -17.4530, 'Marché de quartier, Fass'),
    ('Marché Grand Yoff', 'MARCHE', 'Dakar', 'Dakar', 'Grand Yoff', 'Grand Yoff, Dakar', 14.7347, -17.4596, 'Marché populaire de Grand Yoff'),
    ('Marché Ouakam', 'MARCHE', 'Dakar', 'Dakar', 'Ouakam', 'Ouakam, Dakar', 14.7236, -17.4880, 'Marché de Ouakam'),
    ('Marché Dior', 'MARCHE', 'Dakar', 'Dakar', 'Parcelles Assainies', 'Parcelles Assainies, Dakar', 14.7652, -17.4413, 'Marché des Parcelles Assainies'),
    ('Marché Niary Tally', 'MARCHE', 'Dakar', 'Dakar', 'Biscuiterie', 'Niary Tally, Dakar', 14.7090, -17.4530, 'Marché de Niary Tally'),
    ('Marché Bopp', 'MARCHE', 'Dakar', 'Dakar', 'Biscuiterie', 'Bopp, Dakar', 14.7060, -17.4470, 'Marché de Bopp'),
    ('Marché Yoff', 'MARCHE', 'Dakar', 'Dakar', 'Yoff', 'Yoff, Dakar', 14.7560, -17.4720, 'Marché de Yoff'),
    ('Marché Zinc', 'MARCHE', 'Dakar', 'Pikine', 'Pikine', 'Pikine', 14.7546, -17.3930, 'Grand marché de Pikine'),
    ('Marché Thiaroye', 'MARCHE', 'Dakar', 'Pikine', 'Thiaroye', 'Thiaroye', 14.7533, -17.3800, 'Marché de Thiaroye'),
    ('Marché Boubess', 'MARCHE', 'Dakar', 'Guédiawaye', 'Guédiawaye', 'Guédiawaye', 14.7720, -17.3990, 'Marché de Guédiawaye'),
    ('Marché Keur Massar', 'MARCHE', 'Dakar', 'Keur Massar', 'Keur Massar', 'Keur Massar', 14.7770, -17.3150, 'Marché de Keur Massar'),
    -- Quartiers (repères)
    ('Colobane', 'QUARTIER', 'Dakar', 'Dakar', 'Colobane/Fass/Gueule Tapée', 'Colobane, Dakar', 14.6880, -17.4450, 'Quartier commerçant de Dakar'),
    ('Médina', 'QUARTIER', 'Dakar', 'Dakar', 'Médina', 'Médina, Dakar', 14.6830, -17.4560, 'Quartier de la Médina'),
    ('Plateau', 'QUARTIER', 'Dakar', 'Dakar', 'Plateau', 'Plateau, Dakar', 14.6680, -17.4380, 'Centre-ville de Dakar'),
    ('Almadies', 'QUARTIER', 'Dakar', 'Dakar', 'Ngor', 'Almadies, Dakar', 14.7440, -17.5180, 'Quartier des Almadies'),
    ('Mermoz', 'QUARTIER', 'Dakar', 'Dakar', 'Mermoz-Sacré-Cœur', 'Mermoz, Dakar', 14.7090, -17.4740, 'Quartier de Mermoz'),
    ('Sacré-Cœur', 'QUARTIER', 'Dakar', 'Dakar', 'Mermoz-Sacré-Cœur', 'Sacré-Cœur, Dakar', 14.7210, -17.4630, 'Quartier de Sacré-Cœur'),
    ('Point E', 'QUARTIER', 'Dakar', 'Dakar', 'Fann-Point E-Amitié', 'Point E, Dakar', 14.6940, -17.4660, 'Quartier du Point E'),
    ('Fann', 'QUARTIER', 'Dakar', 'Dakar', 'Fann-Point E-Amitié', 'Fann, Dakar', 14.6930, -17.4690, 'Quartier de Fann'),
    ('Ngor', 'QUARTIER', 'Dakar', 'Dakar', 'Ngor', 'Ngor, Dakar', 14.7520, -17.5130, 'Village et quartier de Ngor'),
    ('Liberté 6', 'QUARTIER', 'Dakar', 'Dakar', 'Sicap Liberté', 'Liberté 6, Dakar', 14.7270, -17.4560, 'Quartier de Liberté 6'),
    ('Sicap Baobab', 'QUARTIER', 'Dakar', 'Dakar', 'Sicap Liberté', 'Sicap Baobab, Dakar', 14.7120, -17.4650, 'Quartier de Sicap Baobab'),
    ('Dieuppeul', 'QUARTIER', 'Dakar', 'Dakar', 'Dieuppeul-Derklé', 'Dieuppeul, Dakar', 14.7140, -17.4570, 'Quartier de Dieuppeul'),
    ('Grand Dakar', 'QUARTIER', 'Dakar', 'Dakar', 'Grand Dakar', 'Grand Dakar', 14.7020, -17.4530, 'Quartier de Grand Dakar'),
    ('Hann', 'QUARTIER', 'Dakar', 'Dakar', 'Hann Bel-Air', 'Hann, Dakar', 14.7170, -17.4290, 'Quartier de Hann'),
    ('Mamelles', 'QUARTIER', 'Dakar', 'Dakar', 'Ouakam', 'Mamelles, Dakar', 14.7300, -17.5050, 'Quartier des Mamelles'),
    ('Ouest Foire', 'QUARTIER', 'Dakar', 'Dakar', 'Yoff', 'Ouest Foire, Dakar', 14.7420, -17.4790, 'Quartier de Ouest Foire'),
    ('Parcelles Assainies', 'QUARTIER', 'Dakar', 'Dakar', 'Parcelles Assainies', 'Parcelles Assainies, Dakar', 14.7660, -17.4380, 'Quartier des Parcelles Assainies'),
    ('Guédiawaye', 'QUARTIER', 'Dakar', 'Guédiawaye', 'Guédiawaye', 'Guédiawaye', 14.7770, -17.3990, 'Ville de Guédiawaye'),
    ('Pikine', 'QUARTIER', 'Dakar', 'Pikine', 'Pikine', 'Pikine', 14.7550, -17.3970, 'Ville de Pikine'),
    ('Sea Plaza', 'CENTRE_COMMERCIAL', 'Dakar', 'Dakar', 'Fann-Point E-Amitié', 'Corniche Ouest, Dakar', 14.6940, -17.4720, 'Centre commercial de la Corniche')
) AS l(nom, type_lieu, region, departement, commune, adresse_complete, latitude, longitude, description)
WHERE NOT EXISTS (SELECT 1 FROM lieu_public p WHERE p.nom = l.nom);

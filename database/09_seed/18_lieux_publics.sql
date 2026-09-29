/**************************************************************************
 * LIEUX PUBLICS DU SENEGAL
 * Repères géographiques (marchés, lieux de culte, hôpitaux, gares, monuments...)
 * utilisés comme résultats de recherche/carte pour aider à se situer.
 * Coordonnées approximatives (échelle du quartier), à affiner si besoin.
 **************************************************************************/

INSERT INTO lieu_public (nom, type_lieu, region, departement, commune, adresse_complete, latitude, longitude, description) VALUES

-- Dakar
('Marché Sandaga', 'MARCHE', 'Dakar', 'Dakar', 'Dakar', 'Avenue Lamine Guèye, Dakar', 14.6708, -17.4359, 'Grand marché populaire au centre-ville de Dakar'),
('Marché Kermel', 'MARCHE', 'Dakar', 'Dakar', 'Dakar', 'Rue Malenfant, Dakar', 14.6656, -17.4304, 'Marché couvert historique, plateau'),
('Marché HLM', 'MARCHE', 'Dakar', 'Dakar', 'Dakar', 'HLM, Dakar', 14.7080, -17.4463, 'Marché réputé pour le tissu et la couture'),
('Marché Castors', 'MARCHE', 'Dakar', 'Dakar', 'Dakar', 'Castors, Dakar', 14.7057, -17.4547, 'Marché de quartier, Castors'),
('Grande Mosquée de Dakar', 'MOSQUEE', 'Dakar', 'Dakar', 'Dakar', 'Boulevard Général de Gaulle, Dakar', 14.6737, -17.4370, 'Principale mosquée de la capitale'),
('Cathédrale du Souvenir Africain', 'EGLISE', 'Dakar', 'Dakar', 'Dakar', 'Plateau, Dakar', 14.6725, -17.4373, 'Cathédrale du Plateau'),
('Place de l''Indépendance', 'MONUMENT', 'Dakar', 'Dakar', 'Dakar', 'Plateau, Dakar', 14.6710, -17.4315, 'Place centrale historique de Dakar'),
('Monument de la Renaissance Africaine', 'MONUMENT', 'Dakar', 'Dakar', 'Dakar', 'Ouakam, Dakar', 14.7245, -17.4835, 'Statue emblématique surplombant Dakar'),
('Gare de Dakar', 'GARE', 'Dakar', 'Dakar', 'Dakar', 'Plateau, Dakar', 14.6733, -17.4373, 'Gare ferroviaire historique'),
('Gare routière Pompiers (Petersen)', 'GARE_ROUTIERE', 'Dakar', 'Dakar', 'Dakar', 'Petersen, Dakar', 14.6825, -17.4423, 'Principale gare routière vers l''intérieur du pays'),
('Aéroport International Blaise Diagne', 'AEROPORT', 'Thiès', 'Thiès', 'Diass', 'Diass', 14.6704, -17.0733, 'Aéroport international du Sénégal'),
('Université Cheikh Anta Diop', 'UNIVERSITE', 'Dakar', 'Dakar', 'Dakar', 'Fann, Dakar', 14.6928, -17.4632, 'Principale université publique du pays'),
('Hôpital Aristide Le Dantec', 'HOPITAL', 'Dakar', 'Dakar', 'Dakar', 'Plateau, Dakar', 14.6668, -17.4368, 'Hôpital universitaire'),
('Hôpital Principal de Dakar', 'HOPITAL', 'Dakar', 'Dakar', 'Dakar', 'Plateau, Dakar', 14.6749, -17.4341, 'Hôpital militaire et civil'),
('CHU de Fann', 'HOPITAL', 'Dakar', 'Dakar', 'Dakar', 'Fann, Dakar', 14.6907, -17.4589, 'Centre hospitalier universitaire de Fann'),
('CHU Abass Ndao', 'HOPITAL', 'Dakar', 'Dakar', 'Dakar', 'Médina, Dakar', 14.6889, -17.4459, 'Centre hospitalier universitaire'),
('Stade Léopold Sédar Senghor', 'STADE', 'Dakar', 'Dakar', 'Dakar', 'Mermoz, Dakar', 14.7247, -17.4402, 'Principal stade national'),
('Île de Gorée', 'MONUMENT', 'Dakar', 'Dakar', 'Dakar', 'Gorée, Dakar', 14.6699, -17.3984, 'Île historique classée au patrimoine mondial'),
('Plage de Ngor', 'PLAGE', 'Dakar', 'Dakar', 'Dakar', 'Ngor, Dakar', 14.7489, -17.5133, 'Plage populaire des Almadies'),
('CICES', 'ADMINISTRATION', 'Dakar', 'Dakar', 'Dakar', 'VDN, Dakar', 14.7375, -17.4453, 'Centre international du commerce extérieur du Sénégal, foires et salons'),

-- Thiès
('Gare de Thiès', 'GARE', 'Thiès', 'Thiès', 'Thiès', 'Centre-ville, Thiès', 14.7889, -16.9256, 'Ancienne gare ferroviaire de Thiès'),
('Marché Central de Thiès', 'MARCHE', 'Thiès', 'Thiès', 'Thiès', 'Centre-ville, Thiès', 14.7897, -16.9264, 'Principal marché de Thiès'),
('Hôpital régional de Thiès', 'HOPITAL', 'Thiès', 'Thiès', 'Thiès', 'Thiès', 14.7940, -16.9280, 'Hôpital régional'),

-- Saint-Louis
('Pont Faidherbe', 'MONUMENT', 'Saint-Louis', 'Saint-Louis', 'Saint-Louis', 'Saint-Louis', 16.0192, -16.4934, 'Pont historique reliant l''île à la terre ferme'),
('Cathédrale de Saint-Louis', 'EGLISE', 'Saint-Louis', 'Saint-Louis', 'Saint-Louis', 'Île de Saint-Louis', 16.0198, -16.4919, 'Cathédrale du centre historique'),
('Marché de Sor', 'MARCHE', 'Saint-Louis', 'Saint-Louis', 'Saint-Louis', 'Sor, Saint-Louis', 16.0247, -16.4831, 'Grand marché de Saint-Louis'),
('Hôpital régional de Saint-Louis', 'HOPITAL', 'Saint-Louis', 'Saint-Louis', 'Saint-Louis', 'Saint-Louis', 16.0158, -16.4901, 'Hôpital régional'),
('Université Gaston Berger', 'UNIVERSITE', 'Saint-Louis', 'Saint-Louis', 'Saint-Louis', 'Sanar, Saint-Louis', 16.0396, -16.4547, 'Université publique de Saint-Louis'),

-- Touba
('Grande Mosquée de Touba', 'MOSQUEE', 'Diourbel', 'Mbacké', 'Touba', 'Touba', 14.8586, -15.8794, 'Principale mosquée du mouridisme'),
('Marché Ocass', 'MARCHE', 'Diourbel', 'Mbacké', 'Touba', 'Touba', 14.8611, -15.8794, 'Grand marché de Touba'),

-- Kaolack
('Marché Central de Kaolack', 'MARCHE', 'Kaolack', 'Kaolack', 'Kaolack', 'Kaolack', 14.1500, -16.0764, 'Principal marché de Kaolack'),
('Hôpital régional de Kaolack', 'HOPITAL', 'Kaolack', 'Kaolack', 'Kaolack', 'Kaolack', 14.1450, -16.0700, 'Hôpital régional'),

-- Ziguinchor
('Marché Saint-Maur', 'MARCHE', 'Ziguinchor', 'Ziguinchor', 'Ziguinchor', 'Ziguinchor', 12.5822, -16.2733, 'Principal marché de Ziguinchor'),
('Hôpital régional de Ziguinchor', 'HOPITAL', 'Ziguinchor', 'Ziguinchor', 'Ziguinchor', 'Ziguinchor', 12.5680, -16.2700, 'Hôpital régional'),

-- Mbour / Saly
('Marché Central de Mbour', 'MARCHE', 'Thiès', 'Mbour', 'Mbour', 'Mbour', 14.4198, -16.9646, 'Marché principal de Mbour'),
('Plage de Saly', 'PLAGE', 'Thiès', 'Mbour', 'Saly Portudal', 'Saly', 14.4536, -17.0083, 'Station balnéaire touristique'),

-- Rufisque
('Marché de Rufisque', 'MARCHE', 'Dakar', 'Rufisque', 'Rufisque', 'Rufisque', 14.7167, -17.2667, 'Marché historique de Rufisque'),

-- Diourbel
('Hôpital régional de Diourbel', 'HOPITAL', 'Diourbel', 'Diourbel', 'Diourbel', 'Diourbel', 14.6572, -16.2317, 'Hôpital régional'),

-- Louga
('Marché Central de Louga', 'MARCHE', 'Louga', 'Louga', 'Louga', 'Louga', 15.6144, -16.2258, 'Marché principal de Louga'),

-- Tambacounda
('Hôpital régional de Tambacounda', 'HOPITAL', 'Tambacounda', 'Tambacounda', 'Tambacounda', 'Tambacounda', 13.7708, -13.6672, 'Hôpital régional'),

-- Kolda
('Marché Central de Kolda', 'MARCHE', 'Kolda', 'Kolda', 'Kolda', 'Kolda', 12.8939, -14.9408, 'Marché principal de Kolda');

/**************************************************************************
 * LIEUX PUBLICS DU SENEGAL — EXTENSION
 * Complète 18_lieux_publics.sql : plus de repères à Dakar (administrations,
 * banques, pharmacies, port) et couverture des régions restantes
 * (Fatick, Kaffrine, Kédougou, Sédhiou, Matam) + plus de villes.
 * Coordonnées approximatives (échelle du quartier), à affiner si besoin.
 **************************************************************************/

INSERT INTO lieu_public (nom, type_lieu, region, departement, commune, adresse_complete, latitude, longitude, description) VALUES

-- Dakar — compléments
('Palais de la République', 'ADMINISTRATION', 'Dakar', 'Dakar', 'Dakar', 'Plateau, Dakar', 14.6672, -17.4275, 'Palais présidentiel'),
('Assemblée nationale', 'ADMINISTRATION', 'Dakar', 'Dakar', 'Dakar', 'Plateau, Dakar', 14.6721, -17.4335, 'Siège du parlement sénégalais'),
('Mairie de Dakar', 'ADMINISTRATION', 'Dakar', 'Dakar', 'Dakar', 'Plateau, Dakar', 14.6714, -17.4293, 'Hôtel de ville de Dakar'),
('Grande Poste de Dakar', 'ADMINISTRATION', 'Dakar', 'Dakar', 'Dakar', 'Plateau, Dakar', 14.6716, -17.4325, 'Bureau de poste central historique'),
('BCEAO Dakar', 'BANQUE', 'Dakar', 'Dakar', 'Dakar', 'Plateau, Dakar', 14.6714, -17.4323, 'Banque centrale des États de l''Afrique de l''Ouest'),
('Port Autonome de Dakar', 'PORT', 'Dakar', 'Dakar', 'Dakar', 'Plateau, Dakar', 14.6656, -17.4227, 'Principal port du Sénégal'),
('Pharmacie Guigon', 'PHARMACIE', 'Dakar', 'Dakar', 'Dakar', 'Plateau, Dakar', 14.6720, -17.4351, 'Pharmacie de garde historique du Plateau'),
('Marché de Pikine', 'MARCHE', 'Dakar', 'Pikine', 'Pikine', 'Pikine', 14.7549, -17.3900, 'Grand marché de Pikine'),
('Hôpital de Pikine', 'HOPITAL', 'Dakar', 'Pikine', 'Pikine', 'Pikine', 14.7539, -17.3925, 'Hôpital de la banlieue dakaroise'),
('Marché de Guédiawaye', 'MARCHE', 'Dakar', 'Guédiawaye', 'Guédiawaye', 'Guédiawaye', 14.7692, -17.4103, 'Marché de Guédiawaye'),

-- Thiès — compléments
('Grande Mosquée de Thiès', 'MOSQUEE', 'Thiès', 'Thiès', 'Thiès', 'Thiès', 14.7910, -16.9270, 'Principale mosquée de Thiès'),
('Pharmacie de Thiès Centre', 'PHARMACIE', 'Thiès', 'Thiès', 'Thiès', 'Centre-ville, Thiès', 14.7895, -16.9250, 'Pharmacie du centre-ville'),

-- Saint-Louis — compléments
('Grande Mosquée de Saint-Louis', 'MOSQUEE', 'Saint-Louis', 'Saint-Louis', 'Saint-Louis', 'Île de Saint-Louis', 16.0210, -16.4900, 'Mosquée du centre historique'),
('Gouvernance de Saint-Louis', 'ADMINISTRATION', 'Saint-Louis', 'Saint-Louis', 'Saint-Louis', 'Île de Saint-Louis', 16.0225, -16.4915, 'Ancien palais du gouverneur'),

-- Touba — compléments
('Hôpital Matlaboul Fawzaini', 'HOPITAL', 'Diourbel', 'Mbacké', 'Touba', 'Touba', 14.8650, -15.8700, 'Hôpital de Touba'),
('Gare routière de Touba', 'GARE_ROUTIERE', 'Diourbel', 'Mbacké', 'Touba', 'Touba', 14.8570, -15.8850, 'Gare routière principale de Touba'),

-- Kaolack — compléments
('Mosquée Médina Baye', 'MOSQUEE', 'Kaolack', 'Kaolack', 'Kaolack', 'Médina Baye, Kaolack', 14.1350, -16.0850, 'Mosquée du foyer religieux de Médina Baye'),
('Gare routière de Kaolack', 'GARE_ROUTIERE', 'Kaolack', 'Kaolack', 'Kaolack', 'Kaolack', 14.1520, -16.0730, 'Gare routière principale de Kaolack'),

-- Ziguinchor — compléments
('Marché Tilène', 'MARCHE', 'Ziguinchor', 'Ziguinchor', 'Ziguinchor', 'Ziguinchor', 12.5680, -16.2650, 'Marché historique de Ziguinchor'),
('Aéroport de Ziguinchor', 'AEROPORT', 'Ziguinchor', 'Ziguinchor', 'Ziguinchor', 'Ziguinchor', 12.5556, -16.2819, 'Aéroport régional de Ziguinchor'),
('Cathédrale de Ziguinchor', 'EGLISE', 'Ziguinchor', 'Ziguinchor', 'Ziguinchor', 'Ziguinchor', 12.5850, -16.2700, 'Cathédrale de Ziguinchor'),

-- Mbour / Rufisque / Diourbel / Louga / Tambacounda / Kolda — compléments
('Hôpital de Mbour', 'HOPITAL', 'Thiès', 'Mbour', 'Mbour', 'Mbour', 14.4210, -16.9660, 'Hôpital de Mbour'),
('Hôpital de Rufisque', 'HOPITAL', 'Dakar', 'Rufisque', 'Rufisque', 'Rufisque', 14.7180, -17.2700, 'Hôpital Youssou Mbargane Diop'),
('Marché central de Diourbel', 'MARCHE', 'Diourbel', 'Diourbel', 'Diourbel', 'Diourbel', 14.6580, -16.2330, 'Marché principal de Diourbel'),
('Hôpital régional de Louga', 'HOPITAL', 'Louga', 'Louga', 'Louga', 'Louga', 15.6150, -16.2270, 'Hôpital régional'),
('Marché central de Tambacounda', 'MARCHE', 'Tambacounda', 'Tambacounda', 'Tambacounda', 'Tambacounda', 13.7700, -13.6680, 'Marché principal de Tambacounda'),
('Aéroport de Tambacounda', 'AEROPORT', 'Tambacounda', 'Tambacounda', 'Tambacounda', 'Tambacounda', 13.7677, -13.6511, 'Aéroport régional'),
('Hôpital régional de Kolda', 'HOPITAL', 'Kolda', 'Kolda', 'Kolda', 'Kolda', 12.8950, -14.9420, 'Hôpital régional'),

-- Fatick
('Marché central de Fatick', 'MARCHE', 'Fatick', 'Fatick', 'Fatick', 'Fatick', 14.3390, -16.4110, 'Marché principal de Fatick'),
('Hôpital régional de Fatick', 'HOPITAL', 'Fatick', 'Fatick', 'Fatick', 'Fatick', 14.3400, -16.4090, 'Hôpital régional'),

-- Kaffrine
('Marché central de Kaffrine', 'MARCHE', 'Kaffrine', 'Kaffrine', 'Kaffrine', 'Kaffrine', 14.1059, -15.5508, 'Marché principal de Kaffrine'),
('Hôpital régional de Kaffrine', 'HOPITAL', 'Kaffrine', 'Kaffrine', 'Kaffrine', 'Kaffrine', 14.1070, -15.5490, 'Hôpital régional'),

-- Kédougou
('Marché central de Kédougou', 'MARCHE', 'Kédougou', 'Kédougou', 'Kédougou', 'Kédougou', 12.5556, -12.1743, 'Marché principal de Kédougou'),
('Hôpital régional de Kédougou', 'HOPITAL', 'Kédougou', 'Kédougou', 'Kédougou', 'Kédougou', 12.5570, -12.1730, 'Hôpital régional'),

-- Sédhiou
('Marché central de Sédhiou', 'MARCHE', 'Sédhiou', 'Sédhiou', 'Sédhiou', 'Sédhiou', 12.7081, -15.5569, 'Marché principal de Sédhiou'),
('Hôpital régional de Sédhiou', 'HOPITAL', 'Sédhiou', 'Sédhiou', 'Sédhiou', 'Sédhiou', 12.7090, -15.5550, 'Hôpital régional'),

-- Matam
('Marché central de Matam', 'MARCHE', 'Matam', 'Matam', 'Matam', 'Matam', 15.6559, -13.2548, 'Marché principal de Matam'),
('Hôpital régional de Matam', 'HOPITAL', 'Matam', 'Matam', 'Matam', 'Matam', 15.6570, -13.2530, 'Hôpital régional');

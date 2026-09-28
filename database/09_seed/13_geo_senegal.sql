-- Référence géographique du Sénégal (14 régions, 46 départements, ~550 communes)
-- Source : subdivisions administratives officielles (réforme de l'Acte III de la
-- décentralisation, 2013 ; création du département de Keur Massar, 2021), compilées
-- depuis Wikipédia. Sert la sélection en cascade région > département > commune lors
-- de la création/modification d'un espace professionnel — jamais de saisie libre pour
-- ces trois niveaux. Idempotent.

INSERT INTO region (nom, ordre_affichage) VALUES
    ('Dakar', 1),
    ('Diourbel', 2),
    ('Fatick', 3),
    ('Kaffrine', 4),
    ('Kaolack', 5),
    ('Kédougou', 6),
    ('Kolda', 7),
    ('Louga', 8),
    ('Matam', 9),
    ('Saint-Louis', 10),
    ('Sédhiou', 11),
    ('Tambacounda', 12),
    ('Thiès', 13),
    ('Ziguinchor', 14)
ON CONFLICT (nom) DO NOTHING;

INSERT INTO departement (id_region, nom)
SELECT id_region, 'Dakar' FROM region WHERE nom = 'Dakar'
UNION ALL
SELECT id_region, 'Guédiawaye' FROM region WHERE nom = 'Dakar'
UNION ALL
SELECT id_region, 'Pikine' FROM region WHERE nom = 'Dakar'
UNION ALL
SELECT id_region, 'Rufisque' FROM region WHERE nom = 'Dakar'
UNION ALL
SELECT id_region, 'Keur Massar' FROM region WHERE nom = 'Dakar'
UNION ALL
SELECT id_region, 'Bambey' FROM region WHERE nom = 'Diourbel'
UNION ALL
SELECT id_region, 'Diourbel' FROM region WHERE nom = 'Diourbel'
UNION ALL
SELECT id_region, 'Mbacké' FROM region WHERE nom = 'Diourbel'
UNION ALL
SELECT id_region, 'Fatick' FROM region WHERE nom = 'Fatick'
UNION ALL
SELECT id_region, 'Foundiougne' FROM region WHERE nom = 'Fatick'
UNION ALL
SELECT id_region, 'Gossas' FROM region WHERE nom = 'Fatick'
UNION ALL
SELECT id_region, 'Birkelane' FROM region WHERE nom = 'Kaffrine'
UNION ALL
SELECT id_region, 'Kaffrine' FROM region WHERE nom = 'Kaffrine'
UNION ALL
SELECT id_region, 'Koungheul' FROM region WHERE nom = 'Kaffrine'
UNION ALL
SELECT id_region, 'Malem Hodar' FROM region WHERE nom = 'Kaffrine'
UNION ALL
SELECT id_region, 'Guinguinéo' FROM region WHERE nom = 'Kaolack'
UNION ALL
SELECT id_region, 'Kaolack' FROM region WHERE nom = 'Kaolack'
UNION ALL
SELECT id_region, 'Nioro du Rip' FROM region WHERE nom = 'Kaolack'
UNION ALL
SELECT id_region, 'Kédougou' FROM region WHERE nom = 'Kédougou'
UNION ALL
SELECT id_region, 'Salémata' FROM region WHERE nom = 'Kédougou'
UNION ALL
SELECT id_region, 'Saraya' FROM region WHERE nom = 'Kédougou'
UNION ALL
SELECT id_region, 'Kolda' FROM region WHERE nom = 'Kolda'
UNION ALL
SELECT id_region, 'Médina Yoro Foulah' FROM region WHERE nom = 'Kolda'
UNION ALL
SELECT id_region, 'Vélingara' FROM region WHERE nom = 'Kolda'
UNION ALL
SELECT id_region, 'Kébémer' FROM region WHERE nom = 'Louga'
UNION ALL
SELECT id_region, 'Linguère' FROM region WHERE nom = 'Louga'
UNION ALL
SELECT id_region, 'Louga' FROM region WHERE nom = 'Louga'
UNION ALL
SELECT id_region, 'Kanel' FROM region WHERE nom = 'Matam'
UNION ALL
SELECT id_region, 'Matam' FROM region WHERE nom = 'Matam'
UNION ALL
SELECT id_region, 'Ranérou Ferlo' FROM region WHERE nom = 'Matam'
UNION ALL
SELECT id_region, 'Dagana' FROM region WHERE nom = 'Saint-Louis'
UNION ALL
SELECT id_region, 'Podor' FROM region WHERE nom = 'Saint-Louis'
UNION ALL
SELECT id_region, 'Saint-Louis' FROM region WHERE nom = 'Saint-Louis'
UNION ALL
SELECT id_region, 'Bounkiling' FROM region WHERE nom = 'Sédhiou'
UNION ALL
SELECT id_region, 'Goudomp' FROM region WHERE nom = 'Sédhiou'
UNION ALL
SELECT id_region, 'Sédhiou' FROM region WHERE nom = 'Sédhiou'
UNION ALL
SELECT id_region, 'Bakel' FROM region WHERE nom = 'Tambacounda'
UNION ALL
SELECT id_region, 'Goudiry' FROM region WHERE nom = 'Tambacounda'
UNION ALL
SELECT id_region, 'Koumpentoum' FROM region WHERE nom = 'Tambacounda'
UNION ALL
SELECT id_region, 'Tambacounda' FROM region WHERE nom = 'Tambacounda'
UNION ALL
SELECT id_region, 'M''bour' FROM region WHERE nom = 'Thiès'
UNION ALL
SELECT id_region, 'Thiès' FROM region WHERE nom = 'Thiès'
UNION ALL
SELECT id_region, 'Tivaouane' FROM region WHERE nom = 'Thiès'
UNION ALL
SELECT id_region, 'Bignona' FROM region WHERE nom = 'Ziguinchor'
UNION ALL
SELECT id_region, 'Oussouye' FROM region WHERE nom = 'Ziguinchor'
UNION ALL
SELECT id_region, 'Ziguinchor' FROM region WHERE nom = 'Ziguinchor'
ON CONFLICT (id_region, nom) DO NOTHING;

INSERT INTO commune (id_departement, nom)
SELECT d.id_departement, 'Biscuiterie' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Cambérène' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Colobane/Fass/Gueule Tapée' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Dakar-Plateau' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Dieuppeul-Derklé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Fann-Point E-Amitié' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Gorée' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Grand Dakar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Grand Yoff' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Hann Bel-Air' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'HLM' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Médina' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Mermoz-Sacré-Cœur' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Ngor' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Ouakam' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Parcelles Assainies' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Patte d''Oie' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Sicap-Liberté' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Yoff' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dakar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Golf Sud' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Guédiawaye' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Médina Gounass' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Guédiawaye' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Ndiarème Limamoulaye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Guédiawaye' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Sam Notaire' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Guédiawaye' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Wakhinane Nimzatt' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Guédiawaye' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Dalifort' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Pikine' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Diacksao' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Pikine' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Diamaguène/Sicap Mbao' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Pikine' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Djidah Thiaroye Kaw' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Pikine' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Guinaw Rail Nord' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Pikine' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Guinaw Rail Sud' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Pikine' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Mbao' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Pikine' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Pikine Est' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Pikine' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Pikine Ouest' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Pikine' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Pikine Sud' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Pikine' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Thiaroye-Gare' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Pikine' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Thiaroye-sur-Mer' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Pikine' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Bambylor' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Rufisque' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Bargny' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Rufisque' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Diamniadio' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Rufisque' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Rufisque Est' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Rufisque' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Rufisque Nord' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Rufisque' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Rufisque Ouest' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Rufisque' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Sangalkam' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Rufisque' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Sébikhotane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Rufisque' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Sendou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Rufisque' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Tivaouane Peulh-Niaga' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Rufisque' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Yène' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Rufisque' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Jaxaay-Parcelles' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Keur Massar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Keur Massar Nord' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Keur Massar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Keur Massar Sud' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Keur Massar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Malika' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Keur Massar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Yeumbeul Nord' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Keur Massar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Yeumbeul Sud' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Keur Massar' AND r.nom = 'Dakar'
UNION ALL
SELECT d.id_departement, 'Baba Garage' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bambey' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Bambey' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bambey' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Dinguiraye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bambey' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Gawane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bambey' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Keur Samba Kane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bambey' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Lambaye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bambey' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Ndangalma' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bambey' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Ndondol' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bambey' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Ngogom' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bambey' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Ngoye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bambey' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Réfane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bambey' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Thiakhar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bambey' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Diourbel' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Diourbel' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Gade Escale' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Diourbel' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Keur Ngalgou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Diourbel' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Dankh Sène' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Diourbel' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Ndindy' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Diourbel' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Ndoulo' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Diourbel' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Ngohé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Diourbel' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Patar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Diourbel' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Taïba Moutoupha' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Diourbel' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Tocky-Gare' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Diourbel' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Touba Lappé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Diourbel' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Touré Mbonde' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Diourbel' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Dalla Ngabou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Mbacké' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Darou Nahim' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Mbacké' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Darou Salam Typ' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Mbacké' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Dendey Gouyegui' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Mbacké' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Kael' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Mbacké' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Madina' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Mbacké' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Mbacké' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Mbacké' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Missirah' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Mbacké' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Ndioumane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Mbacké' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Nghaye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Mbacké' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Sadio' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Mbacké' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Taïba Thiékène' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Mbacké' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Taïf' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Mbacké' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Touba Fall' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Mbacké' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Touba Mboul' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Mbacké' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Touba Mosquée' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Mbacké' AND r.nom = 'Diourbel'
UNION ALL
SELECT d.id_departement, 'Diakhao' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Fatick' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Diaoulé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Fatick' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Diarrère' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Fatick' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Diofior' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Fatick' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Diouroup' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Fatick' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Djilasse' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Fatick' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Fatick' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Fatick' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Fimela' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Fatick' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Loul Séssène' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Fatick' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Mbéllacadiao' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Fatick' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Ndiob' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Fatick' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Ngayokhème' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Fatick' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Niakhar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Fatick' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Palmarin' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Fatick' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Patar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Fatick' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Tattaguine' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Fatick' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Thiaré Ndialgui' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Fatick' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Bassoul' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Foundiougne' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Diagane Barka' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Foundiougne' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Dionewar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Foundiougne' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Diossong' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Foundiougne' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Djilor' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Foundiougne' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Djirnda' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Foundiougne' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Foundiougne' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Foundiougne' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Karang Poste' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Foundiougne' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Keur Saloum Diané' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Foundiougne' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Keur Samba Guèye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Foundiougne' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Mbam' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Foundiougne' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Niassène' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Foundiougne' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Nioro Alassane Tall' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Foundiougne' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Passy' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Foundiougne' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Sokone' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Foundiougne' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Soum' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Foundiougne' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Toubacouta' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Foundiougne' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Colobane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Gossas' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Gossas' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Gossas' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Mbar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Gossas' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Ndiène Lagane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Gossas' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Ouadiour' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Gossas' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Patar Lia' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Gossas' AND r.nom = 'Fatick'
UNION ALL
SELECT d.id_departement, 'Birkilane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Birkelane' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Diamal' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Birkelane' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Keur Mboucki' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Birkelane' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Mabo' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Birkelane' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Mbeuleup' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Birkelane' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Ndiognick' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Birkelane' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Ségré Gatta' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Birkelane' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Touba Mbella' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Birkelane' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Boulel' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaffrine' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Diamagadio' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaffrine' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Diokoul Mbelbouck' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaffrine' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Gniby' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaffrine' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Kaffrine' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaffrine' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Kahi' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaffrine' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Kathiotte' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaffrine' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Médinatoul Salam II' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaffrine' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Nganda' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaffrine' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Fass Thiékène' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koungheul' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Ida Mouride' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koungheul' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Koungheul' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koungheul' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Lour Escale' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koungheul' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Maka Yop' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koungheul' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Missirah Wadène' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koungheul' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Gainte Pathé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koungheul' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Ribot Escale' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koungheul' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Saly Escale' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koungheul' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Darou Minam 2' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Malem Hodar' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Dianké Souf' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Malem Hodar' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Khelcom' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Malem Hodar' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Malem-Hodar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Malem Hodar' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Ndiobène Samba Lamo' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Malem Hodar' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Ndioum Ngainthe' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Malem Hodar' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Sagna' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Malem Hodar' AND r.nom = 'Kaffrine'
UNION ALL
SELECT d.id_departement, 'Dara Mboss' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Guinguinéo' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Fass' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Guinguinéo' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Gagnick' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Guinguinéo' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Guinguinéo' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Guinguinéo' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Khelcom Birane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Guinguinéo' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Mbadakhoune' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Guinguinéo' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Mboss' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Guinguinéo' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Ndiago' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Guinguinéo' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Ngathe Naoudé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Guinguinéo' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Nguélou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Guinguinéo' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Ourour' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Guinguinéo' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Panal Wolof' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Guinguinéo' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Dya' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaolack' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Gandiaye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaolack' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Kahone' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaolack' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Kaolack' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaolack' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Keur Baka' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaolack' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Latmingué' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaolack' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Ndiaffate' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaolack' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Ndiébel' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaolack' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Ndiédieng' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaolack' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Ndofane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaolack' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Sibassor' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaolack' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Thiaré' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaolack' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Thiomby' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kaolack' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Dabaly' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Nioro du Rip' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Darou Salam' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Nioro du Rip' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Gainthe Kaye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Nioro du Rip' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Kayemor' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Nioro du Rip' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Keur Maba Diakhou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Nioro du Rip' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Keur Madiabel' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Nioro du Rip' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Keur Madongo' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Nioro du Rip' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Keur Socé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Nioro du Rip' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Médina Sabakh' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Nioro du Rip' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Ndramé Escale' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Nioro du Rip' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Ngayène' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Nioro du Rip' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Nioro du Rip' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Nioro du Rip' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Paoskoto' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Nioro du Rip' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Porokhane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Nioro du Rip' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Taïba Niassène' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Nioro du Rip' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Wack Ngouna' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Nioro du Rip' AND r.nom = 'Kaolack'
UNION ALL
SELECT d.id_departement, 'Bandafassi' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kédougou' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Dimboli' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kédougou' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Dindefelo' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kédougou' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Fongolimbi' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kédougou' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Kédougou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kédougou' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Ninéfécha' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kédougou' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Tomboroncoto' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kédougou' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Dar Salam' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Salémata' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Ethiolo' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Salémata' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Oubadji' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Salémata' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Salémata' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Salémata' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Bembou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Saraya' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Dakateli' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Saraya' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Kévoye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Saraya' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Khossanto' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Saraya' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Médina Baffé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Saraya' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Missirah Sirimana' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Saraya' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Sabodala' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Saraya' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Saraya' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Saraya' AND r.nom = 'Kédougou'
UNION ALL
SELECT d.id_departement, 'Bagadadji' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kolda' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Coumbacara' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kolda' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Dabo' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kolda' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Dialambéré' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kolda' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Dioulacolon' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kolda' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Guiro Yéro Bocar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kolda' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Kolda' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kolda' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Mampatim' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kolda' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Médina Chérif' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kolda' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Médina El Hadj' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kolda' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Salikégné' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kolda' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Saré Bidji' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kolda' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Saré Yoba Diéga' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kolda' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Tankanto Escale' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kolda' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Thiétty' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kolda' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Bignarabé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Médina Yoro Foulah' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Bourouco' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Médina Yoro Foulah' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Dinguiraye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Médina Yoro Foulah' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Kéréwane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Médina Yoro Foulah' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Koulinto' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Médina Yoro Foulah' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Médina Yoro Foulah' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Médina Yoro Foulah' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Ndorna' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Médina Yoro Foulah' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Niaming' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Médina Yoro Foulah' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Pata' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Médina Yoro Foulah' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Badion' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Vélingara' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Bonconto' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Vélingara' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Diaobé-Kabendou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Vélingara' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Fafacourou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Vélingara' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Kandia' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Vélingara' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Kandiaye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Vélingara' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Kounkané' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Vélingara' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Linkéring' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Vélingara' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Médina Gounass' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Vélingara' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Némataba' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Vélingara' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Ouassadou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Vélingara' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Pakour' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Vélingara' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Paroumba' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Vélingara' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Saré Coly Sallé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Vélingara' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Sinthiang Koundara' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Vélingara' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Vélingara' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Vélingara' AND r.nom = 'Kolda'
UNION ALL
SELECT d.id_departement, 'Bandegne Ouolof' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Darou Marnane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Darou Mousty' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Diokoul Diawrigne' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Guéoul' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Kab Gaye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Kanène Ndiob' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Kébémer' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Loro' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Mbacké Cajor' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Mbadiane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Ndande' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Ndoyene' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Ngourane Ouolof' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Sagatta Gueth' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Sam Yabal' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Thieppe' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Thiolom Fall' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Touba Mérina' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kébémer' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Affé Djoloff' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Barkédji' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Boulal' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Dahra' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Dealy' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Dodji' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Gassane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Kamb' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Labgar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Linguère' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Mbeuleukhé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Mboula' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Ouarkhokh' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Sagatta Djolof' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Téssékéré Forage' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Thiamène Djolof' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Thiarny' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Thiel' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Yang-Yang' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Linguère' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Coki' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Louga' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Gande' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Louga' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Guet Ardo' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Louga' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Kéle Gueye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Louga' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Keur Momar Sarr' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Louga' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Léona' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Louga' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Louga' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Louga' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Mbédiène' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Louga' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Ndiagne' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Louga' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Nguer Malal' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Louga' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Ngueune Sarr' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Louga' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Nguidilé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Louga' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Niomré' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Louga' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Pété Ouarack' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Louga' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Sakal' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Louga' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Syer' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Louga' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Thiamène Cayor' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Louga' AND r.nom = 'Louga'
UNION ALL
SELECT d.id_departement, 'Aouré' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kanel' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Bokiladji' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kanel' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Dembancané' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kanel' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Hamady Ounaré' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kanel' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Kanel' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kanel' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Ndendory' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kanel' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Odobéré' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kanel' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Orkadiere' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kanel' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Semmé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kanel' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Sinthiou Bamambé-Banadji' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kanel' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Waoundé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kanel' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Wouro Sidy' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Kanel' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Agnams' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Matam' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Bokidiawé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Matam' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Dabia' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Matam' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Matam' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Matam' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Nabadji Civol' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Matam' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Nguidjilone' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Matam' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Ogo' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Matam' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Oréfondé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Matam' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Ourossogui' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Matam' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Thilogne' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Matam' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Lougré Thioly' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Ranérou Ferlo' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Oudalaye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Ranérou Ferlo' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Ranérou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Ranérou Ferlo' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Vélingara' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Ranérou Ferlo' AND r.nom = 'Matam'
UNION ALL
SELECT d.id_departement, 'Bokhol' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dagana' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Dagana' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dagana' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Diama' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dagana' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Gaé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dagana' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Mbane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dagana' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Ndombo Sandjiry' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dagana' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Ngnith' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dagana' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Richard-Toll' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dagana' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Ronkh' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dagana' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Ross Béthio' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dagana' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Rosso' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Dagana' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Aéré Lao' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Bodé Lao' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Boké Dialloubé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Démette' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Dodel' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Doumga Lao' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Fanaye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Galoya Toucouleur' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Gamadji Saré' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Golléré' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Guédé Chantier' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Guédé Village' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Madina Diathbé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Mbolo Birane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Mboumba' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Méry' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Ndiandane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Ndiayène Peindao' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Ndioum' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Pété' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Podor' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Walaldé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Podor' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Fass Ngom' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Saint-Louis' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Gandon' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Saint-Louis' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Mpal' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Saint-Louis' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Ndiébène Gandiole' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Saint-Louis' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Saint-Louis' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Saint-Louis' AND r.nom = 'Saint-Louis'
UNION ALL
SELECT d.id_departement, 'Bona' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bounkiling' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Bounkiling' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bounkiling' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Diacounda' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bounkiling' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Diambati' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bounkiling' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Diaroumé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bounkiling' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Djinany' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bounkiling' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Faoune' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bounkiling' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Inor' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bounkiling' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Kandion Mangana' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bounkiling' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Madina Wandifa' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bounkiling' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Ndiamacouta' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bounkiling' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Ndiamalathiel' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bounkiling' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Tankon' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bounkiling' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Baghère' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudomp' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Diattacounda' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudomp' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Diouboudou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudomp' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Djibanar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudomp' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Goudomp' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudomp' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Kaour' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudomp' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Karantaba' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudomp' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Kolibantang' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudomp' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Mangaroungou Santo' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudomp' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Niagha' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudomp' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Samine' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudomp' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Simbandi Balante' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudomp' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Simbandi Brassou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudomp' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Tanaff' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudomp' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Yarang Balante' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudomp' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Bambaly' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Sédhiou' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Bémet Bidjini' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Sédhiou' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Boghall' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Sédhiou' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Diannah Ba' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Sédhiou' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Diannah Malary' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Sédhiou' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Diendé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Sédhiou' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Djibabouya' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Sédhiou' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Djiredji' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Sédhiou' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Koussy' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Sédhiou' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Marsassoum' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Sédhiou' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Oudoucar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Sédhiou' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Sakar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Sédhiou' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Sama Kanta Peulh' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Sédhiou' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Sansamba' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Sédhiou' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Sédhiou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Sédhiou' AND r.nom = 'Sédhiou'
UNION ALL
SELECT d.id_departement, 'Bakel' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bakel' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Ballou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bakel' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Bélé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bakel' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Diawara' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bakel' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Gabou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bakel' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Gathiary' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bakel' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Kidira' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bakel' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Madina Foulbé' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bakel' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Moudéry' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bakel' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Sadatou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bakel' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Sinthiou Fissa' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bakel' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Toumboura' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bakel' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Bala' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudiry' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Bani Israël' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudiry' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Boutoucoufara' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudiry' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Boynguel Bamba' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudiry' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Dianké Makha' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudiry' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Dougué' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudiry' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Goumbayél' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudiry' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Goudiry' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudiry' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Koar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudiry' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Komoti' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudiry' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Kothiary' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudiry' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Koulor' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudiry' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Koussan' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudiry' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Sinthiou Bocar Ali' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudiry' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Sinthiou Mamadou Boubou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Goudiry' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Bamba Thialène' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koumpentoum' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Kahène' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koumpentoum' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Koumpentoum' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koumpentoum' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Kouthia Gaydi' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koumpentoum' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Kouthiaba Wolof' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koumpentoum' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Malem Niani' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koumpentoum' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Méréto' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koumpentoum' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Ndame' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koumpentoum' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Niani Toucouleur' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koumpentoum' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Pass Koto' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koumpentoum' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Payar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koumpentoum' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Sinthiou Malème' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Koumpentoum' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Dialacoto' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tambacounda' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Koussanar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tambacounda' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Makacolibantang' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tambacounda' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Missirah' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tambacounda' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Ndoga Babacar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tambacounda' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Néttéboulou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tambacounda' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Tambacounda' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tambacounda' AND r.nom = 'Tambacounda'
UNION ALL
SELECT d.id_departement, 'Diass' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'M''bour' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Fissel' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'M''bour' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Joal-Fadiouth' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'M''bour' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'M''Bour' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'M''bour' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Malicounda' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'M''bour' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Ndiaganiao' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'M''bour' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Ngaparou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'M''bour' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Nguékhokh' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'M''bour' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Nguéniène' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'M''bour' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Popenguine-Ndayane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'M''bour' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Saly' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'M''bour' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Sandiara' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'M''bour' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Séssène' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'M''bour' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Sindia' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'M''bour' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Somone' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'M''bour' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Thiadiaye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'M''bour' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Diender' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Thiès' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Fandène' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Thiès' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Kayar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Thiès' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Keur Moussa' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Thiès' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Khombole' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Thiès' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Ndiéyène Sirah' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Thiès' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Ngoudiane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Thiès' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Notto' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Thiès' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Pout' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Thiès' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Tassette' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Thiès' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Thiénaba' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Thiès' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Thiès Est' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Thiès' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Thiès Nord' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Thiès' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Thiès Ouest' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Thiès' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Touba Toul' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Thiès' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Yaboyabo' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Thiès' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Chérif Lo' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tivaouane' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Darou Khoudoss' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tivaouane' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Koul' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tivaouane' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Mbayène' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tivaouane' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Mboro' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tivaouane' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Meckhe' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tivaouane' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Méouane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tivaouane' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Mérina Dakhar' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tivaouane' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Mont Rolland' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tivaouane' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Ngandiouf' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tivaouane' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Niakhene' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tivaouane' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Notto Gouye Diama' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tivaouane' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Pambal' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tivaouane' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Pékèsse' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tivaouane' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Pire Goureye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tivaouane' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Taïba Ndiaye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tivaouane' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Thilmakha' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tivaouane' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Tivaouane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Tivaouane' AND r.nom = 'Thiès'
UNION ALL
SELECT d.id_departement, 'Balinghore' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bignona' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Bignona' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bignona' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Diégoune' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bignona' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Diouloulou' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bignona' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Djibidione' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bignona' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Djinaky' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bignona' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Kafountine' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bignona' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Kartiack' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bignona' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Kataba 1' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bignona' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Mangagoulack' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bignona' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Niamone' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bignona' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Oulampane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bignona' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Ouonck' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bignona' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Sindian' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bignona' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Suelle' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bignona' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Tenghory' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bignona' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Thionck Essyl' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Bignona' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Oussouye' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Oussouye' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Mlomp' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Oussouye' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Oukout' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Oussouye' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Diembéring' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Oussouye' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Santhiaba Manjacques' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Oussouye' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Adéane' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Ziguinchor' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Boutoupa Camaracounda' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Ziguinchor' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Enampore' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Ziguinchor' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Niaguis' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Ziguinchor' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Nyassia' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Ziguinchor' AND r.nom = 'Ziguinchor'
UNION ALL
SELECT d.id_departement, 'Ziguinchor' FROM departement d JOIN region r ON r.id_region = d.id_region WHERE d.nom = 'Ziguinchor' AND r.nom = 'Ziguinchor'
ON CONFLICT (id_departement, nom) DO NOTHING;

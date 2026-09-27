INSERT INTO type_espace (nom, description, icone, couleur, ordre_affichage, actif, date_creation)
VALUES
('Boutique', 'Espace de vente de produits', 'store', '#16a34a', 1, TRUE, CURRENT_TIMESTAMP),
('Service', 'Espace de prestations de services', 'briefcase', '#0f766e', 2, TRUE, CURRENT_TIMESTAMP),
('Restaurant', 'Espace de restauration', 'utensils-crossed', '#f59e0b', 3, TRUE, CURRENT_TIMESTAMP),
('Hôtel', 'Espace d’hébergement et de réservation', 'bed-double', '#0ea5e9', 4, TRUE, CURRENT_TIMESTAMP),
('Clinique', 'Espace de soins et de santé', 'stethoscope', '#dc2626', 5, TRUE, CURRENT_TIMESTAMP),
('Pharmacie', 'Espace de vente de médicaments', 'pill', '#7c3aed', 6, TRUE, CURRENT_TIMESTAMP),
('Garage', 'Espace de réparation automobile', 'car-front', '#ea580c', 7, TRUE, CURRENT_TIMESTAMP),
('École', 'Établissement d’enseignement', 'school', '#0891b2', 8, TRUE, CURRENT_TIMESTAMP),
('Université', 'Établissement d’enseignement supérieur', 'graduation-cap', '#2563eb', 9, TRUE, CURRENT_TIMESTAMP),
('Supermarché', 'Espace de grande distribution', 'shopping-cart', '#059669', 10, TRUE, CURRENT_TIMESTAMP),
('Cabinet', 'Espace professionnel de consultation', 'file-text', '#8b5cf6', 11, TRUE, CURRENT_TIMESTAMP),
('Agence', 'Espace de services et de représentation', 'building-2', '#475569', 12, TRUE, CURRENT_TIMESTAMP),
('Atelier', 'Espace de fabrication ou de réparation', 'hammer', '#b45309', 13, TRUE, CURRENT_TIMESTAMP),
('Association', 'Organisation associative', 'users', '#14b8a6', 14, TRUE, CURRENT_TIMESTAMP),
('Administration', 'Service administratif ou public', 'landmark', '#334155', 15, TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (nom) DO NOTHING;
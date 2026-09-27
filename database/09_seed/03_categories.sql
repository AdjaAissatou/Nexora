INSERT INTO categorie (nom, description, icone, couleur, image, ordre_affichage, actif, date_creation)
VALUES
('Informatique', 'Produits et services informatiques', 'laptop', '#2563eb', NULL, 1, TRUE, CURRENT_TIMESTAMP),
('Santé', 'Services médicaux, pharmacies et cliniques', 'heart-pulse', '#dc2626', NULL, 2, TRUE, CURRENT_TIMESTAMP),
('Restauration', 'Restaurants, snacks et services de repas', 'utensils', '#f59e0b', NULL, 3, TRUE, CURRENT_TIMESTAMP),
('Commerce', 'Boutiques et vente de produits', 'shopping-bag', '#16a34a', NULL, 4, TRUE, CURRENT_TIMESTAMP),
('Beauté', 'Coiffure, esthétique et soins', 'sparkles', '#ec4899', NULL, 5, TRUE, CURRENT_TIMESTAMP),
('Transport', 'Taxis, livraison et mobilité', 'car', '#0f766e', NULL, 6, TRUE, CURRENT_TIMESTAMP),
('Immobilier', 'Maisons, appartements et terrains', 'house', '#7c3aed', NULL, 7, TRUE, CURRENT_TIMESTAMP),
('Éducation', 'Écoles, formations et universités', 'graduation-cap', '#0891b2', NULL, 8, TRUE, CURRENT_TIMESTAMP),
('Services', 'Prestations de services divers', 'briefcase', '#4b5563', NULL, 9, TRUE, CURRENT_TIMESTAMP),
('Automobile', 'Véhicules, garages et pièces auto', 'car-side', '#ea580c', NULL, 10, TRUE, CURRENT_TIMESTAMP),
('Hôtellerie', 'Hôtels, chambres et hébergement', 'bed', '#0ea5e9', NULL, 11, TRUE, CURRENT_TIMESTAMP),
('Administration', 'Services et établissements administratifs', 'building', '#475569', NULL, 12, TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (nom) DO NOTHING;
CREATE INDEX idx_offre_titre
ON offre
USING GIN(titre gin_trgm_ops);

CREATE INDEX idx_offre_description
ON offre
USING GIN(description gin_trgm_ops);

CREATE INDEX idx_offre_categorie
ON offre(id_categorie);

CREATE INDEX idx_offre_espace
ON offre(id_espace);

CREATE INDEX idx_espace_nom
ON espace_professionnel(nom);

CREATE INDEX idx_espace_type
ON espace_professionnel(id_type_espace);

CREATE INDEX idx_favori_utilisateur
ON favori(id_utilisateur);

CREATE INDEX idx_commande_utilisateur
ON commande(id_utilisateur);

CREATE INDEX idx_paiement_commande
ON paiement(id_commande);

CREATE INDEX idx_reservation_utilisateur
ON reservation(id_utilisateur);

CREATE INDEX idx_reservation_offre
ON reservation(id_offre);

CREATE INDEX idx_reservation_espace
ON reservation(id_espace);

CREATE INDEX idx_adresse_gps
ON adresse(latitude, longitude);

CREATE INDEX idx_disponibilite_offre
ON disponibilite(id_offre);

CREATE INDEX idx_disponibilite_date
ON disponibilite(date_debut, date_fin);

CREATE INDEX idx_promotion_offre
ON promotion(id_offre);

CREATE INDEX idx_promotion_espace
ON promotion(id_espace);

CREATE INDEX idx_promotion_active
ON promotion(date_debut, date_fin)
WHERE actif = TRUE;

CREATE INDEX idx_offre_publiee
ON offre(id_categorie)
WHERE statut = 'PUBLIE';
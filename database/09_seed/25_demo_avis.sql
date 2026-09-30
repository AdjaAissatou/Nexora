-- ============================================================================
-- DÉMONSTRATION (hors install.sql) : de vrais avis sur les espaces de démo.
--
-- Le jeu de démonstration (07_demo_espaces.sql) affichait des notes et des
-- nombres d'avis inventés, sans aucun avis en base. La note d'un espace est
-- désormais une projection de ses avis visibles (§9.10) : ce script crée des avis
-- réels, écrits par les comptes de démonstration (jamais sur leur propre espace),
-- puis recalcule la note de TOUS les espaces à partir des avis visibles.
--
-- Rejouable : les avis déjà présents (même auteur, même espace) ne sont pas recréés.
-- ============================================================================

INSERT INTO avis (id_utilisateur, id_espace, note, commentaire, date_creation)
SELECT u.id_utilisateur, e.id_espace, v.note, v.commentaire, NOW() - (v.jours || ' days')::interval
FROM (VALUES
    ('ousmane.kane@nexora-demo.sn', 'Auchan Discount Sénégal', 4, 'Rayons bien fournis et prix corrects. Un peu d''attente en caisse le samedi.', 12),
    ('sarah.ba@nexora-demo.sn', 'Auchan Discount Sénégal', 5, 'Toujours propre, personnel aimable. Je fais mes courses ici chaque semaine.', 30),
    ('moussa.diagne@nexora-demo.sn', 'Auchan Discount Sénégal', 3, 'Pratique, mais certains produits manquent souvent.', 45),
    ('fatou.diop@nexora-demo.sn', 'Atelier Diagne Bois', 5, 'Table en bois massif livrée à la date prévue, finition impeccable.', 8),
    ('cheikh.ndiaye@nexora-demo.sn', 'Atelier Diagne Bois', 4, 'Beau travail sur nos portes. Devis clair, délai un peu long.', 60),
    ('baye.fall@nexora-demo.sn', 'SOS Plomberie Dakar', 4, 'Intervention rapide un dimanche pour une fuite. Tarif correct pour une urgence.', 5),
    ('sarah.ba@nexora-demo.sn', 'SOS Plomberie Dakar', 5, 'Très professionnels, ils ont tout nettoyé après les travaux.', 21),
    ('fatou.diop@nexora-demo.sn', 'Plomberie Ndiaye & Fils', 4, 'Chauffe-eau posé proprement, bons conseils.', 15),
    ('ousmane.kane@nexora-demo.sn', 'Plomberie Ndiaye & Fils', 3, 'Travail correct mais arrivés avec une heure de retard.', 40),
    ('moussa.diagne@nexora-demo.sn', 'Plomberie Ndiaye & Fils', 5, 'Débouchage réglé en vingt minutes. Je recommande.', 3),
    ('ibrahima.sarr@nexora-demo.sn', 'Baye Transport', 5, 'Colis livré à Thiès le jour même, chauffeur ponctuel.', 9),
    ('cheikh.ndiaye@nexora-demo.sn', 'Baye Transport', 4, 'Chauffeur prudent et à l''heure pour l''aéroport.', 33),
    ('fatou.diop@nexora-demo.sn', 'Boutique Sarah Mode', 5, 'Superbe robe en wax, retouches faites sur place.', 18),
    ('baye.fall@nexora-demo.sn', 'Boutique Sarah Mode', 4, 'Bon choix de chemises, accueil chaleureux.', 27),
    ('ibrahima.sarr@nexora-demo.sn', 'Boutique Sarah Mode', 4, 'Boubou de qualité pour la Tabaski, prix raisonnable.', 70),
    ('sarah.ba@nexora-demo.sn', 'TechnoPlus Informatique', 4, 'Ordinateur bien configuré avant la livraison. Garantie expliquée clairement.', 11),
    ('moussa.diagne@nexora-demo.sn', 'TechnoPlus Informatique', 5, 'Réparation de mon imprimante en une journée.', 25),
    ('cheikh.ndiaye@nexora-demo.sn', 'TechnoPlus Informatique', 3, 'Bons produits, mais le service après-vente répond lentement.', 50)
) AS v(email, espace, note, commentaire, jours)
JOIN utilisateurs u ON u.email = v.email
JOIN espace_professionnel e ON e.nom = v.espace
WHERE e.id_utilisateur <> u.id_utilisateur
  AND NOT EXISTS (SELECT 1 FROM avis a WHERE a.id_utilisateur = u.id_utilisateur AND a.id_espace = e.id_espace);

-- Note de chaque espace = moyenne de ses avis visibles (sur l'espace et sur ses offres).
UPDATE espace_professionnel e SET
    nombre_avis = s.n,
    note_moyenne = CASE WHEN s.n = 0 THEN 0 ELSE s.moyenne END
FROM (
    SELECT ep.id_espace, COUNT(a.id_avis) AS n, ROUND(COALESCE(AVG(a.note), 0), 1) AS moyenne
    FROM espace_professionnel ep
    LEFT JOIN avis a ON NOT a.masque
        AND (a.id_espace = ep.id_espace OR a.id_offre IN (SELECT id_offre FROM offre WHERE id_espace = ep.id_espace))
    GROUP BY ep.id_espace
) s
WHERE e.id_espace = s.id_espace;

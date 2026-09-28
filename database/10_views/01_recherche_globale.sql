DROP VIEW IF EXISTS vue_recherche_globale CASCADE;

CREATE VIEW vue_recherche_globale AS

SELECT

o.id_offre,
o.titre,
o.description,
o.prix,
o.ancien_prix,

o.disponible,
o.est_commandable,
o.est_reservable,
o.stockable,

o.quantite_disponible,
o.score_pertinence,
o.statut,

ep.id_espace,
ep.nom AS espace,

ep.logo,
ep.couverture,
ep.telephone,

ep.certifie,
ep.verifie,

ep.note_moyenne,
ep.nombre_avis,
ep.nombre_vues,
ep.nombre_favoris,

te.nom AS type_espace,

c.nom AS categorie,

a.pays,
a.region,
a.departement,
a.commune,
a.quartier,
a.adresse_complete,
a.latitude,
a.longitude,

p.nom AS promotion,
p.type_reduction,
p.valeur,

d.date_debut,
d.date_fin,
d.capacite_restante

FROM offre o

JOIN espace_professionnel ep
ON ep.id_espace=o.id_espace

JOIN categorie c
ON c.id_categorie=o.id_categorie

JOIN type_espace te
ON te.id_type_espace=ep.id_type_espace

LEFT JOIN adresse a
ON a.id_espace=ep.id_espace
AND a.principale=TRUE

LEFT JOIN promotion p
ON (
       p.id_offre = o.id_offre
    OR p.id_espace = ep.id_espace
)
AND p.actif = TRUE
AND CURRENT_TIMESTAMP BETWEEN p.date_debut AND p.date_fin

LEFT JOIN disponibilite d
ON d.id_offre = o.id_offre
AND d.est_disponible = TRUE

WHERE o.statut = 'PUBLIE'
AND ep.ouvert = TRUE
AND ep.statut = 'ACTIF';
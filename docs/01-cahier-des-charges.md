# Cahier des charges — Plateforme Nexora

## 1. Contexte et vision
Nexora est une marketplace **generique** regroupant dans une seule application
l'ensemble des vendeurs, commercants, entreprises, artisans, professionnels et
etablissements publics/prives. Elle fusionne les usages de Google Maps
(geolocalisation), WhatsApp Business (messagerie), Jumia (e-commerce),
LinkedIn Services (professionnels) et Booking (reservation de prestations).

L'objectif : permettre a un utilisateur de **rechercher instantanement** un
produit ou un service selon de multiples criteres, puis de discuter, comparer,
commander, payer et noter.

## 2. Principe fondateur : la genericite
La plateforme n'est liee a **aucun domaine metier**. Tout produit ou service
peut etre ajoute **sans modifier le code** :

- Les **categories** sont hierarchiques et configurables en base.
- Les **caracteristiques** (attributs) sont dynamiques (modele EAV :
  `Attribut`, `ValeurAttributPossible`, `OffreAttribut`, `Typechamp`).
- Les **types** (offre, categorie, espace, notification, paiement) sont des
  tables de reference administrables.

Ajouter un domaine = insertion de lignes (voir `docs/mpd/seed.sql`).

## 3. Acteurs
| Acteur | Role principal |
|---|---|
| Visiteur | Recherche et consulte sans compte |
| Client | Commande, discute, note, gere favoris/panier |
| Professionnel / Vendeur / Prestataire | Gere un espace, publie des offres |
| Livreur | Prend en charge les livraisons |
| Moderateur | Modere contenus et avis |
| Support client | Assiste les utilisateurs |
| Controleur qualite | Valide certifications et espaces |
| Gestionnaire financier | Suit paiements, wallet, escrow, commissions |
| Administrateur / Super Administrateur | Configure et supervise la plateforme |

## 4. Besoins fonctionnels
1. **Authentification & securite** : inscription, connexion (BCrypt), profils,
   roles, permissions (RBAC), verification email/telephone.
2. **Espaces professionnels** : vitrine, contacts, reseaux sociaux, identifiants
   legaux (RCCM/NINEA/FISCAL), adresses geolocalisees, horaires, certifications.
3. **Catalogue** : offres (produits et services), categories, attributs
   dynamiques, images/videos, disponibilite.
4. **Recherche intelligente** : mot-cle (et voix cote mobile), categorie,
   prix, note, distance/GPS, ville/quartier/region/pays, disponibilite,
   promotion, neuf/occasion, livraison, ouvert maintenant, verifie/certifie,
   marque, et **attributs personnalises**.
5. **Tri** : proximite, prix, note, popularite, ventes, recence, promotion,
   disponibilite, livraison rapide, pertinence, alphabetique.
6. **Interaction** : favoris, avis/notation, conversation, messagerie, appels.
7. **Commande & paiement** : panier, commande, livraison, paiement, wallet,
   escrow, promotions/coupons.
8. **Notifications, historique, statistiques, tableaux de bord.**
9. **Administration** : moderation, validation des professionnels, gestion des
   litiges, rapports, journalisation, audit.
10. **API REST** pour le mobile (Android/iOS).

## 5. Besoins non fonctionnels
- **Architecture** : MVC2 + Clean Architecture + SOLID, prete pour microservices.
- **Securite** : hachage des mots de passe, RBAC, audit, validation des entrees.
- **Performance** : recherche indexee, pagination, requetes Criteria ciblees.
- **Evolutivite / maintenabilite** : couches decouplees, DTO, DAO generiques.
- **Internationalisation** : multi-langue, multi-devise, multi-pays.
- **Responsive** et pret pour Android/iOS via l'API REST.

## 6. Perimetre technique
Java EE / Jakarta EE 10, JSF + PrimeFaces, JPA/Hibernate, PostgreSQL, Maven,
Apache TomEE. Extension mobile Android et API REST prevues des la conception.

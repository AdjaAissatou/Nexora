# Philosophie du modèle Nexora — pourquoi chaque classe existe

> Ce document explique le *raisonnement* derrière le modèle de données, puis
> le relie au **code réel** du dépôt et à son **état d'implémentation**.
> Légende état : ✅ implémenté · 🟡 partiel · ⛔ à venir.

## 1. La philosophie
Quand un utilisateur ouvre Nexora, il veut : **rechercher → trouver plusieurs
offres → comparer → contacter → acheter un produit / réserver un service ou un
espace**. Toute l'architecture est construite autour de ce parcours unique.

Le principe fondateur : le modèle est **générique**. On n'a pas fait une
plateforme pour vendre des ordinateurs, des hôtels ou des consultations — on a
fait une plateforme où **tout produit, service ou espace s'ajoute sans modifier
la base**, grâce à `Offre`, `CategorieOffre`, `Attribut`,
`ValeurAttributPossible` et `OffreAttribut`.

## 2. Pourquoi chaque classe — et où elle vit dans le code

| Concept | Raison d'être | Classe (fichier) | État |
|---|---|---|---|
| **BaseEntity** | Mutualiser id/audit (createdAt, updatedAt, actif, createdBy) au lieu de les répéter dans 30 classes | `common/entity/BaseEntity.java` (`@MappedSuperclass`) | ✅ |
| **Utilisateur** | Un seul compte, plusieurs rôles (Adja = cliente + vendeuse + prestataire) | `domain/user/Utilisateur.java` | ✅ |
| **Role / Permission / Profile** | Ajouter livreur, modérateur, support… sans toucher au code (`isAdmin` figé = à proscrire) | `domain/user/{Role,Permission,Profile}.java` | ✅ |
| **EspaceProfessionnel** | Ce n'est pas la personne qui vend, c'est sa boutique. Un compte → plusieurs espaces (ADJA TECH, Adja Beauty) | `domain/space/EspaceProfessionnel.java` | ✅ |
| **Offre** | Classe centrale : produits **et** services sont des offres → recherchés de la même façon | `domain/catalog/Offre.java` (racine `SINGLE_TABLE`) | ✅ |
| **Produit** | Spécifique aux biens physiques : stock, état, marque, garantie | `domain/catalog/Produit.java` | ✅ (poids/dimensions ⛔) |
| **ServicePro** | Spécifique aux prestations : tarif, délai, à domicile, disponibilité | `domain/catalog/ServicePro.java` | ✅ (durée 🟡) |
| **CategorieOffre** | Un arbre dynamique (Informatique, Immobilier, Santé…) plutôt qu'une classe par domaine | `domain/catalog/CategorieOffre.java` | ✅ |
| **Attribut** | Décrit une caractéristique (RAM, Carburant, Wi-Fi) : nom, type, filtrable, obligatoire — sans colonne figée | `domain/attribute/Attribut.java` | ✅ |
| **ValeurAttributPossible** | Les valeurs se **choisissent** (Rouge/Bleu ; 8/16/32 Go) → listes déroulantes, pas de saisie | `domain/attribute/ValeurAttributPossible.java` | ✅ |
| **OffreAttribut** | Relie tout : « Dell → RAM → 16 Go ». Une offre porte autant d'attributs que nécessaire, sans modifier la base | `domain/attribute/OffreAttribut.java` | ✅ |
| **Typechamp** | Type de champ (TEXT, NUMBER, LIST, MULTI_LIST…) qui pilote le rendu du formulaire | `domain/attribute/Typechamp.java` | ✅ |
| **Adresse** | Localiser boutique/cabinet/livraison → distance, GPS, itinéraire | `domain/space/Adresse.java` (+ `Pays/Region/Ville`) | ✅ |
| **Conversation / Message** | Nexora = aussi WhatsApp : chaque offre peut avoir des conversations, chacune ses messages | `domain/messaging/{Conversation,Message}.java` | ✅ (temps réel ⛔) |
| **Commande / LigneCommande** | Un « Acheter » crée une commande ; une commande contient plusieurs produits | `domain/order/{Commande,LigneCommande}.java` | 🟡 (entités ✅, service/REST ⛔) |
| **Paiement** | Opération indépendante : payé maintenant, plus tard, remboursé | `domain/payment/Paiement.java` | 🟡 |
| **Livraison** | Toutes les commandes ne sont pas retirées sur place ; cycle propre | `domain/order/Livraison.java` | 🟡 |
| **Avis** | Après achat, une note → réputation du vendeur (note moyenne de l'espace) | `domain/review/Avis.java` | 🟡 |
| **Certification** | Médecin/plombier/vendeur certifié → confiance (badge « Vérifié Nexora ») | `domain/space/Certification.java` | ✅ (validation admin) |
| **Promotion** | -50 % pendant une semaine : dates de début/fin, conditions | `domain/payment/Promotion.java` | 🟡 |
| **Notification** | Informer sans surveiller : commande, message, paiement, livraison, avis | `domain/messaging/Notification.java` | 🟡 |

## 3. Le parcours global (résumé)
```
Utilisateur → possède un compte → crée un EspaceProfessionnel → publie des Offres
   → Produit / Service (/ Espace)
   → appartient à une Catégorie
   → possède des Attributs dynamiques (OffreAttribut → Attribut → ValeurAttributPossible)
   → apparaît dans la recherche
   → le client consulte la fiche
       → Message → Conversation
       → Réservation ─┐
       → Achat → Commande → LigneCommande → Paiement → Livraison → Avis
```
Diagrammes détaillés : `docs/diagrams/` (séquences recherche & commande/escrow).

## 4. Nuances honnêtes entre cette philosophie et le code
Pour rester transparent sur ce qui existe *aujourd'hui* :

1. **« Service »** est nommé **`ServicePro`** dans le code (extends `Offre`). ✅
2. **Le 3ᵉ pilier « Espace »** (réserver une salle, une chambre d'hôtel) : dans
   le modèle actuel, `EspaceProfessionnel` est la **vitrine** du pro ; un lieu
   réservable est représenté comme une **offre** (produit/service) de cet espace.
   Un sous-type d'offre dédié `Espace` (capacité, calendrier) reste **⛔ à ajouter**
   si l'on veut des règles de réservation propres aux lieux.
3. **« Réservation »** n'est pas encore une entité distincte : la réservation de
   prestation est aujourd'hui portée par `Commande` (champs `datePrestation`,
   `heurePrestation`, `statutPrestation`). Une entité **`Reservation`** dédiée
   (avec ses statuts : En attente / Acceptée / Confirmée / Terminée…) est **⛔**.
4. **Produit** : `poids`/`dimensions` ne sont pas des colonnes fixes — ils
   peuvent déjà être des **attributs dynamiques** (cohérent avec la philosophie EAV).

## 5. Prochaines briques cohérentes avec ce parcours
- Entité **`Reservation`** + service + REST (réserver service/espace).
- Sous-type d'offre **`Espace`** (capacité, calendrier, équipements).
- Services **Panier · Commande · Paiement (Wave/Orange Money) · Livraison** (entités déjà prêtes).
- Recalcul de la **réputation** (Avis → note moyenne) et **Trust Score**.

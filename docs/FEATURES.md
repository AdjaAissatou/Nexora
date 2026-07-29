# Nexora — Inventaire des fonctionnalités intégrées

Légende : ✅ implémenté (code fonctionnel) · 🟡 modélisé (entité + schéma SQL,
service/REST à venir) · 🎨 présent dans le prototype web (simulé) ·
⛔ non commencé / extension future.

---

## 1. Modélisation & documentation ✅
- Cahier des charges (`docs/01-cahier-des-charges.md`).
- Architecture technique MVC2 + Clean Architecture / SOLID (`docs/02-architecture.md`).
- 7 diagrammes UML PlantUML : cas d'utilisation, classes, séquences (recherche,
  commande/escrow), activités, composants, déploiement.
- MPD PostgreSQL complet (`docs/mpd/schema.sql`) + données de configuration
  (`docs/mpd/seed.sql`).
- Design system (`docs/ux/design-system.md`) + doc API REST (`docs/api/README.md`).

## 2. Modèle de données — 43 entités JPA ✅
Audit mutualisé (`BaseEntity`), héritage `Offre → Produit / ServicePro`
(SINGLE_TABLE). Modules : utilisateur/RBAC, espace, catalogue, **EAV**, avis,
messagerie, commande, paiement, litige.

## 3. Moteur générique (cœur de la plateforme) ✅
- **Modèle EAV** : `Attribut`, `ValeurAttributPossible`, `OffreAttribut`,
  `Typechamp` — tout produit/service s'ajoute **sans modifier le code**.
- Catégories hiérarchiques configurables + types (offre, catégorie, espace,
  notification, paiement) administrables en base.

## 4. Authentification & sécurité ✅
- Inscription / connexion (`AuthService`) — mots de passe **BCrypt**.
- **JWT HS256** sans dépendance (`JwtService`), secret via `NEXORA_JWT_SECRET`.
- Annotation `@Secured` + `AuthenticationFilter` (routes protégées).
- `CorsFilter` (web + mobile), `ApiExceptionMapper` (erreurs 400/404 normalisées).
- RBAC **modélisé** (Utilisateur → Profile → Role → Permission) 🟡 contrôle fin
  des rôles à activer.

## 5. Recherche intelligente ✅
Moteur multi-critères construit dynamiquement en **JPA Criteria** (`OffreDao`) :
mot-clé, catégorie, type, prix min/max, note, **géolocalisation** (bounding-box
+ haversine), ville/quartier/région/pays, vendeur vérifié/certifié, **facettes
sur attributs dynamiques (EAV)**, tri (proximité, prix, note, popularité,
récent, pertinence, alphabétique), pagination.
Recherche vocale ⛔ (extension mobile).

## 6. API REST (JAX-RS) ✅
| Domaine | Endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` (→ JWT) |
| Catalogue | `GET /api/offres` (recherche), `GET /api/offres/{id}`, `GET /api/categories` |
| Admin (protégé) | `GET /api/admin/stats`, `/repartition`, `/espaces/pending`, `POST /espaces/{id}/valider`, `/espaces/{id}/certifier`, `GET /admin/litiges`, `POST /admin/litiges/{id}/arbitrer` |

## 7. Administration & modération ✅
`AdminService` : statistiques globales, répartition des annonces, **validation
et certification** des espaces professionnels, **arbitrage des litiges**.
DAOs d'agrégation (`StatDao`, `LitigeDao`).

## 8. Espaces professionnels ✅
- Entité riche (contacts, réseaux sociaux, RCCM/NINEA/FISCAL, réputation) ✅.
- Adresses géolocalisées, horaires, certifications ✅ (modèle + cascade).
- Validation/certification côté admin ✅.
- **Création par l'utilisateur connecté** (`POST /api/espaces`) + « mes espaces »,
  et **publication d'offres** (`POST /api/offres`) avec attributs EAV ✅.

## 8b. Référentiel & listes déroulantes ✅ (saisie minimale)
- Tables de référence Pays / Régions (14 du Sénégal) / Villes / Devises +
  `seed_catalogue.sql` (types d'espace, catégories, **attributs avec valeurs
  prédéfinies**, modes de paiement Wave/Orange Money/Free Money…).
- Endpoints `/api/ref/*` et `/api/categories/{id}/attributs` : les formulaires
  (création d'espace, publication d'offre) sont **quasi 100 % en dropdowns**.
- Parcours fournisseur Flutter complet : connexion → ajouter mon espace
  (dropdowns cascadés pays→région→ville) → publier une offre (catégorie →
  attributs dynamiques). Prototype web : vue « Publier » équivalente.

## 9. Présentation web (JSF / PrimeFaces) ✅
- Page de recherche `index.xhtml` + `RechercheBean` (MVC2) branchés sur
  `OffreService`.
- Configuration Jakarta EE 10 (persistence.xml, web.xml, beans.xml,
  faces-config.xml, datasource TomEE), locales FR/EN.

## 10. Prototype web premium (design de référence) 🎨
`docs/ux/nexora-ui-prototype.html` — autonome, Poppins embarquée :
- **Thème clair/sombre** (bouton soleil/lune, persistant). ✅
- **Traduction FR/EN** (système i18n, 70+ clés). ✅
- Accueil (hero + recherche + catégories + meilleures offres + services proches
  sur **carte vectorielle interactive**). 🎨
- Recherche (résultats + carte + filtres), fiche détail. 🎨
- **Tableau de bord fournisseur** (KPIs, courbe de vues, revenus, commandes). 🎨
- **Administration** (KPIs, courbe d'inscriptions, donut, file de modération). 🎨
- **Messagerie** style WhatsApp (conversations + fil + envoi). 🎨
- Sidebar desktop + bottom-nav mobile, animations discrètes, responsive. ✅

## 11. Application mobile Flutter (Android + iOS) ✅
`mobile/` — thème Nexora (émeraude, Poppins, clair/sombre), modèles
Offre/Categorie, `ApiService` REST (**repli hors ligne**), écrans
Accueil / Explorer (recherche) / Détail, carte d'offre, bottom-nav.
Consomme `/categories`, `/offres`, `/auth/*` (base URL configurable).
Écran connexion/inscription 🟡 (méthode `login` prête).

## 12. Fonctionnalités modélisées — service/REST à venir 🟡
Entités + tables + relations prêtes, logique applicative à brancher :
- **Panier & commande** (Panier, LignePanier, Commande, LigneCommande,
  HistoriqueStatutCommande).
- **Livraison** (transporteur, suivi, frais, distance).
- **Paiement & finance** (Paiement, ModePaiement, StatutPaiement, **Wallet**,
  **Transaction**, **Escrow** / séquestre).
- **Promotions & coupons** (Promotion).
- **Avis & notation**, **Favoris**.
- **Litiges** (arbitrage admin ✅ ; ouverture côté client 🟡).

## 13. Messagerie, appels & notifications 🟡🎨
- Entités Conversation, Message, Appel, Notification, TypeNotification ✅.
- Interface de chat 🎨 (prototype). Temps réel WebSocket (JSR-356) ⛔.
- Appels audio/vidéo (WebRTC) ⛔ (traçabilité `Appel` modélisée).

## 14. Transverse
- **Multi-langue** : web FR/EN ✅ ; mobile FR ✅ ; EN mobile 🟡.
- **Multi-devise / multi-pays** : champs `devise`, `Adresse.pays` 🟡 (logique de
  conversion ⛔).
- **Géolocalisation** : recherche par distance ✅ ; cartes = rendu vectoriel
  (prototype) 🎨 ; intégration Google Maps / OSM live ⛔.
- **Statistiques / tableaux de bord** : backend admin ✅ ; dashboards visuels 🎨.
- **Journalisation / audit** : audit d'entité (`BaseEntity`) + historique de
  commande ✅ ; audit applicatif étendu 🟡.
- **Paiement en ligne (passerelle)**, **push/SMS/email**, **microservices** ⛔
  (points d'extension prévus par l'architecture).

---

### Résumé
✅ **Socle complet** : modèle de données générique (EAV), recherche intelligente
multi-critères géolocalisée, authentification JWT, administration/modération,
API REST, front JSF, prototype web premium (thème + i18n + dashboards +
messagerie), app mobile Flutter branchée sur l'API.
🟡 **Prochaines briques** : panier/commande/paiement (wallet, escrow), avis/
favoris, messagerie temps réel, écran d'auth mobile.

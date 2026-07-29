# API REST Nexora

Base : `/{context}/api` (ex. `http://localhost:8080/nexora/api`).
Format : JSON. Auth : JWT (`Authorization: Bearer <token>`) sur les routes protégées.
CORS activé (`CorsFilter`) pour le web et le mobile Flutter.

## Authentification

### POST `/auth/register`
Crée un compte et renvoie un jeton.
```json
// requête
{ "nom": "Diop", "prenom": "Awa", "email": "awa@mail.com", "telephone": "+221...", "motDePasse": "secret123" }
// réponse 201
{ "token": "<jwt>", "tokenType": "Bearer", "expiresIn": 86400,
  "utilisateur": { "id": 1, "nom": "Diop", "prenom": "Awa", "email": "awa@mail.com", "profile": null } }
```

### POST `/auth/login`
```json
{ "email": "awa@mail.com", "motDePasse": "secret123" }
```
Réponse 200 : même structure `AuthResponse` que ci-dessus.

## Catalogue (public)

### GET `/categories`
Liste les catégories racines visibles (`CategorieDTO[]`).

### GET `/offres`
Recherche multi-critères paginée. Paramètres de requête :

| Param | Type | Exemple |
|---|---|---|
| `motCle` | string | `plombier` |
| `idCategorie` | long | `12` |
| `idTypeOffre` | long | `1` (produit) / `2` (service) |
| `prixMin` / `prixMax` | number | `5000` |
| `noteMin` | int | `4` |
| `ville` | string | `Dakar` |
| `lat` / `lng` / `rayon` | number | `14.69` / `-17.44` / `5` (km) |
| `verifie` | bool | `true` |
| `tri` | string | `PROXIMITE`, `PRIX_ASC`, `NOTE`, `RECENT`, `PERTINENCE`… |
| `page` / `taille` | int | `0` / `20` |

Réponse : `PageResult<OffreDTO>` → `{ contenu:[…], total, page, taillePage }`.

Chaque `OffreDTO` porte désormais les champs de la **carte de résultat premium** :
`espaceVerifie`, `espaceCertifie`, `noteEspace`, `nombreAvis`, `ouvert`,
`distanceKm`, `dureeEstimeeMin` (temps estimé, minutes), `telephone`,
`adresseCourte`, `latitude`, `longitude` (pour l'itinéraire).

### GET `/offres/{id}`
Détail d'une offre (incrémente le compteur de vues).

## Référentiel — listes déroulantes (public)
Alimente les formulaires pour minimiser la saisie manuelle.

| Méthode | Route | Retour |
|---|---|---|
| GET | `/ref/types-espace` | Types d'espace (`RefItem[]`) |
| GET | `/ref/categories-espace` | Catégories d'espace |
| GET | `/ref/types-offre` | PRODUIT / SERVICE |
| GET | `/ref/modes-paiement` | Wave, Orange Money, Free Money, Espèces… |
| GET | `/ref/devises` | XOF, EUR, USD… |
| GET | `/ref/pays` | Pays (code + indicatif dans `extra`) |
| GET | `/ref/regions?pays={id}` | Régions d'un pays (`GeoDTO[]`) |
| GET | `/ref/villes?region={id}` | Villes d'une région |
| GET | `/categories/all` | Toutes les catégories |
| GET | `/categories/{id}/attributs` | **Attributs dynamiques + valeurs possibles** → formulaire en dropdowns (`AttributDTO[]`) |

`AttributDTO` : `id`, `nom`, `typeChamp` (LIST / MULTI_LIST / NUMBER / BOOLEAN / TEXT…),
`obligatoire`, `unite`, `valeurs[]` (`{id, valeur}`).

## Espace professionnel (protégé — `Secured`)
Toute connexion mène à un espace utilisateur ; l'utilisateur peut y créer ses
espaces professionnels (boutique, service, clinique…) et publier ses offres.

| Méthode | Route | Corps / effet |
|---|---|---|
| POST | `/espaces` | Crée un espace pour l'utilisateur connecté (`EspaceRequest`) |
| GET | `/espaces/mes` | Liste les espaces de l'utilisateur connecté |
| POST | `/offres` | Publie une offre produit/service (`OffreRequest`, avec attributs EAV) |

`EspaceRequest` : `nomCommercial`, `idTypeEspace`, `telephonePrincipal`,
`ville`, `quartier`, `latitude`, `longitude`, `logo`…
`OffreRequest` : `type` (PRODUIT|SERVICE), `titre`, `prix`, `idEspace`,
`idCategorie`, champs produit/service, et `attributsTexte` (idAttribut → valeur).

## Administration & modération (protégé — `Secured`)

| Méthode | Route | Rôle |
|---|---|---|
| GET | `/admin/stats` | KPIs globaux (`AdminStatsDTO`) |
| GET | `/admin/repartition` | Répartition des annonces (`SegmentDTO[]`) |
| GET | `/admin/espaces/pending` | Espaces en attente de validation |
| POST | `/admin/espaces/{id}/valider` | Marquer un espace vérifié |
| POST | `/admin/espaces/{id}/certifier?valeur=true` | Badge certifié |
| GET | `/admin/litiges` | Litiges ouverts |
| POST | `/admin/litiges/{id}/arbitrer` | Décision `{ statut, decision }` |

## Sécurité
- Mots de passe hachés **BCrypt** (`AuthServiceImpl`).
- Jetons **JWT HS256** (`JwtService`) — secret via `NEXORA_JWT_SECRET`.
- Filtre `AuthenticationFilter` sur les routes `@Secured`.
- Erreurs normalisées par `ApiExceptionMapper` (400 métier / 404 introuvable).

## Clients
- **Web** : prototype `docs/ux/nexora-ui-prototype.html` (mapping documenté dans
  `docs/ux/design-system.md`).
- **Mobile Flutter** : `mobile/lib/services/api_service.dart` consomme
  `/categories`, `/offres`, `/auth/*` (repli hors ligne si l'API est injoignable).

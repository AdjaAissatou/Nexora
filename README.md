# Nexora

Plateforme numérique sénégalaise qui met en relation les utilisateurs avec les professionnels, commerces, services et établissements, au sein d'un même écosystème.

## Backend — Architecture microservices Spring Boot / Java 17

### Services inclus

| Service | Port | Rôle |
|---------|------|------|
| nexora-discovery-server | 8761 | Eureka — registre des services |
| nexora-config-server | 8888 | Config centralisée (`config-repo/`) |
| nexora-api-gateway | 8080 | Point d'entrée unique |
| nexora-auth-service | 8081 | Authentification JWT |
| nexora-user-service | 8082 | Gestion des profils utilisateurs |
| nexora-espace-service | 8083 | Espaces professionnels |
| nexora-catalogue-service | 8084 | Recherche d'offres + fiche détaillée |
| nexora-recherche-service | 8085 | Favoris, historique, avis, signalements |
| nexora-commerce-service | — | Panier, commandes, paiements (à venir) |
| nexora-communication-service | — | Messagerie / conversations (à venir) |
| nexora-administration-service | — | Back-office (à venir) |
| nexora-common | — | Code partagé entre services |

### Ordre de démarrage

1. `nexora-discovery-server`
2. `nexora-config-server`
3. `nexora-api-gateway`
4. `nexora-auth-service`
5. `nexora-user-service`
6. `nexora-espace-service`
7. `nexora-catalogue-service`
8. `nexora-recherche-service`

### Variables d'environnement requises

```
DB_URL=jdbc:postgresql://localhost:5432/nexora
DB_USERNAME=nexora_user
DB_PASSWORD=nexora_pass
JWT_SECRET=votre_secret_jwt_256bits_minimum
```

Facultatives (valeurs par défaut entre parenthèses) :

```
NEXORA_UPLOADS_DIR   photos déposées sur Nexora, partagé par web et catalogue-service (~/nexora-uploads)
NEXORA_MODELS_DIR    modèle de la recherche par photo (~/nexora-models)
```

**Recherche par photo.** Au premier démarrage, catalogue-service télécharge une fois le modèle
DINOv2-small (24,5 Mo, Hugging Face, empreinte SHA-256 vérifiée) dans `NEXORA_MODELS_DIR`, puis
calcule en arrière-plan l'empreinte visuelle des photos des offres. Sans accès Internet, déposez
le fichier `dinov2-small-quantized.onnx` dans ce dossier (voir `docs/architecture-acteurs.md` §11) ;
sans modèle, seule la recherche par photo est indisponible.

### Base de données

Exécuter les scripts SQL dans cet ordre (voir `database/`) :

```
database/00_create_database.sql
database/00_init/01_types.sql
database/01_security/*.sql
database/02_shared/*.sql
database/03_professional/*.sql
database/04_catalogue/*.sql
database/05_search/*.sql
database/09_seed/*.sql
database/10_views/01_recherche_globale.sql
```

**Données de démonstration** (7 espaces, leurs offres, avis et horaires, les comptes du
back-office ; mot de passe `Password1!`), rejouable :

```
psql -U postgres -d nexora_marketplace -v ON_ERROR_STOP=1 -f demo.sql
```

**Base existante : ne rejouez pas `install.sql`.** Ses scripts de création commencent par
`DROP TABLE ... CASCADE` et effaceraient vos données. Pour mettre à jour une base déjà installée,
lancez depuis le dossier `database/` :

```
psql -U postgres -d nexora_marketplace -v ON_ERROR_STOP=1 -f mise_a_jour.sql
```

Le script est rejouable sans risque et fonctionne pour toute base installée depuis le 27/09.
Il ajoute ce qui manque : géographie, catalogue détaillé, lieux publics, vérification des
espaces, modération, horaires, permissions, paramètres et comptes de démonstration.

### Endpoints catalogue-service (port 8084)

```
GET  /api/v1/offres/recherche?q=...&commune=...&prixMax=...&tri=PRIX_ASC
GET  /api/v1/offres/{id}
POST /api/v1/offres/recherche-photo          (multipart, champ « photo »)
GET  /api/v1/offres/{id}/similaires          (le même genre d'article dans d'autres espaces)
GET  /api/v1/offres/suggestions?offres=1,2   (vous pourriez aussi aimer)
```

### Endpoints recherche-service (port 8085)

```
POST   /api/v1/historique/recherches
GET    /api/v1/historique/recherches
DELETE /api/v1/historique/recherches
POST   /api/v1/historique/consultations
DELETE /api/v1/historique/consultations

GET    /api/v1/favoris
POST   /api/v1/favoris
DELETE /api/v1/favoris?offreId=X

GET    /api/v1/recherches-sauvegardees
POST   /api/v1/recherches-sauvegardees
DELETE /api/v1/recherches-sauvegardees/{id}

GET    /api/v1/avis/offre/{offreId}   (PUBLIC)
GET    /api/v1/avis/espace/{espaceId} (PUBLIC)
POST   /api/v1/avis
DELETE /api/v1/avis/{id}

POST   /api/v1/signalements
```

### Stack technique

- Java 17 · Spring Boot 4.0.7 · Spring Cloud 2025.1.0
- PostgreSQL · Spring Data JPA · Hibernate
- Spring Security · JWT (jjwt 0.12.7)
- Eureka · Spring Cloud Config · Spring Cloud Gateway
- SpringDoc OpenAPI 2.8.9
- Architecture hexagonale (ports & adaptateurs)

### Auteure

Adja Aïssatou Dione

# Nexora

**Plateforme Marketplace intelligente, generique, evolutive** — recherche de
produits et de services en temps reel, geolocalisation, messagerie, commandes,
paiement (wallet/escrow), avis et administration complete.

Fusion des usages de Google Maps, WhatsApp Business, Jumia, LinkedIn Services
et Booking, dans une plateforme **entierement configurable** : tout produit ou
service s'ajoute **sans modifier le code** (categories et attributs dynamiques
en base de donnees).

## Stack technique
Jakarta EE 10 · JSF · PrimeFaces · JPA / Hibernate · PostgreSQL · Maven ·
Apache TomEE · API REST (JAX-RS) · architecture MVC2 + Clean Architecture / SOLID.

## Structure du projet
```
docs/
  01-cahier-des-charges.md      Cahier des charges
  02-architecture.md            Architecture technique + demarrage
  diagrams/                     UML PlantUML (UC, classes, sequences,
                                activites, composants, deploiement)
  mpd/schema.sql | seed.sql     MPD PostgreSQL + donnees de configuration
src/main/java/com/nexora/
  common/        BaseEntity (audit), enums, exceptions
  domain/        Entites JPA par module (user, space, catalog, attribute,
                 review, messaging, order, payment, dispute)
  repository/    GenericDao + DAO (moteur de recherche Criteria + EAV)
  service/       Interfaces de cas d'usage + impl EJB
  dto/           DTO, mappers, criteres de recherche
  rest/          API REST JAX-RS (mobile)
  web/           ManagedBeans JSF (MVC2)
src/main/webapp/ index.xhtml (recherche PrimeFaces), WEB-INF, resources
```

## Points cles de conception
- **Modele EAV** (`Attribut`, `ValeurAttributPossible`, `OffreAttribut`,
  `Typechamp`) : caracteristiques dynamiques par categorie, sans schema fige.
- **Recherche multi-facettes** construite dynamiquement en JPA Criteria
  (`OffreDao`), avec filtres geo (bounding-box + haversine) et facettes EAV.
- **Heritage** `Offre → Produit / ServicePro` (SINGLE_TABLE) ; audit mutualise
  via `BaseEntity`.
- **Securite** : BCrypt + RBAC (Utilisateur → Profile → Role → Permission).

## Demarrage
```bash
createdb nexora
psql -d nexora -f docs/mpd/schema.sql
psql -d nexora -f docs/mpd/seed.sql
mvn clean package            # -> target/nexora.war (deployer sur TomEE)
```
Web : `http://localhost:8080/nexora/` — API : `http://localhost:8080/nexora/api/offres`

Voir `docs/02-architecture.md` pour le detail.

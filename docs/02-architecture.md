# Architecture technique — Nexora

## 1. Vue en couches (MVC2 + Clean Architecture)

```
┌──────────────────────────────────────────────────────────────┐
│  PRESENTATION                                                  │
│   • JSF + PrimeFaces (.xhtml)      ← Web responsive            │
│   • ManagedBeans (com.nexora.web)  ← Controleurs MVC2          │
│   • REST/JAX-RS (com.nexora.rest)  ← Mobile Android/iOS        │
├──────────────────────────────────────────────────────────────┤
│  APPLICATION / SERVICES (com.nexora.service)                  │
│   • Interfaces de cas d'usage + impl EJB Stateless            │
│   • Transactions, regles metier, orchestration                │
│   • DTO / Mappers (com.nexora.dto)                            │
├──────────────────────────────────────────────────────────────┤
│  DOMAINE (com.nexora.domain)                                  │
│   • Entites JPA par module metier                             │
│   • BaseEntity (audit), enums                                 │
├──────────────────────────────────────────────────────────────┤
│  INFRASTRUCTURE (com.nexora.repository)                       │
│   • GenericDao + DAO specifiques (JPA/Hibernate, Criteria)    │
│   • PostgreSQL                                                │
└──────────────────────────────────────────────────────────────┘
```

Le sens des dependances va **de l'exterieur vers le domaine** : la presentation
depend des services, les services du domaine ; l'infrastructure implemente
l'acces aux donnees. Les entites ne dependent d'aucune couche superieure.

## 2. Modules du domaine
| Package | Contenu |
|---|---|
| `domain.user` | Utilisateur, Profile, Role, Permission (RBAC) |
| `domain.space` | EspaceProfessionnel, Adresse, Horaire, Certification, CategorieEspace, TypeEspaceProfessionnel |
| `domain.catalog` | Offre (racine), Produit, ServicePro, CategorieOffre, TypeOffre, TypeCategorie, Image |
| `domain.attribute` | **EAV** : Attribut, ValeurAttributPossible, OffreAttribut, Typechamp |
| `domain.review` | Avis, Favori |
| `domain.messaging` | Conversation, Message, Appel, Notification, TypeNotification |
| `domain.order` | Commande, LigneCommande, Panier, LignePanier, Livraison, HistoriqueStatutCommande |
| `domain.payment` | Paiement, ModePaiement, StatutPaiement, Wallet, Transaction, Escrow, Promotion |
| `domain.dispute` | Litige |

## 3. Le moteur generique (EAV)
`Offre` porte des `OffreAttribut` (valeurs) rattaches a des `Attribut`
(definitions) eux-memes lies a une `CategorieOffre` et a un `Typechamp`.
La valeur est stockee de facon poly-typee (texte / nombre / date / booleen /
reference a une `ValeurAttributPossible`). Consequence : ajouter "Kilometrage"
a la categorie Voitures ou "Superficie" a la categorie Terrains ne demande
**aucune recompilation**.

La recherche a facettes exploite ces attributs via des sous-requetes correlees
dans `OffreDao.rechercher(...)` (API Criteria), assemblees dynamiquement selon
les filtres reellement fournis.

## 4. Heritage
- `BaseEntity` : `@MappedSuperclass` fournissant l'audit (created/updated/actif).
- `Offre` → `Produit` / `ServicePro` : `SINGLE_TABLE` avec discriminateur
  `type_offre_dtype` (performant, une seule jointure-libre pour la recherche).

## 5. Securite
- Mots de passe haches en **BCrypt** (`AuthServiceImpl`).
- **RBAC** : Utilisateur → Profile → Role → Permission.
- Audit via `BaseEntity` (createdBy, horodatage) et
  `HistoriqueStatutCommande`.

## 6. Deploiement
Application packagee en **WAR** (`nexora.war`) deployee sur **Apache TomEE**,
connectee a **PostgreSQL** via une datasource JTA (`java:app/jdbc/nexoraDS`,
voir `WEB-INF/resources.xml`). Le schema est cree par `docs/mpd/schema.sql`.

## 7. Evolution microservices
Chaque module du domaine (catalogue, commande, paiement, messagerie) est un
candidat naturel a l'extraction en microservice : frontieres nettes, DTO
d'echange deja definis, et API REST exposee des l'origine.

## 8. Demarrage rapide
```bash
# 1. Base de donnees
createdb nexora
psql -d nexora -f docs/mpd/schema.sql
psql -d nexora -f docs/mpd/seed.sql

# 2. Build
mvn clean package        # produit target/nexora.war

# 3. Deploiement : copier nexora.war dans TomEE/webapps
#    puis ouvrir http://localhost:8080/nexora/
#    API REST : http://localhost:8080/nexora/api/offres
```

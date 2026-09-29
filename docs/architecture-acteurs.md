# Nexora — Acteurs, cas d'utilisation, pages et permissions

Document de référence pour toute nouvelle page de Nexora. Avant de dessiner une page,
on vérifie : quel acteur, quel cas d'utilisation, quelle permission. Ce document est la
source de vérité — à mettre à jour à chaque évolution du modèle de rôles ou de pages.

## 1. Modèle de compte — déjà en place, un point à corriger

Le modèle proposé (un seul compte, rôles additifs, pas de compte « client » séparé d'un
compte « professionnel ») **correspond déjà à ce que la base de données modélise** :

- `accounts` : un compte unique par personne (email, mot de passe).
- `roles` : `UTILISATEUR`, `FOURNISSEUR`, `ADMIN`, `SUPER_ADMIN`, `MODERATEUR`,
  `SUPPORT`, `GESTIONNAIRE` — déjà seedés dans `database/09_seed/01_roles.sql`.
  Le rôle `FOURNISSEUR` est même déjà décrit textuellement comme
  *« Compte ayant au moins un espace professionnel »*.
- `account_roles` : table de jointure many-to-many compte ↔ rôle — un compte peut
  cumuler plusieurs rôles.
- `espace_professionnel.id_utilisateur` : un utilisateur peut posséder plusieurs
  espaces (déjà géré côté web par `mesEspaces` / `+ Créer un autre espace`).
- Le JWT embarque déjà les rôles du compte (`JwtProviderAdapter`) et
  `JwtAuthenticationFilter` les traduit en autorités Spring Security
  (`ROLE_UTILISATEUR`, `ROLE_FOURNISSEUR`...).

**Ce qui manque, concrètement — un seul gap identifié :**

À l'inscription, `RegisterAccountService` attribue uniquement `UTILISATEUR`
(`database/01_security` + `RegisterAccountService.java:75`). **Aucun code
n'attribue jamais `FOURNISSEUR`** : créer un espace ne change actuellement pas les
rôles du compte. De même, aucun endpoint n'utilise encore `hasRole(...)` — tout est
`permitAll()` ou `authenticated()` (n'importe quel compte connecté), jamais
role-spécifique.

**✅ Fait** : `CreateEspaceService.create(...)` attribue désormais le rôle
`FOURNISSEUR` au compte s'il ne l'a pas déjà (`RoleAssignmentRepository`, écriture
directe en SQL natif — les deux services partagent la même base physique
`nexora_marketplace`, même convention que `UtilisateurLookupRepository`/
`GeoQueryRepository`). Point à garder en tête : le JWT embarque les rôles à la
connexion, donc un compte qui vient de créer son premier espace ne verra
`FOURNISSEUR` dans ses autorités Spring Security qu'à sa prochaine connexion —
pertinent pour le chantier "navigation adaptative selon les rôles" (§7, point 4).

## 2. Les 4 acteurs

| Acteur | Compte ? | Rôle(s) | Fonction |
|---|---|---|---|
| Visiteur | ❌ | — | Découvrir Nexora sans connexion |
| Utilisateur / Client | ✅ | `UTILISATEUR` | Rechercher, contacter, réserver, acheter, évaluer |
| Professionnel / Fournisseur | ✅ | `UTILISATEUR` + `FOURNISSEUR` | Gérer un ou plusieurs espaces et leurs offres |
| Administrateur | ✅ | `ADMIN` / `SUPER_ADMIN` / `MODERATEUR` / `SUPPORT` / `GESTIONNAIRE` | Administrer et modérer la plateforme |

Un compte peut être Client et Professionnel **simultanément** — ce sont des rôles
cumulés sur le même compte, pas des comptes distincts.

## 3. Pages par acteur — état réel du code aujourd'hui

Légende : ✅ existe déjà · 🚧 à construire · — non prioritaire pour l'instant.

### Visiteur (public, sans connexion)

| Page | Route | État |
|---|---|---|
| Accueil | `/index` | ✅ |
| Recherche / Résultats / Filtres | `/recherche` | ✅ (+ carte Leaflet, lieux publics) |
| Fiche espace professionnel | `/espace?id=` | ✅ |
| Fiche offre | `/offre?id=` | ✅ |
| Connexion | `/connexion` | ✅ |
| Inscription | `/inscription` | ✅ |
| Mot de passe oublié / Réinitialisation | `/mot-de-passe-oublie`, `/reinitialisation` | ✅ |
| Vérification de compte | `/verification` | ✅ |
| Catégories (parcours dédié, hors barre de recherche) | — | 🚧 |
| Carte plein écran (tous les espaces, pas juste résultats de recherche) | — | 🚧 |
| À propos / Aide | — | 🚧 (pas bloquant) |

### Client (connecté, rôle `UTILISATEUR`)

| Page | Route proposée | État |
|---|---|---|
| Mon profil (infos personnelles) | `/mon-compte` | ✅ séparé de la gestion pro (voir §5) |
| Tableau de bord client complet (au-delà du profil) | `/mon-compte` | 🚧 |
| Mes favoris | `/mon-compte/favoris` | 🚧 (table `favori` existe, jamais écrite par le web) |
| Mon historique de consultation | `/mon-compte/historique` | 🚧 (table `historique_consultation` existe, jamais écrite par le web) |
| Mes demandes / messages | `/mon-compte/messages` | 🚧 (tables `conversation`/`message` existent, service `nexora-communication-service` non branché au web) |
| Mes réservations | `/mon-compte/reservations` | 🚧 (table `reservation` existe) |
| Mes commandes | `/mon-compte/commandes` | 🚧 (tables `commande`/`sous_commande` existent) |
| Mes avis laissés | `/mon-compte/avis` | 🚧 (`AvisApiClient` déjà utilisé en lecture côté fiche espace, jamais en écriture) |
| Notifications | `/mon-compte/notifications` | 🚧 (table `notification` existe) |
| Sécurité / paramètres du compte | `/mon-compte/securite` | 🚧 |

### Professionnel (connecté, rôle `FOURNISSEUR`)

| Page | Route | État |
|---|---|---|
| Mon espace — vue d'ensemble + gestion | `/mon-espace` | ✅ (tableau de bord refait récemment : stats, vocabulaire dynamique par type d'espace, cartes d'offres) |
| Gérer les offres (vocabulaire dynamique) | `/mon-espace` (onglet) | ✅ |
| Créer / modifier une offre | `/creer-offre` | ✅ |
| Créer un espace | `/creer-espace` | ✅ |
| Gérer les informations de l'espace (horaires, contact, localisation, photos) | `/mon-espace` (onglet) | ✅ pour l'essentiel — **horaires** (table `horaire`) et **moyens de contact** (table `moyen_contact`) non encore dans le formulaire |
| Demandes reçues | — | 🚧 (dépend de la messagerie, non branchée) |
| Réservations reçues | — | 🚧 |
| Commandes reçues | — | 🚧 |
| Avis reçus + réponse | — | 🚧 (lecture publique déjà ok sur `/espace`, pas de vue dédiée côté pro, pas de réponse) |
| Statistiques détaillées (au-delà de la Vue d'ensemble) | — | 🚧 |
| Demande / gestion de certification | — | 🚧 (colonnes `certifie`/`date_certification` existent, aucun flux) |
| Ma fiche publique | `/espace?id=` (lien depuis Mon espace) | ✅ |

### Administrateur

**Rien n'existe aujourd'hui** : pas de rôle vérifié côté endpoints, pas de page,
`nexora-administration-service` a son schéma (`statistique`, `journal_action`,
`parametre`) mais aucun contrôleur ni page web. C'est le chantier le plus vide et le
plus structurant à cadrer avant de coder quoi que ce soit dessus.

| Page | État |
|---|---|
| Tableau de bord admin | 🚧 tout |
| Gérer les utilisateurs (suspendre/réactiver/supprimer) | 🚧 |
| Gérer/modérer les espaces | 🚧 |
| Gérer le catalogue (catégories, types d'offre, attributs, tags) | 🚧 (déjà en base et seedé, aucune UI d'admin) |
| Modérer avis / traiter signalements | 🚧 |
| Certifications | 🚧 |
| Statistiques plateforme | 🚧 |
| Journal d'activité | 🚧 |
| Paramètres Nexora | 🚧 |

## 4. Navigation par acteur

- **Visiteur** : Accueil · Explorer · Connexion · Inscription.
- **Client** (sans espace) : Accueil · Explorer · Mon compte · Déconnexion.
- **Professionnel** (au moins un espace) : Accueil · Explorer · Mon compte · **Mon
  espace** · Déconnexion. *(Aujourd'hui la barre affiche seulement « Mon espace » —
  il faudra distinguer « Mon compte » = profil/favoris/commandes du côté client, de
  « Mon espace » = gestion professionnelle, dès que Mon compte existera.)*
- **Admin** : navigation séparée, jamais mélangée à la navigation publique
  (`/admin/...`, layout dédié, pas le header Nexora grand public).

## 5. ✅ `mon-espace.xhtml` et le profil personnel sont maintenant séparés

`MonCompteBean` (page `/mon-compte`) porte le profil personnel (prénom, nom,
téléphone) ; `MonEspaceBean` (page `/mon-espace`) ne garde que la gestion
professionnelle (offres, infos de l'espace, suppression de l'espace). Les deux
pages se renvoient l'une vers l'autre (lien "Mon compte" dans la nav verticale de
Mon espace ; carte "Gérer mon espace" / invitation à en créer un dans Mon compte).
`/mon-compte` reste minimal pour l'instant — favoris/historique/commandes etc.
restent à construire (§3, ligne "Tableau de bord client complet").

## 6. Permissions — mapping rôle → permission (déjà seedées, jamais appliquées)

Les permissions existent déjà en base (`database/09_seed/02_permissions.sql`,
table `role_permissions`) mais **aucun contrôleur ne les vérifie** actuellement.
Proposition de mapping pour quand on branchera `hasAuthority(...)` :

| Permission (déjà seedée) | Rôle(s) |
|---|---|
| `GERER_ESPACES` (créer/modifier son propre espace) | `FOURNISSEUR` sur ses propres espaces ; `ADMIN` sur tous |
| `GERER_OFFRES` | `FOURNISSEUR` sur ses offres ; `ADMIN` sur toutes |
| `GERER_FAVORIS`, `GERER_AVIS` (écriture), `GERER_PANIERS`, `GERER_COMMANDES`, `GERER_RESERVATIONS` | `UTILISATEUR` (tout compte connecté) |
| `GERER_CATEGORIES`, `GERER_TYPES_OFFRES`, `GERER_ATTRIBUTS`, `GERER_CERTIFICATIONS` | `ADMIN` uniquement |
| `GERER_SIGNALEMENTS`, `GERER_AVIS` (modération) | `MODERATEUR`, `ADMIN` |
| `GERER_UTILISATEURS`, `GERER_ROLES`, `GERER_PERMISSIONS` | `SUPER_ADMIN` uniquement |
| `VOIR_STATISTIQUES`, `GERER_PARAMETRES`, `GERER_JOURNAL` | `ADMIN`, `GESTIONNAIRE` |

Aujourd'hui, la seule protection réelle est applicative (ex. `OffreController`
vérifie que `accountId` correspond au propriétaire de l'espace avant de modifier une
offre) — pas encore de vérification de rôle Spring Security. Les deux se
complètent : le rôle dit *"cette catégorie d'action est permise à ce type de
compte"*, la vérification applicative dit *"cette instance précise appartient à ce
compte"*.

## 7. Ordre de chantiers proposé

Vu l'état réel du code, dans l'ordre où chaque chantier dépend logiquement du
précédent :

1. ✅ **Rôle `FOURNISSEUR` auto-attribué** à la création d'un espace.
2. ✅ **`mon-espace.xhtml` séparé** : profil personnel sorti vers `/mon-compte`
   (§5).
3. **`/mon-compte` (Client) — compléter** : le profil existe, il reste favoris et
   historique. Les tables existent déjà, il "suffit" de brancher web ↔
   recherche-service qui a déjà les contrôleurs (`FavoriController`,
   `HistoriqueController`, `AvisController`) jamais appelés par le web.
4. **Navigation adaptative** : afficher "Mon espace" dans le header seulement si le
   compte a le rôle `FOURNISSEUR`.
5. **Back-office admin** : le plus gros chantier, à cadrer précisément (pages,
   permissions, layout séparé) avant de commencer à coder — probablement sa propre
   session de conception dédiée plutôt qu'un ajout au fil de l'eau.
6. **Messagerie, réservations, commandes** (client ET pro) : dépendent de
   `nexora-communication-service` et `nexora-commerce-service`, aujourd'hui non
   branchés au web du tout — chantier à part entière une fois 1-4 posés.

---
*Dernière mise à jour : session du 29/09/2026, après la refonte de `mon-espace.xhtml`
en tableau de bord (vue d'ensemble, vocabulaire dynamique par type d'espace).*

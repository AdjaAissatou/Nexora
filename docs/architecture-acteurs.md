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
rôles du compte. De même, aucun endpoint n'utilisait `hasRole(...)` — tout était
`permitAll()` ou `authenticated()`. *Depuis : les routes protégées vérifient des permissions,
voir §6.*

**✅ Fait** : `CreateEspaceService.create(...)` attribue désormais le rôle
`FOURNISSEUR` au compte s'il ne l'a pas déjà (`RoleAssignmentRepository`, écriture
directe en SQL natif — les deux services partagent la même base physique
`nexora_marketplace`, même convention que `UtilisateurLookupRepository`/
`GeoQueryRepository`). Point à garder en tête : le JWT embarque les rôles à la
connexion, donc un compte qui vient de créer son premier espace ne verra
`FOURNISSEUR` dans ses autorités Spring Security qu'à sa prochaine connexion —
pertinent pour le chantier "navigation adaptative selon les rôles" (§7, point 4).

## 2. Les acteurs

```
                         NEXORA
                           │
          ┌────────────────┼─────────────────┐
      VISITEUR        UTILISATEUR       ADMINISTRATION
                           │                 │
                    ┌──────┴──────┐     ┌────┴──────────┐
                  CLIENT    PROFESSIONNEL ADMIN   AGENT DE
                                                  VÉRIFICATION
```

| Acteur | Compte ? | Rôle(s) | Fonction |
|---|---|---|---|
| Visiteur | ❌ | — | Découvrir Nexora sans connexion |
| Utilisateur / Client | ✅ | `UTILISATEUR` | Rechercher, contacter, réserver, acheter, évaluer |
| Professionnel / Fournisseur | ✅ | `UTILISATEUR` + `FOURNISSEUR` | Gérer un ou plusieurs espaces et leurs offres, **demander leur vérification** |
| Agent de vérification | ✅ | `UTILISATEUR` + `AGENT_VERIFICATION` (**nouveau**) | Examiner les demandes de vérification et décider — **sans accès au reste de l'administration** (§8) |
| Administrateur | ✅ | `ADMIN` / `SUPER_ADMIN` / `MODERATEUR` / `SUPPORT` / `GESTIONNAIRE` | Administrer, modérer, **superviser** la vérification (règles, agents, réattribution, révocation) |

Un compte peut être Client et Professionnel **simultanément** — ce sont des rôles
cumulés sur le même compte, pas des comptes distincts. Même principe pour l'agent :
c'est un rôle ajouté à un compte, pas un type de compte.

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
| Mes favoris | `/mon-compte` (section) | ✅ bouton ♡/♥ sur les fiches offre et espace, liste + retrait dans Mon compte |
| Mon historique de consultation | `/mon-compte` (section) | ✅ enregistré à l'ouverture d'une fiche offre/espace, dédoublonné, effaçable |
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
| Vérification de mon espace (dossier, justificatifs, suivi) | `/mon-espace` (onglet « Vérification ») | ✅ (§8.11) |
| Certification 🏅 | — | — (après la vérification, critères à définir, §8.3) |
| Ma fiche publique | `/espace?id=` (lien depuis Mon espace) | ✅ |

### Administrateur

Back-office en construction, cadré au §9 : layout dédié, tableau de bord, journal
d'actions et supervision des vérifications sont en place (étape 6a) ; le reste suit les
étapes 6b à 6f.

| Page | État |
|---|---|
| Tableau de bord admin | ✅ `/admin/index` (§9.7) |
| Gérer les utilisateurs (rechercher, suspendre, réactiver, rôles) | ✅ `/admin/utilisateurs`, `/admin/utilisateur?id=` (§9.8) |
| Rôles et permissions (matrice) | ✅ `/admin/roles` (§6, §9.8) |
| Gérer/modérer les espaces | 🚧 |
| Gérer le catalogue (catégories, types d'offre, attributs, tags) | 🚧 (déjà en base et seedé, aucune UI d'admin) |
| Modérer avis / traiter signalements | 🚧 |
| Vérifications : supervision, agents (§8) | ✅ `/admin/verifications`, dans le layout admin — l'édition des règles de justificatifs reste à faire (6f) |
| Certifications 🏅 | — (après la vérification) |
| Statistiques plateforme | 🚧 |
| Journal d'activité | ✅ `/admin/journal` (§9.7) |
| Paramètres Nexora | 🚧 |

## 4. ✅ Navigation par acteur

- **Visiteur** : Accueil · Explorer · Connexion · Créer un compte.
- **Client** (sans espace) : Accueil · Explorer · Mon compte · Déconnexion.
- **Professionnel** (au moins un espace) : Accueil · Explorer · Mon compte · **Mon
  espace** · Déconnexion.
- **Agent de vérification** : ✅ espace de travail `/verifications/index` (files À traiter ·
  En cours · Informations demandées · Approuvées · Refusées) et `/verifications/examen?id=`.
  Aucun lien vers l'administration. Lien « Vérifications » dans le menu si le compte a le
  rôle `AGENT_VERIFICATION` ; un compte sans ce rôle est renvoyé vers l'accueil
  (`SessionBean.exigerAgentVerification`). Layout public pour l'instant ; un layout dédié
  viendra avec la refonte visuelle.
- **Admin** : ✅ navigation séparée, jamais mélangée à la navigation publique
  (`/admin/...`, layout `WEB-INF/templates/admin.xhtml`, barre latérale filtrée par rôle).
  Lien « Administration » dans le menu public pour les rôles du back-office (§9.2).
  Menu cible : Utilisateurs · Espaces · Vérifications · Agents · Catalogue · Catégories ·
  Offres · Avis · Signalements · Statistiques · Paramètres. Les sections pas encore
  construites apparaissent grisées (« à venir »).

Menu cible de **Mon espace** (professionnel), à construire au fil des chantiers :
Vue d'ensemble · Mes espaces · Mes offres · Demandes · Réservations · Commandes ·
Clients · Messages · Avis · Statistiques · **Vérification** · Informations de l'espace ·
Paramètres · Ma fiche publique.

En place : `SessionBean.isFournisseur()` lit les rôles renvoyés par auth-service à la
connexion ; `SessionBean.rafraichir()` recharge jeton et rôles après la création ou la
suppression d'un espace, sans reconnexion. Le rôle FOURNISSEUR est retiré à la
suppression du dernier espace ; `database/09_seed/20_synchro_role_fournisseur.sql`
resynchronise les comptes existants.

**✅ Expiration du jeton — corrigé.** Le jeton d'accès expire au bout de 15 minutes
(`jwt.access-token-expiration-minutes`). Avant le correctif, le web ne le renouvelait
jamais : passé ce délai, les services répondaient 403 alors que le menu affichait
toujours le compte connecté. Désormais `SessionBean` lit la date d'expiration du jeton
(claim `exp`) et, dans `isConnecte()` et `getAccessToken()`, le renouvelle via
`/api/v1/auth/refresh` quand il reste moins d'une minute. Si le jeton a expiré sans
pouvoir être renouvelé (refresh token de 7 jours expiré ou révoqué, auth-service
injoignable), la session web est fermée : les pages protégées renvoient vers la
connexion et le menu repasse en mode visiteur.

## 5. ✅ `mon-espace.xhtml` et le profil personnel sont maintenant séparés

`MonCompteBean` (page `/mon-compte`) porte le profil personnel (prénom, nom,
téléphone) ; `MonEspaceBean` (page `/mon-espace`) ne garde que la gestion
professionnelle (offres, infos de l'espace, suppression de l'espace). Les deux
pages se renvoient l'une vers l'autre (lien "Mon compte" dans la nav verticale de
Mon espace ; carte "Gérer mon espace" / invitation à en créer un dans Mon compte).
`/mon-compte` porte aussi les favoris et l'historique de consultation ; demandes,
réservations, commandes, messages, avis laissés et notifications restent à
construire (§3).

## 6. ✅ Rôles et permissions — le modèle appliqué

### 6.1 Les quatre niveaux et leurs liaisons

```
utilisateurs ──account_id──► accounts ──account_roles──► roles ──role_permissions──► permissions
 (profil,                    (identité de                 (8 rôles)                 (41 permissions,
  user-service)               connexion, auth-service)                               par module)
```

- **Utilisateur** : le profil (nom, téléphone…), lié à un seul **compte**.
- **Compte** : ce qui se connecte ; il reçoit un ou plusieurs **rôles** (`account_roles`).
  Tout compte a `UTILISATEUR` ; `FOURNISSEUR` suit la possession d'un espace (§1) ;
  `AGENT_VERIFICATION` est donné par la supervision des vérifications (§8) ; les rôles
  administratifs (`SUPER_ADMIN`, `ADMIN`, `MODERATEUR`, `SUPPORT`, `GESTIONNAIRE`) sont donnés
  par un super administrateur depuis la fiche du compte (§9.8).
- **Rôle** : un paquet de **permissions** (`role_permissions`).
- **Permission** : un droit précis (`CONSULTER_UTILISATEURS`, `MODERER_AVIS`…).

### 6.2 Comment c'est vérifié

1. À la connexion et à chaque renouvellement du jeton, auth-service calcule les **permissions
   effectives** du compte : l'union des permissions actives de ses rôles actifs
   (`Account.effectivePermissions()`). Le JWT porte `roles` **et** `permissions`.
2. Chaque service transforme le jeton en autorités Spring Security, de la même façon partout
   (`nexora-common`, `AutoritesJwt`) : `ROLE_<rôle>` et `PERM_<permission>`. Un jeton de
   rafraîchissement n'ouvre plus aucune route. Catalogue et recherche l'acceptaient jusqu'ici :
   corrigé.
3. Les routes protégées vérifient une **permission** (`hasAuthority("PERM_…")`), jamais une liste
   de rôles. Le web fait de même pour son menu et ses gardes (`SessionBean.aLaPermission`,
   `AccesAdminBean`).
4. La vérification applicative reste en plus : la permission dit *« ce type d'action est
   permis »*, le service vérifie *« cette instance précise appartient à ce compte »* (ex.
   `GERER_OFFRES` + propriétaire de l'offre).

Conséquence : **modifier la matrice change les droits sans toucher au code**. Un changement
s'applique à la prochaine connexion des comptes concernés, ou au plus tard au renouvellement
du jeton (15 minutes).

### 6.3 Matrice par défaut (`database/09_seed/24_rbac_permissions.sql`)

| Rôle | Permissions |
|---|---|
| `UTILISATEUR` | `CONSULTER_RECHERCHE`, `GERER_FAVORIS`, `GERER_AVIS` (ses avis), `GERER_CONVERSATIONS`, `GERER_MESSAGES`, `GERER_NOTIFICATIONS`, `GERER_PANIERS`, `GERER_COMMANDES`, `GERER_RESERVATIONS` |
| `FOURNISSEUR` | `GERER_ESPACES`, `GERER_HORAIRES`, `GERER_OFFRES`, `GERER_PROMOTIONS`, `GERER_IMAGES` (les siens) |
| `AGENT_VERIFICATION` | `TRAITER_VERIFICATIONS` |
| `SUPPORT` | `ACCEDER_BACK_OFFICE`, `CONSULTER_UTILISATEURS`, `REACTIVER_UTILISATEURS` |
| `MODERATEUR` | `ACCEDER_BACK_OFFICE`, `CONSULTER_UTILISATEURS`, `MODERER_ESPACES`, `MODERER_OFFRES`, `MODERER_AVIS`, `GERER_SIGNALEMENTS` |
| `GESTIONNAIRE` | `ACCEDER_BACK_OFFICE`, `GERER_CATEGORIES`, `GERER_TYPES_OFFRES`, `GERER_ATTRIBUTS`, `GERER_PARAMETRES`, `VOIR_STATISTIQUES` |
| `ADMIN` | tout le back-office : les permissions ci-dessus + `SUSPENDRE_UTILISATEURS`, `SUPERVISER_VERIFICATIONS`, `GERER_AGENTS_VERIFICATION`, `GERER_CERTIFICATIONS`, `GERER_JOURNAL` |
| `SUPER_ADMIN` | celles d'`ADMIN` + `GERER_ROLES` (rôles administratifs des comptes) et `GERER_PERMISSIONS` (cette matrice) |

Changements du catalogue : 7 permissions ajoutées (`ACCEDER_BACK_OFFICE`,
`CONSULTER_/SUSPENDRE_/REACTIVER_UTILISATEURS`, `MODERER_ESPACES/OFFRES/AVIS`).
`GERER_UTILISATEURS`, qui mélangeait consulter, suspendre et supprimer, est retirée. Les
descriptions distinguent désormais *gérer ses propres données* et *modérer celles des autres*.
Les 7 permissions du module commerce (paiements, escrow, litiges…) n'ont encore aucun rôle :
elles en recevront un quand le module sera branché.

Garde-fous, tous testés :
- le rôle `SUPER_ADMIN` garde toujours `ACCEDER_BACK_OFFICE`, `GERER_ROLES` et
  `GERER_PERMISSIONS` ;
- on ne suspend pas le dernier super administrateur actif, et on ne lui retire pas son rôle ;
- personne n'agit sur son propre compte ;
- toute modification est motivée et journalisée.

## 7. Ordre de chantiers proposé

Vu l'état réel du code, dans l'ordre où chaque chantier dépend logiquement du
précédent :

1. ✅ **Rôle `FOURNISSEUR` auto-attribué** à la création d'un espace.
2. ✅ **`mon-espace.xhtml` séparé** : profil personnel sorti vers `/mon-compte`
   (§5).
3. ✅ **`/mon-compte` : favoris et historique** branchés sur recherche-service
   (`RechercheApiClient`) ; ajout de l'endpoint manquant
   `GET /api/v1/historique/consultations`. Reste côté client : avis laissés
   (`AvisController` en écriture), demandes, réservations, commandes (point 6).
4. ✅ **Navigation adaptative** : « Mon espace » réservé au rôle `FOURNISSEUR` (§4).
   Au passage, `/api/v1/auth/refresh` réparé (table `revoked_tokens` manquante,
   `database/01_security/09_revoked_tokens.sql`), puis renouvellement automatique du
   jeton d'accès avant son expiration (§4).
5. **Vérification des espaces** (§8), en quatre étapes livrables séparément :
   - 5a. ✅ Base et règles métier : tables `verification_*`, rôle `AGENT_VERIFICATION`,
     machine à états dans `espace-service`, API complète (pro, agent, admin), stockage
     privé des justificatifs, tests (voir §8.10).
   - 5b. ✅ Côté professionnel : onglet « Vérification » de Mon espace (complétude,
     dépôt privé des justificatifs, demande, suivi, compléments).
   - 5c. ✅ Côté agent : `/verifications/...` (files, examen, décision motivée).
   - 5d. ✅ Côté admin : supervision (toutes les demandes, réattribution, annulation,
     révocation, statistiques, agents) — à intégrer au back-office du point 6.
   Reste : la page d'administration des règles de justificatifs (`justificatif_requis`),
   aujourd'hui modifiables en SQL uniquement.
6. **Back-office admin** : cadré au §9 (rôles administratifs, architecture, pages), en
   six étapes 6a à 6f.
7. **Messagerie, réservations, commandes** (client ET pro) : dépendent de
   `nexora-communication-service` et `nexora-commerce-service`, aujourd'hui non
   branchés au web du tout — chantier à part entière une fois 1-4 posés.

## 8. Vérification des espaces professionnels — processus métier

La vérification est un cas d'utilisation majeur, pas une case à cocher de
l'administration. Principe directeur : **une vérification ne fait jamais simplement
`verifie = true`**. Chaque demande, chaque document, chaque décision est conservé
pour que Nexora puisse toujours répondre à : *qui a vérifié cet espace, quand, avec
quels documents, quelle décision a été prise, et pourquoi ?*

### 8.1 Cas d'utilisation par acteur

| Professionnel | Agent de vérification | Administrateur |
|---|---|---|
| Créer son espace, renseigner les informations | Voir les demandes en attente | Configurer les règles (justificatifs requis par type d'espace) |
| Ajouter les justificatifs | Prendre une demande en charge | Gérer les agents (donner / retirer le rôle) |
| Demander la vérification | Consulter l'espace et le professionnel | Voir toutes les demandes |
| Suivre l'état de la demande | Contrôler identité, activité, adresse, coordonnées, documents | Réattribuer une demande |
| Compléter si des informations sont demandées | Demander des informations complémentaires | Annuler une demande |
| Consulter le résultat et le motif | Approuver, ou refuser avec un motif obligatoire | Révoquer une vérification (fraude constatée) |
| Retirer sa demande tant qu'elle n'est pas traitée | | Consulter l'historique, voir les statistiques |

Séparation des rôles, volontaire : **l'agent décide, l'administrateur supervise**.
L'admin n'approuve pas lui-même (sauf s'il reçoit aussi le rôle agent — les rôles sont
cumulables). Règle anti-conflit d'intérêts : **un agent ne peut jamais traiter la demande
d'un espace qui lui appartient**.

### 8.2 Cycle de vie d'une demande

```
 BROUILLON ──(le pro dépose ses justificatifs puis « Demander la vérification »)──┐
                                                                                  ↓
                                   ┌───────────────────────────────────────  EN_ATTENTE  ◄── « À traiter »
                   (retrait par le │                                             │ un agent la prend
                    pro / admin)   ↓                                             ↓
                                ANNULEE                                      EN_COURS
                                                                                 │
                        ┌────────────────────────────────┬───────────────────────┤
                        ↓                                ↓                       ↓
                   APPROUVEE                        A_COMPLETER              REFUSEE (motif)
                  ✓ espace vérifié            (motif : ce qui manque)        le pro pourra déposer
                        │                                │                   une NOUVELLE demande
                        │                   le pro complète et resoumet :
                        │                   retour EN_COURS chez le même agent
                        │                   (EN_ATTENTE s'il a été retiré)
                        ↓
                   REVOQUEE  (admin : fraude ; ou automatique si une information
                              vérifiée est modifiée, voir 8.4)
```

Deux choix par rapport au schéma initial, à valider :

- **À compléter → même demande, pas une nouvelle.** C'est la suite du même examen :
  l'agent garde le dossier et son historique. Une *nouvelle* demande n'est créée
  qu'après un refus (la demande refusée reste archivée telle quelle).
- **Une seule demande ouverte par espace** à la fois (contrainte en base).

Ce que voit le professionnel (et le public) est **déduit de la dernière demande** :

| Dernière demande | Affiché au professionnel | Badge public |
|---|---|---|
| aucune, `BROUILLON`, `ANNULEE` | NON VÉRIFIÉ + liste de complétude | — |
| `EN_ATTENTE`, `EN_COURS` | 🔎 Vérification en cours (reçue le …) | — |
| `A_COMPLETER` | ⚠ Informations demandées + motif de l'agent | — |
| `APPROUVEE` | ✓ Vérifié le … | ✓ Vérifié sur Nexora |
| `REFUSEE` | Refusée + motif, bouton « Nouvelle demande » | — |
| `REVOQUEE` | Vérification retirée + motif | — |

### 8.3 Vérifié ≠ certifié — on garde les deux notions

| | ✓ Vérifié | 🏅 Certifié |
|---|---|---|
| Sens | Nexora a contrôlé l'identité du responsable, l'existence de l'activité, l'adresse, les coordonnées et les documents fournis | Niveau supérieur, critères plus exigeants (entreprise formelle, établissement réglementé, partenaire) |
| Accessible au secteur informel | **Oui** — pièce d'identité + preuves d'activité et d'adresse suffisent | Non — NINEA, RCCM, autorisation d'exercer… |
| Stockage existant | colonnes `verifie`, `date_verification` | colonnes `certifie`, `date_certification` + table `certification` (organisme, numéro, date d'expiration) |
| Quand | chantier 5 | plus tard, **pas d'affichage « certifié » tant que les critères ne sont pas écrits** |

La table `certification` existante (organisme, numéro, document, date d'expiration)
correspond bien au 🏅 (un agrément, une licence délivrée par un organisme) : on la
garde pour ce niveau et on **ne la réutilise pas** pour les justificatifs de la
vérification, sinon les deux notions se mélangeraient dès la base.

### 8.4 Décisions techniques imposées par le code existant

1. **Le statut de l'espace ne sert pas à la vérification.** `statut_espace` contient
   déjà `EN_ATTENTE`, `EN_VERIFICATION`, `REFUSE`, mais la recherche filtre sur
   `ep.statut = 'ACTIF'` (`OffreRepositoryAdapter`, `vue_recherche_globale`) : un espace
   passé `EN_VERIFICATION` **disparaîtrait de la recherche** pendant l'examen, et un
   refus de vérification le ferait disparaître définitivement. Deux axes indépendants :
   - `espace_professionnel.statut` = visibilité / modération (`ACTIF`, `SUSPENDU`,
     `FERME`) ;
   - l'état de vérification = la dernière `verification_espace`.

   Les valeurs `EN_ATTENTE`/`EN_VERIFICATION`/`REFUSE` de l'enum restent inutilisées
   (PostgreSQL ne sait pas retirer une valeur d'enum sans recréer le type) ; elles ne
   resserviraient que si Nexora exigeait un jour la vérification *avant* publication.
2. **`verifie` reste, comme projection.** Le badge est déjà affiché partout (fiche
   espace, fiche offre, résultats, Mon espace) et la recherche a un filtre « vérifiés
   uniquement » (`espaceVerifie`). On garde la colonne pour ces lectures rapides, mais
   **seule la machine à états la modifie**, dans la même transaction que la décision
   (approbation → `true` ; révocation → `false`). Aucun endpoint ne permet de l'écrire
   directement — c'est déjà le cas : `UpdateEspaceRequest` ne contient ni `verifie` ni
   `certifie`.
3. **Les justificatifs ne passent PAS par `/uploads`.** Les images actuelles sont
   écrites par le web dans `nexora-uploads/` et servies **publiquement** sous
   `/uploads/**` (`StaticUploadsConfig`) : quiconque a l'URL les télécharge. Une copie
   de carte d'identité ne peut pas suivre ce chemin. Les justificatifs sont donc :
   - envoyés à `espace-service` (multipart via la gateway) et stockés dans un dossier
     **privé** (`NEXORA_JUSTIFICATIFS_DIR`), jamais exposé en statique ;
   - téléchargeables uniquement par un endpoint authentifié qui vérifie que le
     demandeur est le propriétaire de l'espace, l'agent de la demande ou un admin ;
   - limités (PDF, JPG, PNG ; 8 Mo), avec une empreinte SHA-256 enregistrée au dépôt
     (preuve du fichier exact qui a été examiné) ;
   - jamais supprimés quand le pro en dépose une nouvelle version (statut `REMPLACE`),
     pour que l'historique reste exact ; une durée de conservation après décision est
     à fixer (données personnelles : loi sénégalaise n° 2008-12, CDP).
4. **Modifier une information vérifiée retire le badge.** Sans cette règle, un espace
   pourrait être vérifié avec de vraies coordonnées puis changer de nom ou de numéro.
   Proposition : si un espace vérifié modifie son **nom**, son **téléphone
   principal**, son **adresse** ou ses **NINEA / RCCM**, la vérification passe
   `REVOQUEE` (acteur : système, motif : champ modifié) et `verifie = false`. Le
   formulaire prévient **avant** l'enregistrement (« Cette modification retirera le
   badge Vérifié ; vous pourrez redemander la vérification »). Les autres champs
   (description, photos, horaires…) ne touchent pas au badge.
5. **Le service propriétaire est `espace-service`.** La vérification appartient au
   contexte « Professionnel » : elle met à jour `espace_professionnel.verifie` dans la
   même transaction que la décision, sans appel inter-services. Pas de nouveau
   microservice ; `administration-service` n'est aujourd'hui qu'un schéma.
6. **Le professionnel est prévenu** par la table `notification` existante (le type
   `CERTIFICATION` existe déjà dans l'enum) : demande reçue, informations demandées,
   approuvée, refusée, révoquée.

### 8.5 Modèle de données proposé

Nouveaux scripts (dans `03_professional/`, ajoutés à `install.sql`, plus un script de
migration pour une base existante, et les `GRANT` à `nexora_user`) :

```
statut_verification (enum) : BROUILLON, EN_ATTENTE, EN_COURS, A_COMPLETER,
                             APPROUVEE, REFUSEE, ANNULEE, REVOQUEE

verification_espace ── une ligne par demande ; l'état courant
  id_verification      BIGSERIAL PK
  id_espace            → espace_professionnel (ON DELETE CASCADE)
  id_demandeur         → utilisateurs  (le propriétaire au moment de la demande)
  id_agent             → utilisateurs  (NULL tant que personne ne l'a prise)
  statut               statut_verification
  motif                TEXT            (dernier motif ; l'historique complet est dans les événements)
  date_creation, date_soumission, date_prise_en_charge, date_decision
  UNIQUE partiel : une seule demande ouverte par espace
      (statut IN BROUILLON, EN_ATTENTE, EN_COURS, A_COMPLETER)

verification_document ── les justificatifs d'une demande
  id_document          BIGSERIAL PK
  id_verification      → verification_espace (ON DELETE CASCADE)
  id_type_justificatif → type_justificatif
  nom_original, chemin_stockage (privé), type_mime, taille, empreinte_sha256
  statut               DEPOSE | ACCEPTE | REJETE | REMPLACE
  motif                (ex. « illisible », « expiré »)
  id_agent, date_depot, date_examen

verification_controle ── ce que l'agent a réellement contrôlé
  id_verification, code (IDENTITE | ACTIVITE | ADRESSE | COORDONNEES | DOCUMENTS),
  resultat (NON_FAIT | CONFORME | NON_CONFORME), commentaire, id_agent, date_controle
  UNIQUE (id_verification, code)

verification_evenement ── l'historique, jamais modifié ni supprimé
  id_evenement, id_verification,
  type (CREATION, DOCUMENT_DEPOSE, SOUMISSION, PRISE_EN_CHARGE, DOCUMENT_EXAMINE,
        INFOS_DEMANDEES, RESOUMISSION, APPROBATION, REFUS, ANNULATION,
        REATTRIBUTION, REVOCATION),
  ancien_statut, nouveau_statut, id_acteur (NULL = système), role_acteur,
  commentaire, date_evenement

type_justificatif ── référentiel (seedé, administrable)
  code, libelle, description, actif
  ex. PIECE_IDENTITE (CNI / passeport du responsable), JUSTIFICATIF_ADRESSE
      (facture Senelec / SEN'EAU, bail, certificat de résidence), PHOTO_DEVANTURE,
      NINEA, RCCM, AUTORISATION_EXERCICE (pharmacie, clinique, école…), DIPLOME, AUTRE

justificatif_requis ── les règles configurées par l'admin
  id_type_espace (NULL = tous les types), id_type_justificatif, obligatoire
```

Deux notions à ne pas confondre, et que les pages montrent différemment :

- **Complétude** (automatique) : le champ est-il rempli, le document requis est-il
  déposé ? Affichée au professionnel avant la demande ; le bouton « Demander la
  vérification » reste grisé tant qu'un élément obligatoire manque.
- **Contrôle** (humain) : l'agent a-t-il confirmé ce point ? Enregistré dans
  `verification_controle` ; l'approbation exige que chaque point soit `CONFORME`.

### 8.6 API (`espace-service`, via la gateway)

| Qui | Endpoint | Règle |
|---|---|---|
| Pro | `GET /api/v1/espaces/{id}/verification` | état, complétude, documents, historique — propriétaire uniquement |
| Pro | `POST /api/v1/espaces/{id}/verification/documents` (multipart) | dépôt privé ; demande `BROUILLON` créée au premier dépôt |
| Pro | `POST /api/v1/espaces/{id}/verification/soumettre` | complétude obligatoire respectée ; `BROUILLON`/`A_COMPLETER` → `EN_ATTENTE` |
| Pro | `POST /api/v1/espaces/{id}/verification/retirer` | seulement avant la prise en charge |
| Agent | `GET /api/v1/verifications?statut=` | `hasRole('AGENT_VERIFICATION')`, jamais ses propres espaces |
| Agent | `POST /api/v1/verifications/{id}/prendre` | `EN_ATTENTE` → `EN_COURS`, agent = soi |
| Agent | `PUT /api/v1/verifications/{id}/controles/{code}`, `.../documents/{doc}/examen` | agent assigné uniquement |
| Agent | `POST /api/v1/verifications/{id}/demander-infos` · `approuver` · `refuser` | motif obligatoire pour infos et refus |
| Pro / agent / admin | `GET /api/v1/verifications/{id}/documents/{doc}/fichier` | téléchargement contrôlé, jamais d'URL publique |
| Admin | `GET/POST /api/v1/admin/verifications/...` (liste complète, réattribuer, annuler, révoquer, statistiques ; `GET/POST/DELETE .../agents` pour les agents) | `hasRole('ADMIN')` ou `SUPER_ADMIN` |

Ce sont les **premiers endpoints de Nexora qui vérifient un rôle** (`hasRole`) : jusqu'ici
tout est `permitAll()` ou `authenticated()` (§1). Toute transition non prévue par le
schéma 8.2 est refusée (400, message explicite), et chaque transition écrit son `verification_evenement`
dans la même transaction.

### 8.7 Rôles et permissions

| Nouveau | Type | Attribué à |
|---|---|---|
| `AGENT_VERIFICATION` | rôle | comptes désignés par l'admin |
| `TRAITER_VERIFICATIONS` | permission | `AGENT_VERIFICATION` |
| `SUPERVISER_VERIFICATIONS` | permission | `ADMIN`, `SUPER_ADMIN` |
| `GERER_AGENTS_VERIFICATION` | permission | `ADMIN`, `SUPER_ADMIN` (sans leur donner `GERER_ROLES`, réservé au super admin) |
| `GERER_CERTIFICATIONS` (existante) | permission | gardée pour le 🏅, plus tard |

Pré-requis de test : le compte `admin@nexora.com` seedé a un mot de passe factice
(`REPLACE_THIS_WITH_BCRYPT_HASH`), il ne peut pas se connecter. Il faudra un compte
agent et un compte admin de démonstration avec un vrai hash.

### 8.8 Pages

**Professionnel — onglet « Vérification » de Mon espace**

- Avant la demande : liste de complétude (✓ Informations de l'espace, ✓ Coordonnées,
  ✓ Adresse, ⚠ Documents à fournir), dépôt des justificatifs requis pour **son** type
  d'espace, statut NON VÉRIFIÉ, bouton « Demander la vérification ».
- Après : 🔎 « Vérification en cours — demande reçue le … », historique simplifié.
- À compléter : le motif de l'agent, les documents rejetés à remplacer, « Renvoyer ».
- Résultat : ✓ Vérifié le … / refus avec motif et « Nouvelle demande ».
- La Vue d'ensemble affiche l'état de vérification en une ligne, avec un lien vers
  l'onglet.

**Agent — `/verification/...`**

- Files : À traiter (les plus anciennes d'abord ; « URGENT » au-delà d'un délai à
  fixer, ex. 72 h) · En cours (les siennes) · Informations demandées · Approuvées ·
  Refusées · Historique.
- Carte d'une demande : nom de l'espace, type, commune, responsable, date, statut,
  [Examiner].
- Page Examiner : Professionnel · Informations (nom, téléphone, e-mail, adresse +
  carte) · Documents (aperçu, Accepter / Rejeter avec motif) · Activité (type,
  catégories, description, offres, photos) · Contrôles (Conforme / Non conforme) ·
  Historique · [Demander des informations] [Refuser] [Approuver la vérification].

**Admin** — dans le back-office (chantier 6) : toutes les demandes avec filtres,
réattribution, annulation, révocation motivée, gestion des agents, règles
(`justificatif_requis`), statistiques (délai moyen de traitement, taux d'approbation,
demandes par agent).

### 8.9 Questions ouvertes (décisions du porteur de projet)

1. ✅ *Tranché : proposition par défaut retenue.* Justificatifs **obligatoires** pour
   ✓ Vérifié, par type d'espace. Proposition par
   défaut : pièce d'identité du responsable + justificatif d'adresse *ou* photo de la
   devanture pour tous ; autorisation d'exercer en plus pour pharmacie, clinique,
   école / université ; NINEA et RCCM facultatifs (réservés au 🏅).
2. Contrôle des coordonnées : l'agent appelle-t-il le numéro, ou envoie-t-on un code
   OTP au téléphone de l'espace (l'infrastructure OTP existe déjà pour les comptes) ?
3. Délai avant une nouvelle demande après un refus (proposition : aucun en V1).
4. Durée de validité d'une vérification (revérification tous les 24 mois ?) — hors V1.
5. Durée de conservation des pièces d'identité après la décision.

### 8.10 ✅ Étape 5a — ce qui est en place

**Base** (`database/`) :
- `03_professional/07_verification.sql` : enum `statut_verification` et les 6 tables du §8.5.
  Script **rejouable** : il sert à l'installation complète (inclus dans `install.sql`) comme à
  la mise à jour d'une base existante.
- `09_seed/21_verification.sql` (inclus dans `install.sql`) : rôle `AGENT_VERIFICATION`,
  3 permissions, justificatifs et règles par défaut (§8.9, question 1), `GRANT` à
  `nexora_user`, et **reprise de l'existant** : chaque espace déjà `verifie = true` reçoit une
  demande `APPROUVEE` avec l'événement `REPRISE`, pour que le badge corresponde toujours à une
  décision tracée.
- `09_seed/22_demo_verification.sql` (hors `install.sql`, comme `07_demo_espaces.sql`) :
  `agent.verification@nexora-demo.sn` (agent) et `admin.demo@nexora-demo.sn` (admin),
  mot de passe `Password1!`.

**`espace-service`** :
- `domain/verification/DemandeVerification` : la machine à états, sans dépendance technique.
- `application/service/verification/` : services professionnel, agent, administrateur,
  accès aux justificatifs, et `VerificationModificationService` (règle §8.4.4, appelée par
  `UpdateEspaceService`).
- `infrastructure/storage/StockageJustificatifs` : dossier privé
  (`NEXORA_JUSTIFICATIFS_DIR`, défaut `~/nexora-justificatifs`), type déduit du contenu
  (PDF, JPG, PNG ; un faux PDF est refusé), 8 Mo, empreinte SHA-256, protection contre les
  chemins hors du dossier.
- `SecurityConfig` : `/api/v1/verifications/**` exige `AGENT_VERIFICATION`,
  `/api/v1/admin/verifications/**` exige `ADMIN` ou `SUPER_ADMIN`.
- Au passage, `nexora-common` : un fichier trop volumineux renvoie 400 « Fichier trop
  volumineux » au lieu d'une erreur 500.

**Tests** :
- 19 tests unitaires (`mvn -pl nexora-espace-service test`) : machine à états, stockage,
  détection des modifications d'informations vérifiées.
- Scénario complet via la gateway, 52 contrôles, tous verts : dépôt, envoi, file de
  l'agent, prise en charge, rejet d'un document, demande d'informations, resoumission,
  contrôles, approbation, badge sur la fiche publique, notifications, retrait automatique du
  badge après changement de téléphone, supervision admin, et refus d'accès (tiers, rôles).

### 8.11 ✅ Étapes 5b, 5c, 5d — les pages

Mise en forme fonctionnelle, dans le style actuel : la refonte visuelle viendra ensuite.

**Professionnel** — onglet « Vérification » de `/mon-espace` (`VerificationEspaceBean`) :
- état en clair (non vérifié, en cours, informations demandées avec le motif, vérifié,
  refusé, retiré) ; rappel de l'état dans la navigation et la vue d'ensemble ;
- liste de complétude, justificatifs obligatoires et facultatifs pour **son** type
  d'espace, un bouton d'envoi par justificatif (`p:fileUpload`, PDF/JPG/PNG, 8 Mo),
  « Voir » et « Retirer » sur chaque document, motif affiché sous un document rejeté ;
- « Demander la vérification » (ou « Renvoyer mon dossier ») grisé tant qu'il manque un
  élément obligatoire ; « Retirer ma demande » avant la prise en charge ;
- historique simplifié et demandes précédentes ;
- onglet des informations : encadré d'avertissement si l'espace est vérifié, et
  confirmation avant d'enregistrer une modification du nom, du téléphone, de l'adresse,
  du NINEA ou du RCCM ; après coup, message « Badge Vérifié retiré ».

**Confidentialité côté professionnel** (règle ajoutée) : l'API ne lui renvoie ni le nom de
l'agent, ni ses notes de contrôle, ni les points bloquants internes, ni les
réattributions — protection des agents contre les pressions. Le motif d'un document
rejeté reste visible : c'est ce qu'il doit corriger.

**Agent** — `/verifications/index` (`FileVerificationBean`) et
`/verifications/examen?id=` (`ExamenVerificationBean`) : files de travail, fiche du
professionnel et de l'espace (carte OpenStreetMap, lien vers la fiche publique),
documents (voir, accepter, rejeter avec motif, empreinte SHA-256), cinq points de
contrôle, points bloquants, décision motivée, historique complet.

**Administrateur** — `/admin/verifications` (`SupervisionVerificationBean`) : chiffres
clés, toutes les demandes filtrables par statut, détail, réattribution (ou remise dans
la file), annulation et révocation motivées, gestion des agents par e-mail. Un agent
qui suit encore des demandes ne peut pas perdre son rôle avant leur réattribution. Le
rôle donné s'applique à la prochaine connexion de l'agent, ou au plus tard au
renouvellement de sa session (15 minutes).

**Téléchargement des justificatifs** : le web ne les stocke jamais. Le fichier passe
d'espace-service, qui a vérifié le droit d'accès, directement à la réponse HTTP
(`TelechargementJustificatif`), en `Cache-Control: private, no-store`.

**Tests** : parcours navigateur (Playwright) complet et croisé :
- dépôt et envoi par le professionnel ;
- prise en charge, rejet d'un document et demande d'informations par l'agent ;
- complément et renvoi par le professionnel ;
- contrôles et approbation ;
- badge sur la fiche publique ;
- retrait du badge après modification du téléphone ;
- supervision : agents, réattribution, annulation, révocation ;
- refus d'accès aux pages agent et admin, pour les visiteurs comme pour les autres rôles.

Le scénario API (53 contrôles) reste vert.

## 9. Back-office administrateur — conception

### 9.1 Ce qui existe, vérifié dans le code

| Domaine | Base | API | Web |
|---|---|---|---|
| Comptes | `accounts.enabled/locked` ; `utilisateurs.statut` (`statut_compte`), `compte_bloque`, `actif` | aucune route d'administration ; **la connexion et `/refresh` refusent déjà un compte `locked` ou désactivé** | — |
| Espaces | `statut_espace` (`ACTIF`, `SUSPENDU`, `FERME`…) ; la recherche ne montre que `ACTIF` | aucune route de modération | — |
| Offres | `statut_offre` (`PUBLIE`, `SUSPENDU`…) ; la recherche ne montre que `PUBLIE` | aucune route de modération | — |
| Catalogue | 735 catégories, 1 240 types d'offre, attributs, tags | **lecture seule** (`CategorieController`) | — |
| Avis | table `avis`, **aucune colonne de modération** | lecture publique | lecture sur la fiche espace |
| Signalements | table `signalement` (statut, traité par, commentaire) | `POST /api/v1/signalements` uniquement | **aucun bouton « Signaler »** |
| Vérification | tables `verification_*` | complète (§8) | `/admin/verifications` |
| Journal, paramètres, statistiques | tables `journal_action`, `parametre`, `statistique`, vue `vue_dashboard_admin` | `nexora-administration-service` n'est qu'un `pom.xml` vide | — |

Conséquence utile : **suspendre un compte revient à passer `accounts.locked` à vrai**. La
connexion est refusée, et comme le web renouvelle le jeton toutes les 15 minutes (§4), la
session d'un compte suspendu se ferme d'elle-même au plus tard 15 minutes après.

### 9.2 Qui fait quoi — les rôles administratifs

Les rôles existent déjà (`09_seed/01_roles.sql`) ; on leur donne un périmètre précis. Un
compte peut en cumuler plusieurs.

| Section du back-office | `SUPER_ADMIN` | `ADMIN` | `MODERATEUR` | `SUPPORT` | `GESTIONNAIRE` |
|---|---|---|---|---|---|
| Tableau de bord | ✅ | ✅ | ✅ (ses files) | ✅ (ses files) | ✅ |
| Utilisateurs : consulter | ✅ | ✅ | ✅ | ✅ | — |
| Utilisateurs : suspendre / réactiver | ✅ | ✅ | — | ✅ réactiver seulement | — |
| Rôles administratifs (donner / retirer) | ✅ **seul** | — | — | — | — |
| Espaces et offres : suspendre / réactiver | ✅ | ✅ | ✅ | — | — |
| Avis et signalements | ✅ | ✅ | ✅ | — | — |
| Vérifications : supervision, agents | ✅ | ✅ | — | — | — |
| Catalogue (catégories, types d'offre, attributs, tags) | ✅ | ✅ | — | — | ✅ |
| Paramètres, règles de justificatifs | ✅ | ✅ | — | — | ✅ |
| Statistiques | ✅ | ✅ | — | — | ✅ |
| Journal d'actions | ✅ | ✅ | — | — | — |

Ce tableau est **appliqué par des permissions** (§6.3), pas par des noms de rôles : il se
modifie depuis `/admin/roles` sans toucher au code.

`AGENT_VERIFICATION` reste hors du back-office (§8.1). Règles transverses :
- personne ne suspend son propre compte ni ne retire son propre rôle ;
- un compte `SUPER_ADMIN` ne peut être suspendu que par un autre `SUPER_ADMIN` ;
- **toute action du back-office est motivée et écrite dans `journal_action`** : qui, quoi,
  sur quelle entité, pourquoi, quand, depuis quelle adresse IP.

### 9.3 Architecture

- **Chaque service administre son propre domaine**, sous `/api/v1/admin/**`, avec
  `hasAnyRole(...)` selon le tableau 9.2 :
  - `auth-service` : les comptes et les rôles ;
  - `espace-service` : les espaces et la vérification (déjà fait) ;
  - `catalogue-service` : les offres et le catalogue ;
  - `recherche-service` : les avis et les signalements.
- **`nexora-administration-service` devient réel** pour ce qui est transverse : tableau de
  bord (compteurs lus sur la base partagée), lecture du journal, paramètres.
- **Journal d'actions** : un composant partagé dans `nexora-common`
  (`sn.ucad.nexora.common.audit.JournalActions`). Chaque service l'appelle dans la
  transaction de l'action : si l'action échoue, rien n'est journalisé, et inversement.
- **Web** : un layout dédié `WEB-INF/templates/admin.xhtml` (barre latérale, sans le
  header public), toutes les pages sous `/admin/...`. Le menu n'affiche que les sections
  permises par les rôles du compte. Chaque page est protégée par une garde de
  `SessionBean`. Un lien « Administration » apparaît dans le menu public pour les comptes
  administratifs.
- Le badge « Vérifié », la visibilité dans la recherche, etc. ne changent **que** par
  ces actions motivées. Aucune page n'édite directement une colonne d'état.

### 9.4 Pages

| Page | Route | Contenu |
|---|---|---|
| Tableau de bord | `/admin/index` | compteurs (comptes, espaces, offres, espaces vérifiés), files en attente (vérifications, signalements), dernières actions du journal |
| Utilisateurs | `/admin/utilisateurs`, `/admin/utilisateur?id=` | recherche (nom, e-mail, téléphone), filtres (rôle, suspendu) ; fiche : rôles, espaces, dates, historique ; suspendre / réactiver (motif) ; rôles administratifs |
| Espaces | `/admin/espaces` | recherche, statut, vérifié ; suspendre / réactiver (motif, notification au professionnel) |
| Offres | `/admin/offres` | recherche ; suspendre / republier (motif) |
| Signalements | `/admin/signalements` | file `EN_ATTENTE` ; traiter (avec action liée : suspendre l'offre ou l'espace) ou rejeter, avec commentaire |
| Avis | `/admin/avis` | liste, masquer / rétablir (motif) |
| Vérifications | `/admin/verifications` | existe (§8.11), passe dans le layout admin |
| Catalogue | `/admin/catalogue` | arbre des catégories, types d'offre, attributs, tags : créer, renommer, désactiver (jamais supprimer ce qui est utilisé) |
| Paramètres | `/admin/parametres` | paramètres Nexora, règles de justificatifs par type d'espace |
| Journal | `/admin/journal` | toutes les actions, filtrables par module, auteur, entité, date |

### 9.5 Évolutions de la base

- `avis` : colonnes de modération (`masque`, `motif_moderation`, `modere_par`,
  `date_moderation`). Un avis masqué disparaît de la fiche publique et de la note moyenne.
- `espace_professionnel` / `offre` : motif, date et auteur de la dernière décision de
  modération (`motif_moderation`, `date_moderation`, `id_moderateur`). Tranché à l'étape 6c :
  des colonnes plutôt qu'une lecture du journal, pour que le professionnel voie le motif (§9.9).
- Rien d'autre : `journal_action`, `parametre` et `signalement` ont déjà les colonnes
  nécessaires.

### 9.6 Étapes

1. ✅ **6a — Fondations** (§9.7) :
   - `administration-service` opérationnel, avec tableau de bord et journal ;
   - `JournalActions` partagé, et la supervision des vérifications journalisée ;
   - layout admin et menu par rôle ;
   - pages Tableau de bord et Journal ;
   - supervision des vérifications déplacée dans le layout admin.
2. ✅ **6b — Utilisateurs** (§9.8) : recherche, fiche, suspension / réactivation, rôles
   administratifs, et matrice rôles × permissions (§6).
3. ✅ **6c — Espaces et offres** (§9.9) : modération (suspendre / réactiver, motif,
   notification).
4. ✅ **6d — Signalements et avis** (§9.10) : bouton « Signaler » côté public, file de
   traitement, masquage des avis.
5. **6e — Catalogue** : gestion des catégories, types d'offre, attributs et tags.
6. **6f — Paramètres** : paramètres Nexora et règles de justificatifs de la vérification.

Chaque étape est testée de bout en bout, comme le chantier 5, avant de passer à la
suivante. Les pages suivent le style actuel ; la refonte visuelle viendra après.

### 9.7 ✅ Étape 6a — fondations en place

**`nexora-administration-service`** (port 8086, route Gateway `/administration-service/**`) :
- `GET /api/v1/admin/tableau-de-bord` (tous les rôles du back-office) : compteurs lus en
  direct (comptes, suspendus, nouveaux sur 30 jours, espaces actifs / suspendus /
  vérifiés, offres publiées / suspendues, vérifications en attente et en cours,
  signalements en attente, avis), plus les dernières actions du journal pour
  `ADMIN` / `SUPER_ADMIN` ;
- `GET /api/v1/admin/journal?module=&recherche=&page=` (`ADMIN`, `SUPER_ADMIN`) : 50
  actions par page, les plus récentes d'abord ;
- toute autre route est refusée (`denyAll`).

**Journal d'actions** — `sn.ucad.nexora.common.audit.JournalActions` (nexora-common,
auto-configuré dans tous les services) :
- écrit dans `journal_action` **dans la transaction de l'action** (`MANDATORY`) : une
  action refusée ou en échec ne laisse aucune trace, une action réussie en laisse
  toujours une ;
- l'auteur est retrouvé depuis le compte du JWT ;
- l'adresse IP est celle du navigateur, transmise par le web dans l'en-tête
  `X-Nexora-Client-IP` (`EnTetesClient`, ajouté à tous les clients d'API du web).
- Déjà journalisés : réattribution, annulation, révocation d'une vérification, ajout et
  retrait d'un agent. Nouvelle règle appliquée : on ne peut pas se retirer son propre rôle
  d'agent.

**Web** :
- layout `admin.xhtml` : barre latérale par rôle, compte et rôles en clair, retour au
  site, déconnexion ;
- `AccesAdminBean` (`#{acces.*}`) : qui voit quoi, et gardes des pages. Un visiteur va
  vers la connexion, un compte sans le rôle vers l'accueil ;
- pages `/admin/index` (tableau de bord) et `/admin/journal` (filtres module et
  recherche, pagination) ; `/admin/verifications` passe dans ce layout.

**Comptes de démonstration** (`09_seed/23_demo_administration.sql`, hors `install.sql`,
mot de passe `Password1!`) : `superadmin.demo`, `moderateur.demo`, `support.demo`,
`gestionnaire.demo` @nexora-demo.sn ; `admin.demo` existe déjà (22).

**Tests** :
- API, 22 contrôles : accès au tableau de bord et au journal pour chacun des 7 rôles,
  exactitude des compteurs, journalisation avec l'IP, rien de journalisé pour une action
  refusée, filtres du journal ;
- navigateur : menu et gardes de chaque rôle, du visiteur au super administrateur,
  tableau de bord, supervision dans le nouveau layout, journal filtré ;
- non-régression : scénario de vérification (53 contrôles) et navigation publique.

### 9.8 ✅ Étape 6b — utilisateurs, rôles et permissions

**auth-service** (propriétaire des comptes, rôles et permissions), une permission par route :

| Route | Permission |
|---|---|
| `GET /api/v1/admin/comptes?recherche=&role=&etat=&page=` | `CONSULTER_UTILISATEURS` |
| `GET /api/v1/admin/comptes/{id}` (fiche : rôles, permissions effectives, espaces, dernière connexion, historique) | `CONSULTER_UTILISATEURS` |
| `POST /api/v1/admin/comptes/{id}/suspendre` | `SUSPENDRE_UTILISATEURS` |
| `POST /api/v1/admin/comptes/{id}/reactiver` | `REACTIVER_UTILISATEURS` |
| `POST /api/v1/admin/comptes/{id}/roles/{rôle}` et `.../retirer` | `GERER_ROLES` |
| `GET /api/v1/admin/roles` (matrice) | `ACCEDER_BACK_OFFICE` |
| `POST /api/v1/admin/roles/{rôle}/permissions/{permission}` et `.../retirer` | `GERER_PERMISSIONS` |

- **Suspension** : `accounts.locked` passe à vrai, et le profil suit (`utilisateurs.statut =
  SUSPENDU`, `compte_bloque`). La connexion et `/refresh` sont refusés, donc la session web du
  compte se ferme au plus tard 15 minutes après. La réactivation fait l'inverse.
- **Rôles donnés ici** : uniquement les rôles administratifs. `UTILISATEUR`, `FOURNISSEUR` et
  `AGENT_VERIFICATION` suivent leur propre cycle (§6.1).
- Règles métier : motif obligatoire, jamais sur son propre compte. Un admin ne suspend pas un
  super admin, on ne suspend pas le dernier super admin actif et on ne lui retire pas son
  rôle, et les permissions vitales du rôle `SUPER_ADMIN` sont protégées. Toutes ces actions
  sont journalisées (modules `COMPTES` et `PERMISSIONS`) et visibles dans l'historique de la
  fiche.

**Web** :
- `/admin/utilisateurs` : recherche par nom, e-mail ou téléphone, filtres par rôle et par état,
  pagination ;
- `/admin/utilisateur?id=` : fiche avec les rôles, les permissions effectives, les espaces et
  l'historique. Les actions (motif obligatoire) n'apparaissent que si le compte a la
  permission correspondante, et jamais sur sa propre fiche ;
- `/admin/roles` : les 8 rôles (nombre de comptes, lien vers la liste filtrée) et la matrice
  permissions × rôles groupée par module. Elle se lit depuis tout le back-office ; avec
  `GERER_PERMISSIONS`, chaque case s'active ou se désactive d'un clic.

**Tests** :
- 7 tests unitaires des règles (auth-service) et 2 du helper d'autorités (nexora-common) ;
- API, 45 contrôles, rejouables :
  - contenu du jeton, droits de chaque rôle, recherche et filtres ;
  - suspension (connexion refusée), réactivation par le support ;
  - rôles donnés et retirés, arrivée des permissions dans le jeton ;
  - garde-fous ;
  - une permission accordée au support puis retirée **sans changer le code** ;
  - jeton de rafraîchissement refusé ;
  - journalisation complète ;
- navigateur : liste, filtres, fiche, suspension et réactivation, rôles, matrice, vues du
  support et du gestionnaire, sa propre fiche ;
- non-régression complète : vérification (API et navigateur), tableau de bord, journal,
  navigation publique ; installation complète de la base.

Donnée corrigée au passage : les comptes créés en SQL (administrateur initial,
démonstration) n'avaient pas le rôle `UTILISATEUR`. Le script 24 le leur donne.

### 9.9 ✅ Étape 6c — modération des espaces et des offres

**Règles**
- Deux décisions seulement, toujours motivées : **suspendre** (`ACTIF → SUSPENDU` pour un
  espace, `PUBLIE → SUSPENDU` pour une offre) et **réactiver / republier** (retour à l'état
  visible). Aucune autre transition ici.
- **Ce que change une suspension** :
  - l'espace ou l'offre disparaît de la recherche, de l'accueil et de la carte ;
  - sa fiche publique répond « introuvable ». Suspendre un espace masque aussi toutes ses
    offres, sans changer leur statut ; elles reviennent avec lui.
- **Le professionnel est prévenu** :
  - une notification (`AVERTISSEMENT` à la suspension, `SUCCES` au retour) ;
  - dans Mon espace, un bandeau pour l'espace et un badge « Suspendue » sur l'offre, avec le
    motif ;
  - il voit toujours sa fiche et toutes ses offres, et peut les modifier pour corriger ce
    qui est signalé. Mais **seule la modération rend visible** : aucune modification du
    professionnel ne change le statut.
- On ne modère **jamais son propre espace ni ses propres offres** (refusé par le service, et
  masqué dans l'interface).
- Chaque décision est journalisée (module `MODERATION`, entité `espace` ou `offre`, adresse
  IP), et la notification est écrite dans la même transaction : pas de décision sans trace,
  pas de trace sans décision.

**API**

| Route | Service | Permission |
|---|---|---|
| `GET /api/v1/admin/espaces?recherche=&statut=&verifie=&page=` | espace-service | `MODERER_ESPACES` |
| `GET /api/v1/admin/espaces/{id}` (fiche : propriétaire, offres, historique espace et offres) | espace-service | `MODERER_ESPACES` |
| `POST /api/v1/admin/espaces/{id}/suspendre` et `.../reactiver` `{motif}` | espace-service | `MODERER_ESPACES` |
| `GET /api/v1/admin/offres?recherche=&statut=&idEspace=&page=` | catalogue-service | `MODERER_OFFRES` |
| `POST /api/v1/admin/offres/{id}/suspendre` et `.../republier` `{motif}` | catalogue-service | `MODERER_OFFRES` |
| `GET /api/v1/offres/gestion?idEspace=` : toutes les offres d'un espace, statut et motif compris | catalogue-service | propriétaire de l'espace |

Changements de comportement :
- `GET /api/v1/espaces/{id}` et `GET /api/v1/offres/{id}` restent publics, mais un espace non
  actif, une offre non publiée ou une offre d'un espace suspendu ne sont servis qu'à leur
  propriétaire et aux modérateurs (jeton facultatif) ;
- `GET /api/v1/espaces/me` porte le motif de modération.

Mon espace utilise désormais la liste de gestion au lieu de la recherche publique. Cela
corrige aussi un défaut ancien : un espace marqué « fermé » par son propriétaire voyait ses
offres disparaître de sa propre page.

**Composants partagés**
- `nexora-common` : `Notifications`, à côté de `JournalActions`, écrit dans la transaction de
  l'action qui la motive.
- Le gestionnaire d'erreurs du catalogue traduit désormais les exceptions métier en 400 /
  403 / 404 au lieu de 500.

**Base** : `database/03_professional/08_moderation.sql`, rejouable, dans `install.sql` après
le catalogue.

**Web**
- `/admin/espaces` : recherche par nom de l'espace, nom ou e-mail du propriétaire ; filtres
  par statut et par vérification.
- `/admin/espace?id=` : informations, propriétaire (lien vers sa fiche utilisateur), offres
  avec leurs actions, décision motivée, historique.
- `/admin/offres` : recherche par titre ou par espace, filtre par statut, suspension et
  republication depuis la ligne.
- Menu : Espaces et Offres selon `MODERER_ESPACES` / `MODERER_OFFRES`. Au tableau de bord, les
  compteurs « suspendus » mènent aux listes filtrées.

**Tests**
- API, 64 contrôles rejouables (`test_6c.py`) :
  - droits de chaque rôle, recherche et filtres ;
  - motif obligatoire, transitions interdites ;
  - visibilité pour l'anonyme, un autre utilisateur, le propriétaire et le modérateur ;
  - disparition et retour dans la recherche, liste de gestion ;
  - notifications, journal et historique ;
  - refus de modérer son propre espace ou ses propres offres ;
- navigateur : menus selon le rôle, parcours du modérateur (espace puis offre), bandeau et
  badge côté professionnel, fiche publique introuvable ;
- non-régression : vérification, 6a, 6b (API et navigateur), session, installation complète
  de la base.

### 9.10 ✅ Étape 6d — avis et signalements

**Avis**
- Écrire un avis : tout compte connecté, depuis la fiche d'un espace : une note de 1 à 5 et un
  commentaire facultatif (1 000 caractères au plus). **Un seul avis par personne et par
  espace**, même masqué : on ne contourne pas la modération en en publiant un autre. Pas
  d'avis sur son propre espace, ni sur un espace qui n'est pas visible.
- L'auteur est affiché par son prénom et l'initiale de son nom (« Moussa D. »).
- **La note d'un espace est une projection** : la moyenne de ses avis visibles, sur l'espace et
  sur ses offres. Elle est recalculée à chaque avis publié, supprimé, masqué ou rétabli.
  Auparavant, la publication d'un avis ne changeait pas la note. Les notes du jeu de
  démonstration étaient inventées : `09_seed/25_demo_avis.sql` (hors installation) crée
  18 avis réels et recalcule toutes les notes.
- Modération (`MODERER_AVIS`) : **masquer** ou **rétablir**, avec un motif.
  - Un avis masqué disparaît de la fiche publique et de la note.
  - Son auteur reçoit une notification et voit, sur la fiche, que son avis a été masqué et
    pourquoi.
  - On ne modère ni son propre avis, ni un avis sur son propre espace.

**Signalements**
- **« Signaler »** sur la fiche d'un espace, d'une offre et sur chaque avis. Il faut être
  connecté ; après la connexion, on revient au formulaire, paramètres compris. Cette
  redirection vaut désormais pour tout le site, et n'accepte qu'une page de Nexora.
- Six motifs : arnaque, informations fausses, contenu inapproprié, lieu inexistant ou fermé,
  avis faux, autre (description alors obligatoire).
- On ne signale pas son propre contenu, ni deux fois le même élément tant que le premier
  signalement est ouvert.
- Le signalement est confidentiel : le professionnel ne sait pas qui l'a envoyé.
- File de traitement (`GERER_SIGNALEMENTS`) :
  - `EN_ATTENTE` → prise en charge (`EN_COURS`, au nom du modérateur) ;
  - puis clôture motivée : **traité** (une suite a été donnée) ou **rejeté** (rien de
    contraire aux règles) ;
  - la personne qui a signalé est prévenue de l'issue, sans le détail de la sanction ;
  - on ne traite pas un signalement qui vise son propre contenu.
- La sanction elle-même passe par la modération existante : masquer l'avis (directement depuis
  le signalement), suspendre l'espace ou l'offre (lien vers la fiche de l'espace, §9.9).

**API** (recherche-service)

| Route | Accès |
|---|---|
| `GET /api/v1/avis/espace/{id}`, `/offre/{id}`, `/{id}` (avis visibles, avec l'auteur) | public |
| `POST /api/v1/avis`, `DELETE /api/v1/avis/{id}` (le sien), `GET /api/v1/avis/mien?espaceId=` | connecté |
| `GET /api/v1/signalements/motifs` | public |
| `POST /api/v1/signalements` `{espaceId | offreId | avisId, motif, description}` | connecté |
| `GET /api/v1/admin/avis?recherche=&etat=&idEspace=&page=`, `POST .../{id}/masquer`, `.../retablir` | `MODERER_AVIS` |
| `GET /api/v1/admin/signalements?statut=&type=&page=`, `GET .../{id}`, `POST .../{id}/prendre`, `.../traiter`, `.../rejeter` | `GERER_SIGNALEMENTS` |

Journal : modules `AVIS` et `SIGNALEMENTS`. Les exceptions métier de recherche-service
donnent 400 / 404 au lieu de 500.

**Base** : `05_search/07_moderation_avis_signalements.sql`, rejouable : colonnes de modération
de `avis` et `signalement.id_avis`.

**Web**
- Fiche espace : avis avec étoiles, auteur et date ; formulaire « Votre avis » ; liens
  « Signaler » ; bandeau pour l'auteur d'un avis masqué.
- Fiche offre : « Signaler cette offre ».
- `/signaler?espace=|offre=|avis=` : le formulaire de signalement.
- Back-office :
  - `/admin/signalements` : file, filtrée par défaut sur « en attente » ;
  - `/admin/signalement?id=` : l'élément signalé, les précisions, le nombre de
    signalements sur cet élément, et les décisions ;
  - `/admin/avis` : recherche, masquer et rétablir.
- Menu selon les permissions ; au tableau de bord, la tuile des signalements mène à la file.

**Tests**
- API, 63 contrôles rejouables (`test_6d.py`) :
  - publication et règles, note recalculée à chaque étape ;
  - dépôt et règles des signalements ;
  - droits de chaque rôle, prise en charge, clôtures ;
  - masquage et rétablissement ;
  - notifications, journal, conflits d'intérêts, suppression par l'auteur ;
- navigateur :
  - un visiteur signale : il passe par la connexion puis revient au formulaire ;
  - publication d'un avis, et signalement de cet avis par le professionnel ;
  - dans la file : prise en charge, masquage, clôture ;
  - disparition de l'avis et retour de la note ;
- test unitaire de la redirection après connexion (jamais vers une adresse externe) ;
- non-régression complète : vérification, 6a, 6b, 6c, session (API et navigateur).

---
*Dernière mise à jour : session du 30/09/2026, avis et signalements livrés (étape 6d).*

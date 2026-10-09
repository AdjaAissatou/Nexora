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
| Mon profil (infos personnelles) | `/mon-compte` | ✅ séparé de la gestion pro (voir §5) ; en-tête avec avatar (initiales), « membre depuis », compteurs favoris / vus / espaces ; liste de tous mes espaces (vues, ♥, note, Gérer `?id=` / Voir la fiche) et raccourcis Découvrir · Rechercher · Explorer · Lieux |
| Mes favoris | `/mon-compte` (section) | ✅ bouton ♡/♥ sur les fiches offre et espace ; dans Mon compte, grille de cartes avec photo (espace : couverture, sinon une de ses photos, son logo ou la photo d'un de ses articles ; repli 🛍️/🏪), prix ou slogan, espace et lieu, « ajouté il y a … », filtre Tout / Articles / Espaces, retrait par le ♥ |
| Mon historique de consultation | `/mon-compte` (section) | ✅ enregistré à l'ouverture d'une fiche offre/espace, dédoublonné, effaçable ; affiché en bande « Vus récemment » défilante avec vignettes et « vu il y a … » |
| Mes demandes / messages | `/mon-compte/messages` | 🚧 (tables `conversation`/`message` existent, service `nexora-communication-service` non branché au web) |
| Mes réservations | `/mon-compte/reservations` | 🚧 (table `reservation` existe) |
| Mes commandes | `/mon-compte/commandes` | 🚧 (tables `commande`/`sous_commande` existent) |
| Mes avis laissés | `/mon-compte` (section « Mes avis ») | ✅ masqués compris (motif), réponse du pro, modifier (fiche espace) et supprimer (§22) |
| Notifications | `/mon-compte/notifications` | 🚧 (table `notification` existe) |
| Sécurité / paramètres du compte | `/securite` | ✅ mot de passe, email de connexion, appareils connectés, historique (§25) |

### Professionnel (connecté, rôle `FOURNISSEUR`)

| Page | Route | État |
|---|---|---|
| Mon espace — vue d'ensemble + gestion | `/mon-espace` | ✅ (tableau de bord refait récemment : stats, vocabulaire dynamique par type d'espace, cartes d'offres) |
| Gérer les offres (vocabulaire dynamique) | `/mon-espace` (onglet) | ✅ |
| Créer / modifier une offre | `/creer-offre` | ✅ ; « Autre… » en bas des catégories pour écrire la sienne (§21) |
| Créer un espace | `/creer-espace` | ✅ |
| Gérer les informations de l'espace (horaires, contact, localisation, photos) | `/mon-espace` (onglet) | ✅ pour l'essentiel — **horaires** (table `horaire`) et **moyens de contact** (table `moyen_contact`) non encore dans le formulaire |
| Demandes reçues | — | 🚧 (dépend de la messagerie, non branchée) |
| Réservations reçues | — | 🚧 |
| Commandes reçues | — | 🚧 |
| Avis reçus + réponse | `/mon-espace` onglet « Avis reçus » (`#avis`) | ✅ synthèse des notes, filtres, réponse publique modifiable (§22) |
| Statistiques détaillées (au-delà de la Vue d'ensemble) | — | ✅ onglet « Statistiques » de Mon espace : évolution par semaine, contacts par canal, tableau par offre (§26) |
| Vérification de mon espace (dossier, justificatifs, suivi) | `/mon-espace` (onglet « Vérification ») | ✅ (§8.11) |
| Certification 🏅 | — | — (après la vérification, critères à définir, §8.3) |
| Ma fiche publique | `/espace?id=` (lien depuis Mon espace) | ✅ |

### Administrateur

Back-office complet, cadré au §9 (étapes 6a à 6f) : layout dédié, menu filtré par permission.

| Page | État |
|---|---|
| Tableau de bord admin | ✅ `/admin/index` (§9.7) |
| Gérer les utilisateurs (rechercher, suspendre, réactiver, rôles) | ✅ `/admin/utilisateurs`, `/admin/utilisateur?id=` (§9.8) |
| Rôles et permissions (matrice) | ✅ `/admin/roles` (§6, §9.8) |
| Gérer/modérer les espaces et les offres | ✅ `/admin/espaces`, `/admin/espace?id=`, `/admin/offres` (§9.9) |
| Gérer le catalogue (catégories, types d'offre, attributs) | ✅ `/admin/catalogue` (§9.11) |
| Traiter les catégories proposées (« Autre… ») | ✅ `/admin/propositions` (§21) |
| Modérer avis / traiter signalements | ✅ `/admin/avis`, `/admin/signalements` (§9.10) |
| Vérifications : supervision, agents (§8) | ✅ `/admin/verifications` ; règles des justificatifs dans `/admin/parametres` (§9.12) |
| Certifications 🏅 | — (après la vérification) |
| Statistiques plateforme | 🚧 |
| Journal d'activité | ✅ `/admin/journal` (§9.7) |
| Paramètres Nexora | ✅ `/admin/parametres` (§9.12) |

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
5. ✅ **6e — Catalogue** (§9.11) : gestion des catégories, types d'offre, attributs et valeurs.
6. ✅ **6f — Paramètres** (§9.12) : paramètres Nexora et règles de justificatifs de la vérification.

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

### 9.11 ✅ Étape 6e — gestion du catalogue

**Ce qui se gère** : l'arbre des 735 catégories, les 1 240 types d'offre, les attributs d'une
catégorie (taille, couleur, marque…) et les valeurs proposées des attributs « liste ». Pour
chaque catégorie racine, on choisit aussi les types d'espace auxquels elle est proposée : ce
que voit un restaurant, une boutique… quand il ajoute une offre. Les tags ne sont gérés nulle
part : aucune offre ne les utilise.

**Règles**
- On **crée**, on **modifie** (renommer, décrire, ordonner, couleur #RRGGBB, icône), on
  **désactive** ou **réactive** (avec un motif).
- On ne **supprime** (avec un motif) que ce qui n'a jamais servi :
  - une catégorie sans sous-catégorie, type d'offre, attribut ni offre ;
  - un type d'offre sans offre ;
  - un attribut, ou une valeur, qu'aucune offre n'a renseigné.
  Pour le reste, on désactive.
- **Désactiver** retire l'élément, et pour une catégorie toute sa branche, du parcours de
  création d'offre. Les offres existantes restent intactes et visibles, dans la recherche comme
  sur leur fiche.
- Noms uniques là où il le faut : une catégorie parmi ses sœurs, un type d'offre ou un attribut
  dans sa catégorie, une valeur dans son attribut. La casse n'est pas prise en compte.
- Le type de champ d'un attribut déjà renseigné par des offres ne change plus. Le code technique
  d'un attribut est dérivé de son nom (« Taille d'écran » → `taille_d_ecran`) et reste unique.
- Tout est journalisé (module `CATALOGUE`), avec le chemin complet de la catégorie concernée.

**Permissions** : une par nature d'élément, conformément à la matrice (§6.3) :
- `GERER_CATEGORIES` : catégories et liens aux types d'espace ;
- `GERER_TYPES_OFFRES` : types d'offre ;
- `GERER_ATTRIBUTS` : attributs et valeurs ;
- la lecture demande l'une des trois.

Le test le vérifie en retirant `GERER_ATTRIBUTS` au gestionnaire : il crée toujours des
catégories et des types d'offre, mais plus d'attributs.

**API** (catalogue-service, `/api/v1/admin/catalogue`) :
- `GET /categories` (les racines) ;
- `GET /categories/{id}` : la fiche, avec son chemin, ses sous-catégories, types, attributs,
  valeurs, types d'espace et compteurs d'offres (branche comprise) ;
- `GET /recherche?q=` : catégories et types d'offre, avec leur chemin ;
- `POST /categories`, `PUT /categories/{id}`, `POST /categories/{id}/activer|desactiver|supprimer` ;
- `POST|…/types-espace/{te}[/retirer]` ;
- `POST /categories/{id}/types`, `PUT /types/{id}`, `POST /types/{id}/activer|desactiver|supprimer` ;
- la même chose pour `/attributs` et pour `/valeurs`.

Les corps JSON illisibles ou incomplets renvoient désormais 400, au lieu de 500.

**Web** : `/admin/catalogue[?id=]`.
- En haut : la recherche, le fil d'Ariane, et un seul champ « motif » pour les actions sensibles.
- Sans id : les catégories racines, avec leurs compteurs.
- Avec id, la fiche en deux colonnes :
  - à gauche : les sous-catégories, les types d'offre (ajout, modification sur place), les
    attributs avec leurs valeurs en pastilles ;
  - à droite : la catégorie elle-même, et ses types d'espace s'il s'agit d'une racine.
- Chaque bouton n'apparaît qu'avec la permission correspondante.
- Un avertissement s'affiche si une catégorie parente est désactivée.

**Tests**
- API, 54 contrôles rejouables (`test_6e.py`) : droits, création et règles d'unicité,
  désactivation et effet sur le parcours public, protections des éléments utilisés, types
  d'espace, suppressions, permissions séparées, journal (24 actions attendues) ;
- navigateur : parcours complet du gestionnaire, nettoyage compris par l'interface ;
- non-régression complète.

### 9.12 ✅ Étape 6f — paramètres et justificatifs de vérification

**Paramètres de Nexora** (table `parametre`, seed `27_parametres.sql`). Seuls les paramètres
connus du code se modifient, chacun avec ses bornes :

| Code | Rôle | Valeur par défaut | Lu par |
|---|---|---|---|
| `SITE_BANDEAU` | bandeau d'annonce en haut des pages publiques (vide : aucun) | vide | web |
| `SITE_CONTACT_EMAIL` | e-mail du pied de page | contact@nexora.sn | web |
| `SITE_CONTACT_TELEPHONE` | téléphone du pied de page | 33 800 00 00 | web |
| `INSCRIPTIONS_OUVERTES` | non : l'inscription est refusée (la connexion reste possible) | oui | auth-service, web |
| `ESPACES_MAX_PAR_COMPTE` | espaces qu'un compte peut créer (1 à 50) | 5 | espace-service |
| `AVIS_LONGUEUR_MAX` | caractères d'un commentaire d'avis (100 à 5000) | 1000 | recherche-service, web |
| `VERIFICATION_DELAI_JOURS` | délai annoncé au pro pour sa vérification (1 à 60) | 5 | web |

- Les services lisent la valeur à chaque usage (`Parametres` de nexora-common, avec la valeur
  par défaut si la ligne manque) : pas de redémarrage.
- Le web lit `GET /api/v1/public/parametres` (sans jeton, sans les paramètres internes) et garde
  les valeurs une minute (`SiteBean`, `#{site.…}`).
- Chaque modification demande un motif, est refusée si la valeur n'a pas changé, et est
  journalisée (module `PARAMETRES`) : « libellé » : ancienne → nouvelle (motif).

**Justificatifs de vérification** (tables `type_justificatif`, `justificatif_requis`, §8) :
- types : créer (code dérivé du libellé), modifier, désactiver ou réactiver avec un motif. On
  ne désactive pas un type encore demandé par une règle ;
- règles : générales (tous les espaces) ou propres à un type d'espace ; obligatoire ou
  facultative ; un *groupe* rend des justificatifs interchangeables (un seul suffit) ;
- gardes : pas de doublon entre une règle générale et une règle de type sur le même
  justificatif ; pas de règle sur un type désactivé ; il reste toujours au moins un justificatif
  obligatoire pour tous les espaces ;
- effet immédiat : le dossier de vérification du professionnel suit les règles en vigueur.

**Permission** : `GERER_PARAMETRES` (super administrateur, administrateur, gestionnaire).

**API**
- administration-service : `GET /api/v1/public/parametres`, `GET /api/v1/admin/parametres`,
  `PUT /api/v1/admin/parametres/{code}` `{valeur, motif}` ;
- espace-service, `/api/v1/admin/justificatifs` : `GET`, `POST /types`, `PUT /types/{id}`,
  `POST /types/{id}/activer|desactiver`, `POST /regles`, `PUT /regles/{id}`,
  `POST /regles/{id}/supprimer`.

**Web**
- `/admin/parametres`, deux onglets : les paramètres par catégorie (valeur, motif,
  enregistrer) ; les justificatifs (règles générales et celles d'un type d'espace choisi,
  ajout, modification, retrait ; liste des types avec leur usage) ;
- site public : bandeau, contact dans le pied de page, page d'inscription fermée, longueur
  maximale du champ d'avis, délai annoncé dans l'onglet Vérification de Mon espace.

**Tests**
- API, 72 contrôles (`test_6f.py`) : droits, validation de chaque genre de valeur, effet sur
  l'inscription, la création d'espace et la longueur des avis, lecture publique, journal ;
  types et règles, gardes, effet sur le dossier de vérification du pro ;
- navigateur : bandeau visible par un visiteur, inscription fermée puis rouverte, règles
  d'un type d'espace, refus sans motif, modérateur sans accès, téléphone sans débordement.

## 10. ✅ Horaires des espaces

**Le modèle** (tables `horaire` et `horaire_exception`, qui existaient sans être utilisées) :
- **Semaine type**, une ligne par jour : fermé, ouvert **24 h/24**, ou une plage
  [ouverture, fermeture) avec une **pause** facultative. Si la fermeture est inférieure ou
  égale à l'ouverture, la plage passe minuit (18 h – 2 h).
- **Jours exceptionnels** : une date précise qui remplace la semaine type (fermé toute la
  journée, ou une plage dans la journée), avec un motif affiché aux visiteurs (Tabaski,
  Magal, inventaire…). Une exception n'annule pas la nuit qui déborde de la veille.
- **Fermeture temporaire** : la case « Espace ouvert » décochée par le professionnel prime sur
  les horaires (« Fermé temporairement »).
- Toutes les heures sont celles de **Dakar** (`Africa/Dakar`).
- Sans horaires renseignés, Nexora n'affiche **rien** : ni « Ouvert », ni « Fermé ».

**Une seule règle, deux implémentations vérifiées l'une contre l'autre**
- Java : `sn.ucad.nexora.espace.domain.horaire.Horaires`. Elle calcule l'état et son libellé :
  « Ouvert · ferme à 19 h », « Fermé · ouvre demain à 8 h 30 », « Ouvert 24 h/24 »,
  « Fermé · ouvre lundi à 8 h », « ferme vendredi à minuit ».
- SQL : `espace_ouvert_a(espace, moment)`, dans `03_professional/09_horaires.sql` (rejouable),
  utilisée par le filtre de recherche.
- Les mêmes cas sont testés des deux côtés : tests unitaires Java d'un côté, `test_horaires.py`
  sur la fonction SQL de l'autre.

**API** (espace-service)

| Route | Accès |
|---|---|
| `GET /api/v1/espaces/{id}/horaires` : semaine, exceptions à venir, `ouvertMaintenant`, `etat` | public (espace suspendu : propriétaire et modération) |
| `PUT /api/v1/espaces/{id}/horaires` `{semaine: [...]}` (liste vide = effacer) | propriétaire, `GERER_HORAIRES` |
| `POST /api/v1/espaces/{id}/horaires/exceptions` (une par date, remplacée) ; `DELETE .../exceptions/{id}` | propriétaire, `GERER_HORAIRES` |

Catalogue : la recherche accepte `ouvertMaintenant=true`, et chaque résultat porte
`espaceOuvertMaintenant`, ou null sans horaires.

**Web**
- Mon espace, onglet **Horaires** :
  - un tableau de la semaine, avec des champs heure ;
  - une première proposition, 8 h – 18 h du lundi au samedi, à ajuster ;
  - « Recopier le lundi jusqu'au samedi » ;
  - l'état actuel ;
  - les jours exceptionnels, à ajouter ou supprimer.
- Fiche espace : l'état en tête de fiche (vert si ouvert), puis la section Horaires, avec
  le jour courant mis en avant et les jours exceptionnels à venir.
- Recherche : la case « Ouverts maintenant », et une puce Ouvert / Fermé sur chaque carte.
- Démonstration : `09_seed/26_demo_horaires.sql` (hors installation) donne des horaires
  réalistes aux 7 espaces de démo. On y trouve un dépannage 24 h/24, un transport ouvert
  jusqu'à 2 h le samedi et un atelier fermé le vendredi après-midi.

**Tests**
- 8 tests unitaires de la règle ;
- API, 36 contrôles rejouables (`test_horaires.py`) :
  - droits et validation ;
  - 8 cas de la fonction SQL, identiques aux tests Java ;
  - exceptions, 24 h/24, fermeture temporaire, espace suspendu ;
  - filtre de recherche ;
- navigateur : saisie, recopie, erreur de pause, jour exceptionnel, fiche publique,
  puces et filtre de recherche ;
- non-régression complète.

À venir, si utile : proposer au professionnel les jours fériés du Sénégal (table
`jour_ferie`, vide pour l'instant) comme jours exceptionnels.

## 11. ✅ Recherche par photo, articles similaires, « Vous pourriez aussi aimer »

**Le besoin** : trouver un article à partir d'une photo (« je veux ces chaussures »), comparer
les prix d'un même article chez plusieurs commerçants, et proposer des articles voisins.

**Le modèle** : DINOv2-small (Meta, licence Apache 2.0), version quantifiée au format ONNX
(24,5 Mo), exécutée par ONNX Runtime dans catalogue-service, sans service externe. Une image
devient un vecteur de 384 nombres ; deux photos se ressemblent si le cosinus de leurs vecteurs
est élevé.
- Choix mesuré sur les photos de démonstration : une méthode classique (couleurs, contours,
  empreinte) retrouve 100 % des photos identiques retouchées, mais seulement 8 paires
  « même article, autre photo » sur 19 dans les 5 premiers ; DINOv2 en retrouve 18 sur 19.
- Fichier cherché dans `NEXORA_MODELS_DIR` (par défaut `~/nexora-models`), téléchargé au
  premier besoin depuis une révision figée de Hugging Face (`Xenova/dinov2-small`, `c2bb04a`),
  empreinte SHA-256 vérifiée. Sans modèle, la recherche par photo répond qu'elle est
  indisponible ; le reste du catalogue fonctionne.
- L'image est posée sur un carré blanc (sans être rognée), réduite à 224 × 224 par un filtre
  identique à celui de Pillow (les seuils ont été mesurés en Python ; parité Java/Python ≥ 0,99,
  vérifiée par `ModeleVisionTest`), puis normalisée comme pour ImageNet.
- Les appels au modèle passent par un fil dédié à grande pile (la pile par défaut d'un fil Java
  fait planter la JVM au chargement).
- Formats lus : JPEG, PNG, WebP (module TwelveMonkeys), GIF, BMP ; 10 Mo au plus.

**L'index** : table `image_vecteur` (url, modèle, vecteur, erreur). Toutes les images des offres
non supprimées sont indexées au démarrage, en arrière-plan (environ 0,2 s par image) ; les
nouvelles le sont avant chaque recherche. Une image illisible garde son erreur et n'est
retentée qu'après un jour. Changer de modèle (constante `ModeleVision.NOM`) recalcule tout.

**Les trois usages** (publics, sans connexion) :

| Usage | API (catalogue-service) | Règle |
|---|---|---|
| Recherche par photo | `POST /api/v1/offres/recherche-photo` (multipart « photo ») | offres visibles dont la ressemblance ≥ 0,35, 24 au plus, de la plus ressemblante à la moins ressemblante |
| Le même genre d'article dans d'autres espaces | `GET /api/v1/offres/{id}/similaires` | ressemblance ≥ 0,55, autres espaces seulement, 8 au plus |
| Vous pourriez aussi aimer | `GET /api/v1/offres/suggestions?offres=…` | voisins ≥ 0,30 sans « le même article ailleurs », deux par espace d'abord, un même produit une seule fois ; complété par les offres récentes des mêmes catégories puis du même espace |

Seuils mesurés : un même article photographié autrement obtient au moins 0,37 (médiane 0,67),
deux photos sans rapport 0,04 en médiane ; à 0,55, 15 paires sur 19 restent et moins de 1 %
des photos sans rapport passent.

**Web**
- `/recherche-photo` : choisir ou prendre une photo (sur téléphone, l'appareil photo est
  proposé), aperçu, résultats avec un badge « Très ressemblant / Ressemblant / Proche » (des
  mots plutôt qu'un pourcentage : le score classe, ce n'est pas une probabilité), et, si
  plusieurs espaces proposent des articles très ressemblants, la fourchette de prix.
- Accès : pastille « Par photo » de l'accueil, lien « Ou rechercher par photo » de la recherche,
  lien depuis la fiche d'un article.
- Fiche d'un article : « Le même genre d'article dans d'autres espaces », puis « Vous pourriez
  aussi aimer ».
- Accueil d'un visiteur connecté : « Vous pourriez aussi aimer », d'après ses cinq dernières
  consultations et ses favoris.
- La carte d'offre est un fragment commun (`WEB-INF/includes/offre-carte.xhtml`).

**Tests** : 24 contrôles API (`test_photo.py` : photo identique et retouchée, WebP et PNG,
erreurs, indexation d'une nouvelle offre sans relance, offre non publiée exclue, similaires
d'autres espaces, suggestions sans doublon ni recoupement) ; parcours navigateur sur ordinateur
et téléphone ; tests unitaires du prétraitement et de la parité Java/Python.

## 12. Messages d'erreur explicites

Un message doit dire **pourquoi** l'action a échoué, en français, sans terme technique.
- `h:messages` n'affiche que le résumé d'un message : tous les messages passent par
  `Messages.complet(gravité, titre, détail)`, qui les réunit (« Connexion impossible :
  Email/téléphone ou mot de passe incorrect. »).
- Validation des formulaires : chaque champ obligatoire porte le libellé de son étiquette, et les
  messages de JSF sont traduits (`messages.properties` : « Prénom : ce champ est obligatoire. »).
- Erreurs de l'API (`ApiErrors`) : le message du service est repris ; une erreur serveur (500 et
  plus) n'est jamais montrée telle quelle (« Nexora rencontre un souci technique… »), son détail
  reste dans le journal ; 401 sans message : « Votre session a expiré. Reconnectez-vous » ;
  403 : « Vous n'avez pas le droit de faire cette action ».
- Inscription : champs obligatoires, format de l'e-mail et du téléphone, mot de passe d'au moins
  8 caractères, tous contrôlés par auth-service avec un message clair (au lieu d'une erreur 500) ;
  l'e-mail est comparé en minuscules (une même adresse en majuscules est reconnue).

## 13. Déconnexion et bouton « Retour »

Après une déconnexion, le bouton « Retour » du navigateur ne doit rien réafficher de connecté
(sur un ordinateur partagé, la personne suivante verrait le compte, l'espace ou le back-office).
- Les pages ne sont jamais gardées en cache (`PagesSansCache` : `Cache-Control: no-store`) ;
  feuilles de style, scripts et images restent en cache, ils ne contiennent rien de personnel.
- Une page restaurée depuis la mémoire du navigateur (cache « aller-retour ») est rechargée
  depuis le serveur (script `pageshow` des deux gabarits).
- La déconnexion révoque le jeton de session auprès d'auth-service (`POST /api/v1/auth/logout` ;
  il ne permet plus d'obtenir de jeton d'accès) puis détruit la session du serveur.
- À la connexion, l'identifiant de session change (protection contre la fixation de session).

Vérifié dans le navigateur, pour un professionnel et pour le super administrateur : après la
déconnexion, trois « Retour » successifs n'affichent que des pages publiques ou la connexion.

## 14. ✅ Caractéristiques des articles (tailles, couleurs, matière…)

Ce qu'un client veut savoir avant de se déplacer : quelles tailles et quelles couleurs sont
disponibles, en quel tissu, quelles pointures, combien de stockage pour un téléphone, quelle
contenance pour un produit alimentaire. Le modèle reprend celui de DressIT (branche
`dressit-plateforme-vente`) : tailles et couleurs en pastilles à cocher, une pastille de couleur
réelle, et des valeurs « épuisées » qui restent affichées, barrées.

**Modèle** (tables existantes `attribut`, `valeur_attribut_possible`, `offre_attribut`) :
- Les caractéristiques sont **héritées** : celles de « Mode et textile » (couleurs, matière,
  fabrication, entretien) valent pour toutes ses sous-catégories, celles de « Vêtements homme »
  (tailles, coupe, manches) s'y ajoutent, puis « Col » pour les chemises. Le formulaire et la fiche
  les présentent de la catégorie la plus générale à la plus précise.
- `valeur_attribut_possible.code_couleur` : pastille `#RRGGBB` d'une couleur.
- `offre_attribut.epuise` : valeur proposée mais momentanément épuisée.
- Un choix multiple (`MULTI_LISTE`) donne une ligne `offre_attribut` par valeur cochée.
- Référentiel : `09_seed/28_caracteristiques.sql` (mode, chaussures, enfant, téléphonie,
  informatique, électroménager, alimentation) ; colonnes : `11_migrations/03_couleurs_epuises.sql`.

**Fiche article** : section « Caractéristiques » (marque et modèle, état neuf ou occasion,
garantie, puis chaque caractéristique ; tailles et couleurs en pastilles, épuisées barrées).
`GET /api/v1/offres/{id}` renvoie `caracteristiques: [{nom, typeChamp, unite, valeurs:
[{libelle, couleur, epuise}]}]`.

**Formulaire de création et de modification** : pastilles à cocher pour les choix multiples,
avec une ligne repliable « Marquer comme épuisé » ; Oui / Non pour les caractéristiques
booléennes ; l'unité (W, L, Go, mois, pouces) est rappelée dans le libellé.

**Produit ou service** (`attribut.pour_type` : TOUS, PRODUIT, SERVICE) : le formulaire n'affiche
les caractéristiques qu'une fois le type d'offre choisi, et seulement celles qui le concernent — un
service (« Câblage réseau ») n'a ni taille, ni couleur, ni garantie de produit. Les anciennes listes à
choix unique (taille, couleur, pointure de « Commerce > Mode »…) sont devenues des choix multiples
(`11_migrations/05_choix_multiples.sql`), et 65 types d'offre de service enregistrés comme produits
(câblage, coiffure, réparation, cours, ménage, locations…) sont redevenus des services.
À l'inverse, les rayons « de service » vendent aussi des produits : 15 types (Médicaments,
Parapharmacie, Hygiène et soins, Mobilité, Parfums, Pneus, Batteries, Pièces moteur, Voitures neuves et
d'occasion, Motos, Objets traditionnels, Bijouterie artisanale, Poterie, Paniererie) et leur « Autre »
sont redevenus des produits (`11_migrations/08_types_produits.sql`) : état, stock et garantie au
formulaire, présence dans « Boutique ». Réparation, location, assurance et formation restent des services. L'état d'un
produit est une seule information : « Neuf » ou « Occasion » dans « Détails de l'offre »
(la caractéristique « État » qui le doublait est retirée).

**Démonstration** : `09_seed/29_demo_caracteristiques.sql` range dans la bonne catégorie les
offres de démonstration classées trop haut (« Commerce ») ou dans la réparation alors qu'elles
sont vendues, puis renseigne leurs caractéristiques d'après leur titre (une chemise « en lin
blanche » : Blanc, Lin…). Il fonctionne aussi pour les offres des packs locaux ; il est lancé par
`demo.sql` et `mise_a_jour.sql`, et peut être rejoué après les packs.

## 15. ✅ Page Explorer et recherche autour d'un lieu

**Explorer et Rechercher sont deux pages différentes.** La recherche répond à une question
précise (« chemise », « Sandaga ») ; Explorer sert à se promener sans savoir exactement ce qu'on
cherche. Menu : Accueil · Explorer · Rechercher.

**Page Explorer** (`explorer.xhtml`, `GET /api/v1/explorer` de catalogue-service, public) :
- **Rayons** : chaque catégorie racine ayant des articles visibles, son nombre d'articles et
  ses sous-catégories, chacune avec son compte (lien vers la recherche filtrée) ;
- **Par quartier** : nombre d'espaces et d'articles par commune ;
- **Autour des lieux connus** : marchés d'abord, puis gares, hôpitaux… ayant des commerces à
  moins de 1,5 km (lien vers la recherche autour du lieu) ;
- **Carte** : tous les espaces (vert) et ces lieux (orange) ;
- **Nouveautés**, **Promotions en cours**, **Ouvert en ce moment**.
Seules les offres visibles du public sont comptées (publiées, disponibles, espace actif et ouvert).

**Recherche autour d'un lieu** : quand le texte saisi désigne un lieu public (« sandaga »,
« tilene » sans accent → Marché Tilène), la recherche ne cherche plus ce mot dans les articles
(ce qui ramenait des produits sans rapport) : elle affiche
- la fiche du lieu (type, adresse, description) et le bouton **Itinéraire jusqu'au lieu** ;
- les commerces autour, **du plus proche au plus loin**, avec la distance sur chaque article
  (« à 350 m ») ; rayon au choix : 500 m, 1 km (par défaut), 2 km, 5 km ;
- la carte avec le cercle de la zone ;
- « Vous cherchiez plutôt » : les autres lieux trouvés ; « Chercher « … » dans les articles
  plutôt » : revenir à la recherche de texte.
Les autres filtres (catégorie, type d'espace, prix, ouvert maintenant) restent utilisables.
Lien direct : `/recherche.xhtml?lieu={id}`.

API : `GET /api/v1/offres/recherche?lat=&lng=&rayonKm=` (rayon 2 km par défaut, 50 au plus ;
tri `DISTANCE`, appliqué d'office autour d'un point) ; chaque résultat porte `distanceKm`.
`GET /api/v1/lieux-publics/{id}`. La recherche de lieux ignore les accents.

## 16. ✅ Recherche par mots tolérante (« plombier » trouve « Débouchage canalisation »)

Avant, le texte saisi devait figurer tel quel dans le titre, la description, la catégorie ou la
commune : « plombier » ne trouvait rien, aucune offre ne contenant ce mot. Désormais :
- **Partout** : titre, description, catégorie **et ses catégories parentes**, type d'offre,
  **nom et type de l'espace**, tags, commune, quartier, département.
- **Sans accents ni majuscules** : « electricien » = « Électricien ».
- **Par racine** : « plombier » → « plomb », qui trouve « plomberie » ; « canalisations » trouve
  « canalisation » (`TexteRecherche`, terminaisons -ier, -erie, -eur, -euse, -ation, pluriels…).
  Mots vides ignorés (« le », « de », « pour »…).
- **Synonymes** (table `synonyme_recherche`, modifiable) : un mot que les clients tapent →
  ce qu'il couvre (plombier → canalisation, sanitaire, chauffe-eau ; frigo → réfrigérateur ;
  taxi → VTC, chauffeur ; coiffeur → coiffure, tresse…). Un seul sens, formes entières, sans mots
  trop larges (« mobile » trouverait « mobilier »).
- Plusieurs mots : **tous** doivent être trouvés (« chemise lin » → les chemises en lin).
- **Sans espaces** : « adjashop » trouve « Adja Shop », « sosplomberie » trouve SOS Plomberie.
- **Les espaces par leur nom** : au-dessus des offres, les espaces dont le nom (ou le slogan)
  correspond, même sans offre dans la zone ou les filtres choisis (`GET /api/v1/explorer/espaces?q=`).
- **Le vrai total** : le nombre de résultats compte toutes les pages (avant, la première page se
  croyait la dernière : on ne voyait que 12 offres). Sans recherche, le tri par défaut s'appelle « Tout ».
- **Ordre** : mot trouvé dans le titre d'abord, puis synonyme dans le titre, puis ailleurs.
- **Listes longues** (catégories, types…) : un champ « Tapez pour chercher… » les réduit pendant la
  saisie, sans accents ni majuscules (`listes-recherche.js`, toutes les pages, y compris le formulaire d'offre).
- Après « Suivant », un tri, un filtre ou un rayon, la page reste sur les résultats.
- Les caractéristiques de même nom (« Tailles disponibles » homme et femme) ne font qu'un filtre,
  proposé seulement pendant une recherche ou pour une catégorie.

Données : `04_catalogue/18_synonyme_recherche.sql`, `09_seed/30_synonymes_recherche.sql`. Les offres
de démonstration rangées dans « Services » ou « Transport » (racine) vont dans Plomberie,
Menuiserie bois, Livraison colis, VTC, Location avec chauffeur (`29_demo_caracteristiques.sql`).

## 17. ✅ Tris et filtres de recherche

**Tris** (barre au-dessus des résultats) : Pertinence · **Plus proches** · **Moins chers** ·
**Plus vendus** · Mieux notés · Nouveautés · Meilleures remises · Plus chers.
- *Plus proches* : autour du lieu reconnu (« Sandaga ») ou de **votre position** ; sans l'un ni
  l'autre, le bouton demande la position au navigateur.
- *Plus vendus* : quantité vendue (commandes non annulées ni remboursées), puis favoris, puis
  consultations ; tant qu'il n'y a pas de ventes, c'est la popularité qui classe. La carte d'un
  article affiche « N vendu(s) » dès la première vente.
- *Meilleures remises* : écart entre le prix barré et le prix.

**Filtres** : texte ; **Autour de moi** (position du navigateur, rayon 500 m à 5 km, « Ne plus
utiliser ma position ») ; catégorie, et **Affiner** par les sous-catégories présentes dans les
résultats ; produits ou services ; **prix minimum et maximum** (fourchette des résultats en
indication) ; **caractéristiques** présentes dans les résultats, avec leurs comptes (tailles,
couleurs avec pastille, matière, pointures…) — plusieurs valeurs d'une même caractéristique =
l'une ou l'autre, plusieurs caractéristiques = toutes ; une taille ou couleur **épuisée ne compte
pas** ; **note minimale** de l'espace ; **neuf ou occasion** ; en promotion (promotion en cours
ou prix barré) ; prix négociable ; intervention à domicile ; ouverts maintenant ; espaces vérifiés.
Sur téléphone, les filtres sont repliés (avec le nombre de filtres actifs) et les tris défilent.

API : `GET /api/v1/offres/recherche` accepte `noteMin`, `neuf`, `negociable`, `domicile`,
`valeurs` (id de valeurs, répétable) et les tris `POPULARITE`, `REMISE` ; chaque résultat porte
`nombreVentes`. `GET /api/v1/offres/recherche/facettes` (mêmes paramètres) : catégories et
valeurs de caractéristiques présentes dans les résultats, avec leurs comptes, et la fourchette de prix.

## 18. ✅ Nexora Découvrir : une offre à la fois, autour de moi, adaptée à mes goûts

Trois façons de trouver, trois intentions :
- **Rechercher** : « je sais ce que je veux » (robe bazin rouge taille M) ;
- **Explorer** et le filtre **Autour de moi** de la recherche : « qu'y a-t-il autour de moi, dans ce quartier ? » ;
- **Découvrir** : « montre-moi ce qui pourrait me plaire ».
Menu : Accueil · Explorer · ✨ Découvrir · Rechercher (« autour de moi » est un filtre de la recherche).

On ne copie pas TikTok : on reprend son meilleur mécanisme (une découverte à la fois, sans fin,
sans friction) pour en faire une vitrine **locale** et **transactionnelle**.

**Le flux** (`decouvrir.xhtml`, plein écran) : on glisse vers le haut pour passer à la carte
suivante ; les pages suivantes se chargent avant d'arriver au bout. Quatre sortes de cartes :
- **Produit** — photo plein écran, prix, tailles et couleurs disponibles, professionnel (✓ vérifié,
  note, ouvert) ; **Commander** (par WhatsApp, message prérempli) ;
- **Service** — « À partir de … » ; **Demander un devis** ;
- **Prestation** (mariage, décoration, pack, réception, séance…) et restaurant — **Réserver** ;
- **Professionnel** (une par page) — sa vitrine (couverture, photos de ses articles), note,
  nombre d'offres ; **Découvrir l'espace**, **Appeler**.
Sur chaque carte : ❤️ J'aime (double-tap aussi), 🔖 Enregistrer (favori du compte), ↗ Partager
(partage du téléphone ou lien copié), 📞 Appeler ; plusieurs photos : toucher à gauche / à droite ;
vidéos (.mp4, .webm) lues en boucle, sans le son, quand la carte est à l'écran.
Une **accroche locale** : « 🔥 Ça bouge à Sandaga », « 🍽️ Ce soir à Plateau », « ✨ Nouveauté à
Almadies », « 🏷️ −20 % en ce moment », « 💍 Mariage » ; et **pourquoi** cette carte :
« Parce que vous aimez « Mode et textile » », « Tendance à Dakar », « Pour changer : à découvrir ».

**La zone** : tout le Sénégal, un quartier (liste des quartiers ayant des offres), ou **autour de
moi** (3 km). Le flux ne montre que ce qui est réellement accessible dans la zone.

**L'apprentissage** (table `decouverte_signal`) : chaque interaction est un signal, du plus faible
au plus fort — passée vite (< 1 s, −0,6), vue (≥ 1 s, 0,2), vue longue (≥ 3,5 s, 1), J'aime (3),
partage (3), enregistré (4), fiche ouverte (4), tailles consultées (5), contact / devis (7),
commande / réservation (8). Les poids s'atténuent avec le temps (moitié en deux semaines
environ, 90 jours au plus). Le profil s'établit par rayon, catégorie parente, catégorie et
professionnel. Une offre passée vite n'est pas remontrée pendant 7 jours.

**Le classement** de chaque offre :
`3 × intérêt (profil) + 1,2 × proximité + photos + 0,5 × disponibilité (ouvert) + popularité
(vues, favoris, ventes) + qualité du professionnel (vérifié, certifié, note) + nouveauté
(+ 1,5 × rang dans la recherche) + un peu de hasard`.
Chaque page mélange ~**70 %** d'offres pertinentes, ~**20 %** de tendances locales (nouveautés,
succès du coin) et ~**10 %** de découverte (un rayon que le visiteur n'a pas exploré), sans plus de
deux offres d'une même catégorie, jamais deux fois de suite le même professionnel, au moins un
service s'il y en a, et une fiche de professionnel en 5e position.

**Selon le besoin** : en haut du flux, « Pour vous · 🛍️ Boutique · 🛠️ Services » — la boutique ne
montre que des produits, les services que des services et prestations. Explorer propose les mêmes
deux entrées (« Boutique », « Services »), qui ouvrent la recherche filtrée (`?nature=PRODUIT|SERVICE`).

**Pas de boîte noire** : ✨ « Vos goûts » montre ce que Nexora a compris (rayons et catégories
préférés), le mélange 70/20/10, et permet d'**effacer ses goûts**.

**Identité** : un visiteur anonyme est un identifiant aléatoire gardé dans un cookie
(`nx_visiteur`, un an) ; une fois connecté, c'est son compte (`c-<compte>`) — le profil le suit
d'un appareil à l'autre, et l'historique anonyme du navigateur y est rattaché à la première visite
connectée.

**Recherche → Découvrir** : sous les résultats, « ✨ Découvrir ces N offres, une à la fois »
ouvre le flux limité à la recherche (mêmes filtres, caractéristiques comprises).

API (catalogue-service, publique) : `GET /api/v1/decouvrir` (visiteur, zone ou lat/lng/rayonKm,
vus, vusEspaces, taille, et les critères de la recherche), `POST /api/v1/decouvrir/signaux`,
`DELETE /api/v1/decouvrir/signaux/j-aime`, `GET|DELETE /api/v1/decouvrir/profil`,
`POST /api/v1/decouvrir/fusion`. Le web relaie (`/api/decouvrir/flux|signal|jaime|enregistrer|gouts|zones`)
en ajoutant l'identité du visiteur.

**À venir** : vidéos et formats « avant / après » ou « démonstration » déposés par les
professionnels (le flux sait déjà lire une vidéo), commande et réservation dans Nexora quand le
panier existera (aujourd'hui par WhatsApp), notifications « nouveauté chez un professionnel suivi ».

## 19. Vérification globale (2e passe) et corrections

**Parcours automatique** de toutes les pages, par rôle (visiteur, client, professionnel, super
administrateur, agent de vérification), sur ordinateur (1280 px) et téléphone (390 px) : ~190 pages
par largeur, en suivant les liens de chaque page. Relevés : code HTTP, erreurs JavaScript et de
console, messages d'erreur affichés, débordement horizontal, images locales cassées, liens morts.
Résultat : aucun souci.

**Corrigé à cette occasion** (retours de test) :
- **Téléphones** : aucun champ ne vérifiait le format — une adresse e-mail s'était retrouvée dans
  le téléphone d'un espace. Inscription, Mon compte, création et modification d'espace exigent
  désormais un numéro sénégalais (9 chiffres commençant par 7 ou 3, +221 facultatif), avec le
  message sous le champ. La fiche d'un espace n'affiche plus un numéro invalide, et les liens
  « Appeler » / « WhatsApp » sont au format international (+221…), quels que soient les espaces
  saisis. Le téléphone du **compte** (Mon compte) et celui de l'**espace** (Mon espace, vu par les
  clients) sont deux informations distinctes : la page Mon compte le rappelle.
- **Lieux** : 33 marchés et quartiers de Dakar et de sa banlieue ajoutés comme repères
  (Colobane, Gueule Tapée, Fass, Grand Yoff, Ouakam, Parcelles, Pikine, Guédiawaye, Almadies,
  Mermoz, Sacré-Cœur, Point E…) : « colobane » montre le quartier, l'itinéraire et les commerces
  autour (`09_seed/31_lieux_dakar_complements.sql`, coordonnées approximatives).

## 20. ✅ Lieux et repères, galerie de photos, compteurs de l'espace, filtres de la recherche par photo

**Lieux et repères** (`lieux.xhtml`, lien dans Explorer, la recherche et le pied de page) : tous les
lieux publics (133), filtrés par famille — À visiter (Gorée, Maison des Esclaves, Musée des
Civilisations noires, Phare des Mamelles, Pointe des Almadies, Lac Rose…), Marchés et shopping,
Quartiers, Transports (gares, TER, gare routière, aéroport, port), Santé, Lieux de culte,
Administrations et services, Sport — et par texte (sans accents), avec la carte. Chaque lieu :
**🧭 Itinéraire** (Google Maps) et **🛍️ Commerces autour** (recherche autour du lieu).
Lieux célèbres ajoutés : `09_seed/32_lieux_celebres.sql` (coordonnées approximatives). Un texte qui
est le nom d'un espace (« auchan ») cherche ses offres plutôt qu'un lieu homonyme.
Lieux en double (base aux accents abîmés où les scripts de lieux s'étaient réinsérés) : supprimés
par `11_migrations/07_lieux_doublons.sql` (même nom et même commune, on garde la plus ancienne copie),
passé par `mise_a_jour.sql` après la réparation des accents.

**Fiche d'une offre** : toutes ses photos — galerie avec flèches, compteur, vignettes, glisser au
doigt sur téléphone, flèches du clavier (avant : la photo principale seulement).

**Vue d'ensemble de Mon espace** : les compteurs d'un espace créé depuis l'application étaient
vides (NULL), et « NULL + 1 » restant NULL, les vues ne montaient jamais ; aucun service ne tenait
le nombre de favoris. Compteurs à 0 dès la création, incrément robuste, et un déclencheur
`trg_favori_compter` recalcule les favoris d'un espace (ceux de l'espace et de ses offres) à chaque
favori ajouté ou retiré (`11_migrations/06_compteurs_espace.sql`). La tuile « vues » additionne les
vues de l'espace et de ses offres.

**Recherche par photo** : tri (les plus ressemblants, moins chers, plus chers, mieux notés) et
filtres (prix maximum, quartier, très ressemblants, ouverts maintenant, espaces vérifiés),
appliqués sur place.

---
*Dernière mise à jour : session du 06/10/2026, Nexora Découvrir.*

## 21. ✅ « Autre… » : catégorie écrite par le professionnel, traitée par l'administration

**Principe** : on ne sort jamais du domaine de l'espace. Les catégories proposées à l'ajout d'une offre
restent celles du type de l'espace ; « Autre… (écrire ma catégorie) », en bas de la liste, sert quand
aucune ne convient. Le texte libre ne crée jamais de catégorie tout seul (sinon « Tissu », « tissus »,
« Tisus wax »… éparpilleraient le catalogue) : c'est une proposition que l'administration traite.

**Côté professionnel** (`creer-offre.xhtml`, `CreerOffreBean`) : « Autre… » ouvre « Quelle catégorie ? »
(obligatoire, 120 caractères) et « C'est… un article à vendre / un service » ; le reste du formulaire suit
la nature choisie (état, stock, garantie ou durée, domicile). L'offre est publiée tout de suite.
`RangementAutre` (catalogue-service) la range dans le rayon de l'espace — celui où il a déjà le plus
d'offres, sinon un rayon principal de son type d'espace — avec le type « Autre » de ce rayon (créé à la
racine la première fois). Le texte est gardé dans `offre.categorie_proposee`
(`11_migrations/09_categorie_proposee.sql`) : il est cherchable, affiché comme nom de catégorie sur les
cartes, et l'édition de l'offre rouvre le mode « Autre… ».

**Côté administration** (`admin/propositions.xhtml`, menu Gestion › Catégories proposées, permission
GERER_CATEGORIES ; API `GET/POST /api/v1/admin/catalogue/propositions[/creer|/rattacher|/ecarter]`) :
les propositions sont regroupées quand le texte revient au même (accents, majuscules, pluriels :
« Tissus wax » = « tissu Wax »), les plus demandées d'abord, avec les offres, les espaces, la nature et le
rayon actuel. Trois décisions, chacune écrite dans le journal (module CATALOGUE) :
- **Créer la catégorie** sous le rayon actuel ou l'une de ses sous-catégories, avec un type d'offre du
  même nom et un « Autre » : les offres y sont rangées, le texte proposé est effacé ;
- **Ranger dans une catégorie existante** (recherche dans le catalogue) : chaque offre prend le type de
  sa nature (« Autre » de préférence) ;
- **Écarter** (motif) : les offres restent dans leur rayon, le texte est effacé.

## 22. ✅ Avis : mes avis, avis reçus et réponse du professionnel

**Client**
- **Fiche espace** : son avis porte le badge « Votre avis » ; « Modifier mon avis » (note et commentaire,
  « modifié le … » affiché) et « Supprimer ». Un avis masqué par la modération ne se modifie plus
  (on ne contourne pas la modération) ; il reste supprimable.
- **Mon compte › Mes avis** (`#mes-avis`, pastille dans l'en-tête) : tous ses avis, masqués compris
  avec le motif, la photo et le lien de l'espace (ou de l'offre), la réponse du professionnel
  (badge « Le pro a répondu »), « Modifier » (vers la fiche) et « Supprimer ».

**Professionnel** (`mon-espace.xhtml?id=…#avis`, onglet « Avis reçus », badge = avis sans réponse)
- Synthèse : moyenne, nombre d'avis, répartition 5 → 1 étoiles ; avis masqués signalés mais non comptés.
- Filtres Tous / Sans réponse / Critiques (1-2 ★) ; avis sur l'espace et sur ses offres (« Sur : … »).
- **Réponse publique** sous chaque avis : publier, modifier, retirer. Elle s'affiche sous l'avis sur la
  fiche espace (« ↳ Réponse de … ») et dans « Mes avis » de son auteur. Pas de réponse à un avis masqué.
- Sur sa propre fiche, le professionnel voit « répondez à vos avis depuis Mon espace → ».

**Notifications** (table `notification`, en attendant la page Notifications) : le professionnel est
prévenu de chaque nouvel avis (lien vers `#avis` de Mon espace) ; l'auteur est prévenu de la première
réponse (pas des retouches).

**API** (recherche-service, connecté)

| Route | Règle |
|---|---|
| `PUT /api/v1/avis/{id}` `{note, commentaire}` | son avis, non masqué ; note recalculée |
| `GET /api/v1/avis/mes-avis` | ses avis, masqués compris |
| `GET /api/v1/avis/recus?espaceId=` | propriétaire de l'espace : synthèse et avis visibles |
| `PUT /api/v1/avis/{id}/reponse` `{texte}`, `DELETE …/reponse` | propriétaire de l'espace, avis non masqué, `AVIS_LONGUEUR_MAX` |

**Base** : `11_migrations/10_avis_modification.sql` (colonne `avis.date_modification` ; la réponse
utilise `reponse_fournisseur` / `date_reponse`, présentes depuis l'origine). Démonstration :
`09_seed/33_demo_reponses_avis.sql`, six réponses (surtout aux critiques) ; les avis de Boutique Sarah
Mode restent sans réponse pour essayer l'onglet.

**Correctif** : sur téléphone, Mon espace passe en une colonne (le menu au-dessus du contenu) au lieu
d'écraser le contenu à côté du menu.

## 23. ✅ Popularité des offres : vues, j'aime et favoris comptés, badge « 🔥 Populaire »

**Ce qui est compté** (par la base, `11_migrations/11_popularite_offres.sql`, quel que soit le service qui agit) :

| Compteur | Source | Règle |
|---|---|---|
| `offre.vue_count` | fiche de l'offre ouverte | existait déjà |
| `offre.vues_decouvrir` | carte regardée ≥ 1 s dans Découvrir (`VUE`, `VUE_LONGUE`) | compteur : ne redescend pas si un visiteur efface ses goûts |
| `offre.nombre_jaime` | « J'aime » dans Découvrir | visiteurs distincts ; retiré quand on n'aime plus ; un même visiteur connecté sur deux appareils compte une fois |
| `offre.nombre_favoris` | cœur de la fiche ou « Enregistrer » dans Découvrir | nombre de comptes |
| `espace_professionnel.nombre_jaime` | « J'aime » sur la fiche d'un espace dans Découvrir | lecture seule côté espace-service |

`offre.score_popularite` (colonne calculée) = vues + ½ vue Découvrir + 3 j'aime + 5 favoris.

**Propriété de démarquage — badge « 🔥 Populaire »** : une offre publiée parmi les **20 % au meilleur
score**, avec **au moins deux j'aime ou favoris**. Le seuil est relatif : il suit la vie de la plateforme
(pas de chiffre magique à revoir quand Nexora grandit). Le badge et les compteurs servent :
- sur les cartes d'offres (badge en haut à droite, « ♥ n » et « 🔖 n ») et sur la fiche (« 🔥 Populaire ·
  ♥ j'aime · 🔖 favoris · 👁 vues ») ;
- dans la recherche : tri « Populaires » (score + 10 × ventes), filtre « 🔥 Populaires » (`?populaire=true`) ;
- sur Explorer : section « 🔥 Populaires sur Nexora » ;
- dans Découvrir : le nombre de j'aime sous le cœur (mis à jour au clic), l'accroche « 🔥 Populaire à … »,
  et la popularité dans le classement des tendances.

**Pour le professionnel** (Mon espace) : vue d'ensemble « vues (dont n dans Découvrir) » et « j'aime dans
Découvrir » (offres + fiche de l'espace), message quand des offres portent le badge ; sur chaque offre,
badge et « 👁 vues · ♥ j'aime · 🔖 favoris » (au survol : fiche / Découvrir).

**API** : `OffreSummaryResponse` et `OffreDetailResponse` portent `vuesDecouvrir`, `nombreJaime`,
`nombreFavoris`, `populaire` ; `GET /api/v1/offres/recherche?populaire=true` ; la carte Découvrir porte
`nombreJaime` et `populaire` ; `EspaceResponse.nombreJaime`.

**Démonstration** : `09_seed/34_demo_popularite.sql`, vues et j'aime de visiteurs fictifs (`demo-…`),
uniquement sur les offres des comptes `@nexora-demo.sn` ; rejouable.

## 24. ✅ J'aime, Enregistrer et Partager sur les fiches ; Découvrir ne s'épuise plus

**Fiche offre et fiche espace** : une barre « ♡ n j'aime · 🔖 Enregistrer · n · ↗ Partager » (et, sur l'offre,
« 👁 vues » et le badge « 🔥 Populaire »).
- **J'aime** : ouvert à tous, sans compte. C'est le même signal que dans Découvrir (`decouvrir/Reactions`) :
  le même visiteur (cookie anonyme, puis « c-&lt;compte&gt; » une fois connecté, l'historique anonyme y étant
  rattaché), compté une fois, retirable ici ou là. L'état est relu au chargement
  (`GET /api/v1/decouvrir/signaux/j-aime?visiteur=&idOffre=|idEspace=`). Un pro n'aime pas son propre espace.
- **Enregistrer** : le favori du compte (ajout et retrait) ; sans compte, lien vers la connexion qui ramène à la fiche.
- **Partager** : le partage du téléphone (WhatsApp, SMS…) quand il existe, sinon le lien est copié.
- Correctif : l'ancien bouton « ♡ Favori » était invisible (bouton clair prévu pour fond sombre, blanc sur blanc).

**Découvrir** : une offre passée vite (moins d'une seconde) n'est plus cachée 7 jours, elle recule en fin de flux
(pénalité de score). Un défilement rapide vidait le flux d'un petit catalogue, « Revoir depuis le début »
compris. Seules les cartes déjà montrées pendant la séance sont exclues ; « Vous avez tout vu » ne s'affiche
donc qu'après avoir vraiment tout parcouru.

## 25. ✅ Sécurité du compte (`/securite`, lien depuis Mon compte)

- **Mot de passe** : l'actuel est exigé ; 8 caractères au moins, différent de l'actuel, confirmé. Les **autres
  appareils sont déconnectés**, celui-ci reste connecté. Notification « Votre mot de passe a été modifié ».
- **Email de connexion** : nouvelle adresse + mot de passe → un **code** est envoyé à la nouvelle adresse
  (valable 15 minutes, 5 essais) ; l'email ne change qu'à la saisie du code (compte et profil). Adresse déjà
  prise refusée. Les autres appareils sont déconnectés ; notification.
- **Appareils connectés** : chaque connexion ouvre une **session** (`connexion_compte`, identifiant « sid »
  inscrit dans les jetons), avec l'appareil (« Chrome · Windows », « Safari · iPhone ») et l'adresse IP
  relayés par le web (`X-Nexora-Client-IP`, `X-Nexora-Client-Agent`). « Déconnecter » un appareil ou « tous
  les autres » : son rafraîchissement est refusé, il perd l'accès au plus tard 15 minutes après (durée du
  jeton d'accès). La déconnexion ferme la session ; 30 jours sans activité, elle est considérée terminée.
- **Historique et alerte** : sessions terminées (motif) et **tentatives échouées** (mauvais mot de passe sur
  un compte existant) des 30 derniers jours, avec un bandeau d'alerte s'il y en a.
- Journal (module SECURITE) : changement de mot de passe, d'email, déconnexion d'appareils. Le journal
  enregistre désormais le navigateur réel de l'utilisateur, et non plus celui du serveur web.

**API** (auth-service, connecté) : `GET /api/v1/compte/securite` ; `POST …/mot-de-passe`, `…/email`,
`…/email/confirmer`, `…/sessions/{sid}/terminer`, `…/sessions/terminer-autres`.
**Base** : `11_migrations/12_securite_comptes.sql` (`connexion_compte`, `changement_email`).

## 26. ✅ Statistiques détaillées du professionnel (Mon espace → « Statistiques »)

- **Période** : 4 semaines, 12 semaines ou 6 mois (semaines du lundi, heure de Dakar, semaine en cours
  comprise). Elle est **comparée à la période précédente de même durée, arrêtée à la même heure** : la
  semaine en cours n'est jamais opposée à une semaine entière.
- **Indicateurs** : Vues (fiches + Découvrir), J'aime, Favoris, Contacts (Appeler + WhatsApp + Itinéraire)
  et **Taux de contact** (contacts pour 100 vues de fiche), chacun avec son évolution (▲/▼ en %, en points
  pour le taux ; « nouveau » si la période précédente était vide) et sa courbe semaine par semaine.
- **Évolution par semaine** : histogramme Vues (Fiches et Découvrir empilées), J'aime, Favoris ou Contacts ;
  info-bulle au survol (détail par canal pour les contacts), légende avec totaux, et les **chiffres en tableau**.
- **Contacts par canal** : WhatsApp, Appels, Itinéraire, Partages (nombre et part).
- **Par offre** : vues de fiche, Découvrir, j'aime, favoris, contacts, taux ; tri par colonne, 10 premières
  puis « Voir les n offres ». Lien depuis la Vue d'ensemble de l'onglet Offres.

**Collecte** : les boutons Appeler, WhatsApp, Itinéraire et Partager des fiches offre et espace portent
`data-stat` ; un clic est envoyé sans bloquer la navigation (`fetch keepalive`) à `POST /api/statistiques/clic`
(web), qui le relaie avec l'identifiant de visiteur (le même que Découvrir) à
`POST /api/v1/statistiques/evenements` (catalogue, ouvert). Un même visiteur n'est compté qu'une fois toutes
les 30 secondes par bouton et par cible. Les vues de fiches sont datées par déclencheur à chaque hausse de
`offre.vue_count` / `espace_professionnel.nombre_vues`. Les vues, j'aime, appels et partages de Découvrir
(`decouverte_signal`) et les favoris (`favori.date_creation`) sont relus tels quels.

**API** : `GET /api/v1/statistiques/espaces/{id}?semaines=4|12|26` (propriétaire de l'espace).
**Base** : `11_migrations/13_statistiques.sql` (`evenement_statistique`, déclencheurs de vues) ;
démonstration `09_seed/35_demo_statistiques.sql` (25 semaines d'historique et des favoris datés pour les
comptes `@nexora-demo.sn`).

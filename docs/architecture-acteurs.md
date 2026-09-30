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

Seule la supervision de la vérification existe (`/admin/verifications`, §8.11), dans le
layout public en attendant le back-office. `nexora-administration-service` a son schéma
(`statistique`, `journal_action`, `parametre`) mais aucun contrôleur ni page web. C'est le
chantier le plus vide et le plus structurant à cadrer avant de coder quoi que ce soit
dessus.

| Page | État |
|---|---|
| Tableau de bord admin | 🚧 tout |
| Gérer les utilisateurs (suspendre/réactiver/supprimer) | 🚧 |
| Gérer/modérer les espaces | 🚧 |
| Gérer le catalogue (catégories, types d'offre, attributs, tags) | 🚧 (déjà en base et seedé, aucune UI d'admin) |
| Modérer avis / traiter signalements | 🚧 |
| Vérifications : supervision, agents (§8) | ✅ `/admin/verifications` — l'édition des règles de justificatifs reste à faire |
| Certifications 🏅 | — (après la vérification) |
| Statistiques plateforme | 🚧 |
| Journal d'activité | 🚧 |
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
- **Admin** : navigation séparée, jamais mélangée à la navigation publique
  (`/admin/...`, layout dédié, pas le header Nexora grand public) — pas encore construite.
  Menu cible : Utilisateurs · Espaces · Vérifications · Agents · Catalogue · Catégories ·
  Offres · Avis · Signalements · Statistiques · Paramètres. Aujourd'hui : un lien
  « Supervision » (rôles `ADMIN`/`SUPER_ADMIN`) vers `/admin/verifications`, protégé par
  `SessionBean.exigerAdministrateur`.

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
6. **Back-office admin** : le plus gros chantier, à cadrer précisément (pages,
   permissions, layout séparé) avant de commencer à coder — probablement sa propre
   session de conception dédiée plutôt qu'un ajout au fil de l'eau.
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

---
*Dernière mise à jour : session du 30/09/2026, vérification des espaces livrée de bout en
bout (§8, étapes 5a à 5d).*

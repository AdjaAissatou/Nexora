# Nexora — langage visuel

> Découvrez. Connectez. Faites vivre le local.

Nexora doit donner envie de sortir découvrir sa ville, pas seulement « lister des espaces ».
Tout le style est dans `nexora-web/.../resources/css/nexora-theme.css`. Les composants utilisent
les **variables**, jamais les couleurs en dur : changer une variable change tout le site.

## 1. Les sept principes

| Principe | Traduction |
|---|---|
| **Deep** | Vert forêt, émeraude, charbon : la couleur de marque, sérieuse et profonde. |
| **Glow** | Or champagne et halos lumineux, **sur peu d'éléments** : l'étoile ✦, un badge « Certifié », un bouton. |
| **Warm** | Terracotta et corail : chaleur, promotions, lieux. |
| **Light** | Fond ivoire et sable, jamais un blanc ou un gris plat. |
| **Life** | De vraies photographies : des gens, des plats, des rues. |
| **Depth** | Dégradés, verre dépoli, ombres douces : des couches qui flottent. |
| **Editorial** | Serif (Playfair Display) pour les grands titres, sans-serif (DM Sans) pour l'interface. |

## 2. Palette

| Variable | Couleur | Usage |
|---|---|---|
| `--nx-gold` | `#E7B84B` or lumineux | ✦, badge Certifié, bouton doré, soulignés |
| `--nx-emerald` | `#0F5B4F` vert émeraude | couleur principale (`--nx-primary`) |
| `--nx-forest` | `#073B35` vert profond | titres, pied de page, dégradés (`--nx-primary-dark`) |
| `--nx-terracotta` | `#C96F4A` | lieux (📍), accents chauds |
| `--nx-coral` | `#E8956A` | promotions (dégradé chaud) |
| `--nx-ivory` | `#FFF9EF` | fond des pages (`--nx-bg`) |
| `--nx-sand` | `#F2E5D0` | fonds de cartes et d'images en attente |

Dégradés : `--nx-grad-primary` (forêt → émeraude, boutons), `--nx-grad-gold`
(`#C99632 → #E7B84B → #F4D27A`, un or « champagne », pas jaune), `--nx-grad-warm`.

## 3. Composants

- **Fond** : trois halos radiaux (or en haut à gauche, vert à droite, terracotta en bas) sur l'ivoire.
- **Boutons** : dégradé vert profond ; au survol ils montent de 2 px et s'entourent d'une lueur dorée
  (`--nx-glow-gold`). `nx-button-gold` pour l'action phare d'un bloc sombre.
- **Recherche** : barre de verre (`rgba(255,255,255,.72)`, flou 18 px), *quoi* + 📍 *où* + →.
  Au focus, un liseré doré.
- **Pastilles ✦** (`nx-pastille`) : accès rapides aux types d'espace, et filtres de « Près de vous ».
- **Cartes vivantes** (`nx-espace-carte`, `nx-offre-card`, `nx-categorie-photo`) : au survol la photo
  zoome, la carte monte de 4 px, l'ombre se creuse, et le bouton doré « Découvrir » apparaît
  (toujours visible sur écran tactile).
- **Photos plein cadre** (`nx-hero-photo`, `nx-talents-photo`, `nx-auth-visuel`) : l'image passe par la
  variable CSS `--photo`, recouverte d'un dégradé vert profond et d'un halo doré.
- **Navigation** : barre de verre collante, soulignée d'or au survol (non collante sur téléphone).
- **Pied de page** : vert profond éclairé de halos.
- **Back-office** : même palette, plus sobre (outil de travail, sans photos ni animations).
  Sur téléphone, les tableaux défilent dans leur cadre (jamais la page entière) et les colonnes
  de grille ne s'élargissent pas à la taille de leur contenu.
- **Badge de ressemblance** (`nx-ressemblance`) : posé sur la photo d'une carte d'offre dans la
  recherche par photo ; doré pour « Très ressemblant », blanc sinon.
- **Bandeau d'annonce** (`nx-bandeau`) : bande dorée au-dessus de la navigation, alimentée par
  le paramètre `SITE_BANDEAU` (§9.12 de l'architecture).

`prefers-reduced-motion` coupe les animations. Points de rupture : 1000, 760, 700 et 450 px.

## 4. Pages

- **Accueil** : hero « Découvrez ce qui fait vivre *votre ville*. » avec recherche et photo de Dakar ;
  catégories en images ; tous les types d'espace ; « Le Sénégal regorge de talents » avec les
  **chiffres réels** (`GET /api/v1/public/chiffres` d'administration-service : espaces actifs, offres
  publiées, espaces vérifiés, communes) ; « Près de vous » : carte Leaflet des espaces et cartes
  d'espace, filtrées par les pastilles ; bloc professionnels ; trois valeurs.
- **Connexion / inscription** : photo à gauche (dégradé vert, halo doré, « Découvrez. Connectez.
  *Faites vivre le local.* »), formulaire de verre à droite. Identifiants des champs inchangés.
- Toutes les autres pages héritent du thème : titres en serif, cartes, boutons, badges.

## 5. Photographies

Dans `resources/images/`, détail et sources dans `CREDITS.md` (Pexels : usage libre, y compris
commercial). Deux règles :

1. une légende ne cite un lieu que si la photo y a réellement été prise ;
2. une photo d'ambiance n'est jamais présentée comme celle d'un espace inscrit. Elle sert de **repli**
   quand un espace n'a pas encore de photo (`PhotosNexora.pourType`), ou tant que sa photo charge.

Pour remplacer une photo, garder le même nom de fichier. Les meilleures photos viendront des
professionnels eux-mêmes : devantures, plats, ateliers, avec leur accord.

# Nexora — Design System (identité visuelle premium)

> Mot d'ordre : **Simple • Élégant • Moderne • Professionnel**.
> Référence vivante : `docs/ux/nexora-ui-prototype.html` (prototype interactif :
> accueil, recherche + carte, fiche détail — light & dark).

L'objectif : à l'ouverture, l'impression d'Airbnb / Notion / Uber — beaucoup
d'espace, interface épurée, cartes élégantes, navigation fluide.

## 1. Palette
| Rôle | Token | Hex |
|---|---|---|
| Identité (émeraude) | `--emerald` | `#10B981` |
| Action / bouton | `--emerald-600` | `#059669` |
| Survol action | `--emerald-700` | `#047857` |
| Teintes douces | `--emerald-50 / -100` | `#ECFDF5` / `#D1FAE5` |
| Éléments importants (rare) | `--nuit` | `#1E3A8A` |
| Fond principal | `--bg` | `#FFFFFF` |
| Surfaces / cartes | `--surface` | `#F8FAFC` |
| Texte principal | `--text` | `#0F172A` |
| Texte secondaire | `--text-2` | `#475569` |
| Alerte | `--danger` | `#EF4444` |
| Note (étoiles) | `--amber` | `#F59E0B` |

Règle : **un seul accent dominant** (le vert émeraude) guide le regard vers les
actions clés (recherche, contact, réservation). Le bleu nuit est réservé aux
éléments de marque / importants. Pas de multiplication des couleurs.

Le mode sombre existe (tokens redéfinis, fond `#0A0F1C` bleu-nuit) mais le
**light est l'expérience par défaut** — jamais de fond sombre permanent imposé.

## 2. Typographie — Poppins (embarquée en @font-face data-URI)
- **Titres** : Poppins SemiBold (600), `letter-spacing:-.02em`, `text-wrap:balance`.
- **Texte** : Poppins Regular (400).
- **Labels / éléments UI** : Poppins Medium (500).
- **Eyebrow** : 12px, 600, `letter-spacing:.12em`, majuscules, couleur émeraude.

## 3. Formes & profondeur
- Rayons : cartes `22px`, contrôles `12–16px`, boutons/chips **pill** `999px`.
- Ombres **très douces**, teintées vert-neutre (`--shadow-sm/md/lg`).
- **Glassmorphism léger** : topbar & badges (`backdrop-filter: blur` + `--glass`).
- Pas de gros contours noirs ; bordures à très faible opacité.

## 4. Icônes
SVG **outline** inline (stroke 1.6–1.75, bouts arrondis), sprite `<symbol>` —
plus premium et cohérent que l'emoji, entièrement autonome (aucun CDN).

## 5. Navigation
- **Desktop** : sidebar fixe `270px` — Accueil, Explorer, Produits, Services,
  Espaces, Favoris, Commandes, Messages, Notifications, Profil + carte « Devenir
  partenaire ». État actif = pastille émeraude douce.
- **Mobile** (`≤900px`) : bottom-nav glass — Accueil, Explorer, Favoris,
  Messages, Profil (façon Instagram / Airbnb).

## 6. Composants clés
- **Hero** : panneau arrondi aéré, wash émeraude/nuit subtil, grande searchbar
  flottante, chips de raccourci, chip de localisation.
- **Cartes catégorie** : icône dans carré émeraude arrondi, titre, compteur ;
  au survol montée `-5px`.
- **Cartes offre** : média duotone, badge Ouvert/Fermé (point coloré), cœur
  favori (glass), titre, catégorie · distance, étoiles, prix, bouton
  **Voir / Réserver**. Survol montée `-6px` + ombre `lg`.
- **Carte interactive** : SVG vectoriel (blocs, axes, parc, rivière) + pins
  émeraude ; survol carte ⇄ liste synchronisé.
- **Fiche détail** : grande image, statut ouvert, note, galerie, infos,
  avis, aside sticky (prix + Réserver/Contacter/Appeler + mini-carte).

## 7. Animations (discrètes)
- Apparition des cartes : montée + fondu (`IntersectionObserver`).
- Survol des boutons : `scale(1.03)`.
- Favori : pop `1.35→1`. Validation : toast avec coche tracée (stroke-dashoffset).
- `prefers-reduced-motion` respecté (animations neutralisées).

## 8. À éviter (cadré par la vision produit)
Fond sombre permanent · trop de couleurs · dizaines de boutons · tableaux
partout · look « application de gestion ».

## 9. Portage vers JSF / PrimeFaces
Les tokens (section 1) deviennent des variables CSS globales chargées avant le
thème PrimeFaces ; surcharger les composants PF (`p:card`, `p:inputText`,
`p:commandButton`) via ces variables pour retrouver l'identité. Le prototype
sert de spécification pixel-près pour l'intégration.

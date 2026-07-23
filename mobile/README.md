# Nexora Mobile (Flutter — Android & iOS)

Application mobile de la marketplace Nexora, consommant l'**API REST** du
backend Jakarta EE. Même identité visuelle que le web (émeraude, Poppins,
cartes arrondies, bottom-nav).

## Structure
```
lib/
  main.dart               Point d'entrée + navigation bottom-nav
  theme.dart              Thème Nexora (clair/sombre, tokens émeraude)
  models/                 Offre, Categorie (miroirs des DTO backend)
  services/api_service.dart  Client REST (+ repli hors ligne)
  screens/                Accueil, Explorer (recherche), Détail
  widgets/offer_card.dart Carte d'offre
```

## Prérequis
- Flutter SDK ≥ 3.3 (`flutter --version`)

## Génération des plateformes & lancement
Ce dépôt versionne le code applicatif (`lib/`, `pubspec.yaml`). Générez les
dossiers de plateforme (android/ios) puis lancez :

```bash
cd mobile
flutter create .            # crée android/ + ios/ autour du lib/ existant
flutter pub get
flutter run                 # sur un émulateur/appareil connecté
```

## Connexion à l'API
Par défaut, l'app cible `http://10.0.2.2:8080/nexora/api` (l'hôte depuis
l'émulateur Android). Pour pointer une autre URL :

```bash
flutter run --dart-define=NEXORA_API=https://api.mondomaine.com/nexora/api
```

Si l'API est injoignable, l'app bascule automatiquement sur des données de
démonstration (mode hors ligne) afin de rester utilisable.

## Permission réseau (Android)
Après `flutter create .`, ajouter dans
`android/app/src/main/AndroidManifest.xml`, au-dessus de `<application>` :

```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

## Endpoints consommés
| Écran | Appel |
|---|---|
| Accueil (catégories) | `GET /api/categories` |
| Accueil / Explorer | `GET /api/offres?motCle=…&idCategorie=…&tri=…` |
| Connexion | `POST /api/auth/login` → JWT |
| Inscription | `POST /api/auth/register` → JWT |

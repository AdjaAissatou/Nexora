# Importer et lancer Nexora dans Eclipse

Projet **Maven** (WAR) — Java 17, Jakarta EE 10, JSF/PrimeFaces, JPA/Hibernate,
PostgreSQL. Serveur cible : **Apache TomEE 10 (Plume/Plus)**.

---

## 1. Prérequis à installer

| Outil | Version | Notes |
|-------|---------|-------|
| **JDK** | 17 | Window → Preferences → Java → Installed JREs |
| **Eclipse** | *for Enterprise Java and Web Developers* | inclut m2e (Maven) + WTP (serveurs) |
| **Lombok** | dernière | **obligatoire dans l'IDE** (voir §3) |
| **Apache TomEE** | 10.x Plume ou Plus | serveur Jakarta EE 10 |
| **PostgreSQL** | 14+ | base de données |

---

## 2. Importer le projet

1. **File → Import… → Maven → Existing Maven Projects**
2. *Root Directory* = le dossier contenant `pom.xml` → **Finish**
3. m2e télécharge les dépendances (patiente au 1er import).

> Le projet est déjà versionné (Git). Tu peux aussi faire
> **File → Import → Git → Projects from Git** puis « Import as Maven ».

---

## 3. Activer Lombok dans Eclipse (important)

Le code utilise Lombok (`@Getter`, `@Setter`, `@Named`…). Sans l'agent Lombok,
Eclipse affiche de **fausses erreurs** « méthode getXxx() introuvable »
(le build Maven, lui, fonctionne déjà).

1. Récupère `lombok.jar` (déjà dans ton dépôt Maven :
   `~/.m2/repository/org/projectlombok/lombok/…/lombok.jar`, ou depuis projectlombok.org).
2. Lance-le : `java -jar lombok.jar`
3. Pointe l'installeur sur ton `eclipse.exe` (ou `eclipse.ini`) → **Install / Update**.
4. **Redémarre Eclipse**, puis *Project → Clean…*.

---

## 4. Base de données PostgreSQL

```bash
createdb nexora
psql -d nexora -f docs/mpd/schema.sql
psql -d nexora -f docs/mpd/seed.sql
psql -d nexora -f docs/mpd/seed_catalogue.sql
psql -d nexora -f docs/mpd/seed_demo.sql   # annonces de démo avec photos
```

La datasource attendue est déjà déclarée pour TomEE dans
`src/main/webapp/WEB-INF/resources.xml` (id `nexoraDS`). **Adapte-y
`UserName` / `Password` / `JdbcUrl`** à ta base.

---

## 5. Ajouter TomEE et lancer

1. Vue **Servers** → *New → Server → Apache → Apache TomEE* → indique le
   dossier d'installation de TomEE.
2. Clic droit sur le projet → **Run As → Run on Server** → choisis TomEE.
3. Ouvre : `http://localhost:8080/nexora-platform/accueil.xhtml`
   (le contexte dépend du `finalName` / nom du WAR).

---

## 5 bis. Microservice de reconnaissance d'image (optionnel)

La **recherche par photo** est assurée par un service séparé
(`vision-service/`, MobileNetV2 / ONNX) — dissocié du WAR pour rester léger.
Il n'est nécessaire **que** pour la recherche par photo ; tout le reste
fonctionne sans lui.

```bash
cd vision-service
mvn -q package
java -Xmx512m -jar target/nexora-vision.jar     # écoute sur le port 8090
# test : curl -s --data-binary @photo.jpg http://localhost:8090/recognize
```

L'application le trouve par défaut sur `http://localhost:8090/recognize`
(configurable via la variable d'environnement `NEXORA_VISION_URL`). S'il est
absent, la recherche par photo affiche « Image non reconnue » et le reste de
la recherche continue. Détails : `vision-service/README.md`.

---

## 6. Comptes de démonstration

Chargés par `seed_demo.sql` :

| Rôle | Identifiant | Mot de passe |
|------|-------------|--------------|
| **Administrateur** | `admin@nexora.sn` | `admin123` |

Les comptes **client / professionnel** se créent depuis **Se connecter →
Créer un compte** (l'inscription hache le mot de passe en BCrypt).

---

## 7. Parcours à tester

- **Accueil / Explorer** → recherche réelle sur la base : mot-clé, **filtres**
  (catégorie, prix min/max, tri, vérifiés/disponibles), **« Près de moi »**
  (géolocalisation → tri par proximité), boutons **Itinéraire / Appeler /
  Message**.
- **Recherche par photo** → icône appareil photo dans la barre → une photo
  (sac, chemise, robe, montre…) est reconnue et lance la recherche
  *(nécessite le microservice §5 bis)*.
- **Créer mon espace** → connexion → nom + **nature** (boutique / prestataire
  / les deux). Puis **Mon espace** = back-office : vue d'ensemble, onglets
  **Produits / Services**, ajout via catégories imbriquées + attributs (EAV),
  **Modifier** une annonce en ligne, **Corriger** une annonce rejetée.
- **Messagerie** → depuis *Explorer → Message* : conversation client ↔
  boutique, **badge de messages non lus** (barre du haut + latérale) qui se
  remet à zéro à la lecture, **notification** à la réception d'un message.
- **Admin** (`admin@nexora.sn`) → **valider / rejeter (avec motif)** les
  annonces, **certifier** les espaces.

> L'application a besoin que la datasource `nexoraDS` existe pour **démarrer**
> (les EJB injectent un `EntityManager`).

---

## Dépannage rapide

| Symptôme | Cause / solution |
|----------|------------------|
| Erreurs « getXxx() introuvable » dans l'éditeur | Lombok non installé dans Eclipse (§3) |
| Déploiement échoue sur la persistance | datasource `nexoraDS` absente/mal configurée (§4) |
| Page blanche / 404 | vérifier le contexte du WAR et l'URL `…/accueil.xhtml` |
| Accents mal affichés | s'assurer que la base est en UTF-8 |

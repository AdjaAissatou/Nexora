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

## 6. Parcours à tester

- **Accueil** → non connecté : bouton **Se connecter**, carte *Vous êtes pro ?*.
- **Créer mon espace** → connexion → nom + nature (produits/services).
- **Mon espace** → onglets **Produits / Services** → *Ajouter* → catégorie
  (listes imbriquées) → attributs auto-chargés → **Enregistrer**.
- **Explorer** → recherche (lit la base : nécessite PostgreSQL + seed).

> Le parcours *Mon espace* fonctionne **sans base** (état en mémoire de
> session) ; seule la page **Explorer** interroge la base. L'application a
> toutefois besoin que la datasource `nexoraDS` existe pour **démarrer**
> (les EJB injectent un `EntityManager`).

---

## Dépannage rapide

| Symptôme | Cause / solution |
|----------|------------------|
| Erreurs « getXxx() introuvable » dans l'éditeur | Lombok non installé dans Eclipse (§3) |
| Déploiement échoue sur la persistance | datasource `nexoraDS` absente/mal configurée (§4) |
| Page blanche / 404 | vérifier le contexte du WAR et l'URL `…/accueil.xhtml` |
| Accents mal affichés | s'assurer que la base est en UTF-8 |

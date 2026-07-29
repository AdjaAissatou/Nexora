# Nexora Vision — microservice de reconnaissance d'image

Process autonome qui classe une image avec **MobileNetV2 (ONNX Runtime)** et
renvoie un mot-clé de recherche marchand. Il est **dissocié** du serveur
applicatif (TomEE) : le runtime ONNX (~87 Mo natif) et le modèle vivent ici,
ce qui allège le WAR et permet de scaler la vision indépendamment.

## Construire

```bash
cd vision-service
mvn -q package
# -> target/nexora-vision.jar (jar exécutable autonome, modèle inclus)
```

## Lancer

```bash
java -Xmx512m -jar target/nexora-vision.jar
# écoute sur le port 8090 (configurable via NEXORA_VISION_PORT)
```

## API

| Méthode | Chemin        | Corps                     | Réponse                                   |
|---------|---------------|---------------------------|-------------------------------------------|
| POST    | `/recognize`  | octets d'une image (≤12 Mo) | `{"keyword":"robe","label":"gown"}`     |
| GET     | `/health`     | —                         | `{"status":"UP"}`                         |

Exemple :

```bash
curl -s --data-binary @photo.jpg http://localhost:8090/recognize
# {"keyword":"sac","label":"backpack, back pack, knapsack, ..."}
```

## Branchement côté application

L'application Nexora appelle ce service via `VisionServiceImpl`. L'URL est
configurable :

```bash
export NEXORA_VISION_URL="http://localhost:8090/recognize"   # défaut
```

Si le service est injoignable, la recherche par photo renvoie simplement
« Image non reconnue » — le reste de la recherche continue de fonctionner.

package sn.ucad.nexora.catalogue.infrastructure.vision;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PreDestroy;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Le modèle de vision de la recherche par photo (§11) : DINOv2-small (Meta, licence Apache 2.0),
 * version quantifiée au format ONNX (24,5 Mo), exécuté localement par ONNX Runtime.
 *
 * <p>Il transforme une image en un vecteur de 384 nombres : deux photos du même genre d'article
 * donnent des vecteurs proches (cosinus élevé). Le fichier du modèle est cherché dans
 * {@code NEXORA_MODELS_DIR} (par défaut {@code ~/nexora-models}) ; s'il manque, il est téléchargé
 * une fois depuis Hugging Face, à une révision figée, et son empreinte SHA-256 est vérifiée.
 * Sans modèle, la recherche par photo répond qu'elle est indisponible ; le reste du catalogue
 * fonctionne normalement.
 */
@Component
public class ModeleVision {

    /** Identifiant enregistré avec chaque vecteur : changer de modèle impose de tout recalculer. */
    public static final String NOM = "dinov2-small-q8@c2bb04a";
    public static final int DIMENSION = 384;
    static final int TAILLE = 224;
    static final String FICHIER = "dinov2-small-quantized.onnx";
    static final String URL_PAR_DEFAUT =
            "https://huggingface.co/Xenova/dinov2-small/resolve/c2bb04a51fab207c420665f1946016107bffc701/onnx/model_quantized.onnx";
    static final String SHA256 = "3afdc8bc63b50558d6e5770f5b799bb82455c2311183a2de43803f343a29d917";

    private static final float[] MOYENNE = {0.485f, 0.456f, 0.406f};
    private static final float[] ECART = {0.229f, 0.224f, 0.225f};
    /** Après un échec de chargement, on ne réessaie pas avant ce délai. */
    private static final long ATTENTE_APRES_ECHEC_MS = 10 * 60 * 1000L;
    private static final Logger LOG = LoggerFactory.getLogger(ModeleVision.class);

    private final Path fichier;
    private final String url;
    private volatile OrtEnvironment environnement;
    private volatile OrtSession session;
    private volatile String indisponibilite;
    private volatile long dernierEchec;
    /**
     * Tous les appels à ONNX Runtime passent par ce fil, doté d'une grande pile : avec la pile par
     * défaut d'un fil Java (1 Mo), le chargement du modèle fait planter la JVM (code 139).
     */
    private final java.util.concurrent.ExecutorService fil = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(null, r, "modele-vision", 64L * 1024 * 1024);
        t.setDaemon(true);
        return t;
    });

    public ModeleVision(@Value("${nexora.vision.modele-dir:}") String dossier,
                        @Value("${nexora.vision.modele-url:" + URL_PAR_DEFAUT + "}") String url) {
        this.fichier = dossierModeles(dossier).resolve(FICHIER);
        this.url = url;
    }

    static Path dossierModeles(String configure) {
        String valeur = configure;
        if (valeur == null || valeur.isBlank()) valeur = System.getenv("NEXORA_MODELS_DIR");
        if (valeur == null || valeur.isBlank()) valeur = Paths.get(System.getProperty("user.home"), "nexora-models").toString();
        return Paths.get(valeur).toAbsolutePath().normalize();
    }

    /** Charge le modèle si besoin (téléchargement compris). Renvoie false s'il est indisponible. */
    public boolean pret() {
        if (session != null) return true;
        try {
            return fil.submit(this::charger).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (java.util.concurrent.ExecutionException e) {
            return false;
        }
    }

    private boolean charger() {
        synchronized (this) {
            if (session != null) return true;
            if (dernierEchec > 0 && System.currentTimeMillis() - dernierEchec < ATTENTE_APRES_ECHEC_MS) return false;
            try {
                if (!Files.isRegularFile(fichier)) telecharger();
                environnement = OrtEnvironment.getEnvironment();
                session = environnement.createSession(fichier.toString(), new OrtSession.SessionOptions());
                indisponibilite = null;
                LOG.info("Modèle de vision chargé : {}", fichier);
                return true;
            } catch (Exception | UnsatisfiedLinkError e) {
                dernierEchec = System.currentTimeMillis();
                indisponibilite = e.getMessage();
                LOG.warn("Recherche par photo indisponible : {}", e.getMessage());
                return false;
            }
        }
    }

    /** Pourquoi le modèle n'a pas pu être chargé (null s'il est prêt ou pas encore essayé). */
    public String getIndisponibilite() {
        return indisponibilite;
    }

    private void telecharger() throws IOException, InterruptedException {
        Files.createDirectories(fichier.getParent());
        Path temporaire = Files.createTempFile(fichier.getParent(), "modele-", ".part");
        LOG.info("Téléchargement du modèle de vision (24,5 Mo) vers {}", fichier);
        try {
            HttpClient http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
                    .proxy(ProxySelector.getDefault()).connectTimeout(Duration.ofSeconds(20)).build();
            HttpResponse<InputStream> reponse = http.send(HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofMinutes(10)).GET().build(), HttpResponse.BodyHandlers.ofInputStream());
            if (reponse.statusCode() != 200) throw new IOException("téléchargement du modèle refusé (HTTP " + reponse.statusCode() + ")");
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            try (InputStream in = new DigestInputStream(reponse.body(), sha)) {
                Files.copy(in, temporaire, StandardCopyOption.REPLACE_EXISTING);
            }
            String empreinte = HexFormat.of().formatHex(sha.digest());
            if (URL_PAR_DEFAUT.equals(url) && !SHA256.equals(empreinte)) {
                throw new IOException("empreinte du modèle inattendue (" + empreinte + ")");
            }
            Files.move(temporaire, fichier, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException(e);
        } finally {
            Files.deleteIfExists(temporaire);
        }
    }

    /** Vecteur normalisé (norme 1) de l'image : le produit scalaire de deux vecteurs est leur cosinus. */
    public float[] vecteur(BufferedImage image) throws OrtException {
        if (!pret()) throw new IllegalStateException("modèle de vision indisponible");
        float[] pixels = pretraiter(image);
        try {
            return fil.submit(() -> executer(pixels)).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("analyse interrompue");
        } catch (java.util.concurrent.ExecutionException e) {
            if (e.getCause() instanceof OrtException o) throw o;
            throw new IllegalStateException(e.getCause());
        }
    }

    private float[] executer(float[] pixels) throws OrtException {
        try (OnnxTensor entree = OnnxTensor.createTensor(environnement, FloatBuffer.wrap(pixels), new long[]{1, 3, TAILLE, TAILLE});
             OrtSession.Result sortie = session.run(java.util.Map.of("pixel_values", entree))) {
            float[][][] etats = (float[][][]) sortie.get(0).getValue();
            return normaliser(etats[0][0].clone()); // jeton [CLS] : résumé global de l'image
        }
    }

    static float[] normaliser(float[] v) {
        double n = 0;
        for (float x : v) n += x * x;
        float inverse = (float) (1 / Math.max(Math.sqrt(n), 1e-12));
        for (int i = 0; i < v.length; i++) v[i] *= inverse;
        return v;
    }

    /**
     * L'image est posée au centre d'un carré blanc (sans la rogner : l'article reste entier, même
     * décentré), réduite à 224 × 224, puis normalisée comme à l'entraînement du modèle (ImageNet).
     * Résultat en ordre canal, ligne, colonne.
     */
    static float[] pretraiter(BufferedImage source) {
        int l = source.getWidth(), h = source.getHeight(), c = Math.max(l, h);
        BufferedImage carre = new BufferedImage(c, c, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = carre.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, c, c);
        g.drawImage(source, (c - l) / 2, (c - h) / 2, null);
        g.dispose();
        BufferedImage petite = reduire(carre, TAILLE);
        float[] t = new float[3 * TAILLE * TAILLE];
        int plan = TAILLE * TAILLE;
        for (int y = 0; y < TAILLE; y++) {
            for (int x = 0; x < TAILLE; x++) {
                int rgb = petite.getRGB(x, y), i = y * TAILLE + x;
                t[i] = (((rgb >> 16) & 0xFF) / 255f - MOYENNE[0]) / ECART[0];
                t[plan + i] = (((rgb >> 8) & 0xFF) / 255f - MOYENNE[1]) / ECART[1];
                t[2 * plan + i] = ((rgb & 0xFF) / 255f - MOYENNE[2]) / ECART[2];
            }
        }
        return t;
    }

    /**
     * Redimensionnement bicubique avec anti-crénelage, identique à celui de Pillow (Python) : les seuils
     * de ressemblance ont été mesurés avec lui. Filtre séparable (lignes puis colonnes), noyau de
     * Keys a = -0,5, support élargi du facteur de réduction, arrondi à l'octet entre les deux passes.
     */
    private static BufferedImage reduire(BufferedImage image, int cible) {
        int l = image.getWidth(), h = image.getHeight();
        int[] rgb = image.getRGB(0, 0, l, h, null, 0, l);
        int[][] canaux = new int[3][l * h];
        for (int i = 0; i < rgb.length; i++) {
            canaux[0][i] = (rgb[i] >> 16) & 0xFF;
            canaux[1][i] = (rgb[i] >> 8) & 0xFF;
            canaux[2][i] = rgb[i] & 0xFF;
        }
        Noyau horizontal = new Noyau(l, cible), vertical = new Noyau(h, cible);
        BufferedImage r = new BufferedImage(cible, cible, BufferedImage.TYPE_INT_RGB);
        int[][] sortie = new int[3][];
        for (int c = 0; c < 3; c++) {
            int[] lignes = new int[cible * h];
            for (int y = 0; y < h; y++) horizontal.appliquer(canaux[c], y * l, 1, lignes, y * cible, 1);
            int[] fin = new int[cible * cible];
            for (int x = 0; x < cible; x++) vertical.appliquer(lignes, x, cible, fin, x, cible);
            sortie[c] = fin;
        }
        for (int i = 0; i < cible * cible; i++) r.setRGB(i % cible, i / cible, (sortie[0][i] << 16) | (sortie[1][i] << 8) | sortie[2][i]);
        return r;
    }

    /** Poids du filtre pour une dimension (comme ImagingResample de Pillow). */
    private static final class Noyau {
        private final int[] debut;
        private final double[][] poids;

        Noyau(int entree, int sortie) {
            double echelle = (double) entree / sortie, filtre = Math.max(echelle, 1.0), support = 2.0 * filtre;
            debut = new int[sortie];
            poids = new double[sortie][];
            for (int i = 0; i < sortie; i++) {
                double centre = (i + 0.5) * echelle;
                int min = Math.max((int) (centre - support + 0.5), 0), max = Math.min((int) (centre + support + 0.5), entree);
                double[] w = new double[max - min];
                double total = 0;
                for (int k = 0; k < w.length; k++) {
                    w[k] = bicubique((k + min - centre + 0.5) / filtre);
                    total += w[k];
                }
                for (int k = 0; k < w.length; k++) w[k] = total == 0 ? 0 : w[k] / total;
                debut[i] = min;
                poids[i] = w;
            }
        }

        void appliquer(int[] source, int origine, int pas, int[] cible, int origineCible, int pasCible) {
            for (int i = 0; i < debut.length; i++) {
                double s = 0;
                double[] w = poids[i];
                for (int k = 0; k < w.length; k++) s += source[origine + (debut[i] + k) * pas] * w[k];
                cible[origineCible + i * pasCible] = (int) Math.max(0, Math.min(255, Math.round(s)));
            }
        }

        private static double bicubique(double x) {
            double a = -0.5;
            x = Math.abs(x);
            if (x < 1) return ((a + 2) * x - (a + 3)) * x * x + 1;
            if (x < 2) return (((x - 5) * x + 8) * x - 4) * a;
            return 0;
        }
    }

    @PreDestroy
    void fermer() throws OrtException {
        fil.shutdownNow();
        if (session != null) session.close();
    }
}

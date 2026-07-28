package com.nexora.service.impl;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import com.nexora.service.VisionService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation de la reconnaissance d'image avec ONNX Runtime et le modele
 * MobileNetV2 (ImageNet, 1000 classes) embarque dans le WAR. L'inference est
 * 100 % locale (aucun appel a un service externe). La classe reconnue est
 * ensuite traduite en mot-cle marchand francais.
 */
@Singleton
@Startup
public class VisionServiceImpl implements VisionService {

    private static final Logger LOG = Logger.getLogger(VisionServiceImpl.class.getName());
    private static final int TAILLE = 224;
    private static final float[] MEAN = {0.485f, 0.456f, 0.406f};
    private static final float[] STD  = {0.229f, 0.224f, 0.225f};

    private OrtEnvironment env;
    private OrtSession session;
    private String inputName;
    private List<String> labels = List.of();

    /** ImageNet (anglais) -> mot-cle marchand (francais). Premier motif trouve gagne. */
    private static final Map<String, String> CORRESPONDANCES = new LinkedHashMap<>();
    static {
        CORRESPONDANCES.put("wallet", "portefeuille");
        CORRESPONDANCES.put("purse", "sac");
        CORRESPONDANCES.put("backpack", "sac");
        CORRESPONDANCES.put("mailbag", "sac");
        CORRESPONDANCES.put("bag", "sac");
        CORRESPONDANCES.put("shirt", "chemise");
        CORRESPONDANCES.put("jersey", "t-shirt");
        CORRESPONDANCES.put("sweatshirt", "sweat");
        CORRESPONDANCES.put("cardigan", "pull");
        CORRESPONDANCES.put("sweater", "pull");
        CORRESPONDANCES.put("suit", "costume");
        CORRESPONDANCES.put("gown", "robe");
        CORRESPONDANCES.put("dress", "robe");
        CORRESPONDANCES.put("kimono", "robe");
        CORRESPONDANCES.put("abaya", "robe");
        CORRESPONDANCES.put("jean", "jean");
        CORRESPONDANCES.put("skirt", "jupe");
        CORRESPONDANCES.put("trench", "manteau");
        CORRESPONDANCES.put("coat", "manteau");
        CORRESPONDANCES.put("poncho", "veste");
        CORRESPONDANCES.put("running shoe", "chaussures");
        CORRESPONDANCES.put("sandal", "chaussures");
        CORRESPONDANCES.put("clog", "chaussures");
        CORRESPONDANCES.put("loafer", "chaussures");
        CORRESPONDANCES.put("boot", "chaussures");
        CORRESPONDANCES.put("sneaker", "chaussures");
        CORRESPONDANCES.put("shoe", "chaussures");
        CORRESPONDANCES.put("watch", "montre");
        CORRESPONDANCES.put("sunglass", "lunettes");
        CORRESPONDANCES.put("cellular", "téléphone");
        CORRESPONDANCES.put("telephone", "téléphone");
        CORRESPONDANCES.put("ipod", "téléphone");
        CORRESPONDANCES.put("hand-held computer", "téléphone");
        CORRESPONDANCES.put("laptop", "ordinateur");
        CORRESPONDANCES.put("notebook", "ordinateur");
        CORRESPONDANCES.put("desktop computer", "ordinateur");
        CORRESPONDANCES.put("computer keyboard", "clavier");
        CORRESPONDANCES.put("mouse", "souris");
        CORRESPONDANCES.put("headphone", "casque");
        CORRESPONDANCES.put("earphone", "écouteurs");
        CORRESPONDANCES.put("microphone", "microphone");
        CORRESPONDANCES.put("camera", "appareil photo");
        CORRESPONDANCES.put("television", "télévision");
        CORRESPONDANCES.put("monitor", "écran");
        CORRESPONDANCES.put("screen", "écran");
        CORRESPONDANCES.put("refrigerator", "réfrigérateur");
        CORRESPONDANCES.put("washer", "lave-linge");
        CORRESPONDANCES.put("microwave", "micro-ondes");
        CORRESPONDANCES.put("stove", "cuisinière");
        CORRESPONDANCES.put("chair", "chaise");
        CORRESPONDANCES.put("rocking chair", "chaise");
        CORRESPONDANCES.put("desk", "bureau");
        CORRESPONDANCES.put("dining table", "table");
        CORRESPONDANCES.put("table", "table");
        CORRESPONDANCES.put("couch", "canapé");
        CORRESPONDANCES.put("studio couch", "canapé");
        CORRESPONDANCES.put("wardrobe", "armoire");
        CORRESPONDANCES.put("lamp", "lampe");
        CORRESPONDANCES.put("bicycle", "vélo");
        CORRESPONDANCES.put("mountain bike", "vélo");
        CORRESPONDANCES.put("motor scooter", "moto");
        CORRESPONDANCES.put("moped", "moto");
        CORRESPONDANCES.put("sports car", "voiture");
        CORRESPONDANCES.put("convertible", "voiture");
        CORRESPONDANCES.put("limousine", "voiture");
        CORRESPONDANCES.put("minivan", "voiture");
        CORRESPONDANCES.put("jeep", "voiture");
        CORRESPONDANCES.put("cab", "voiture");
        CORRESPONDANCES.put("pickup", "voiture");
        CORRESPONDANCES.put("perfume", "parfum");
        CORRESPONDANCES.put("lotion", "cosmétique");
        CORRESPONDANCES.put("sunscreen", "cosmétique");
        CORRESPONDANCES.put("hair spray", "cosmétique");
        CORRESPONDANCES.put("water bottle", "bouteille");
        CORRESPONDANCES.put("pop bottle", "bouteille");
        CORRESPONDANCES.put("book", "livre");
        CORRESPONDANCES.put("necklace", "collier");
        CORRESPONDANCES.put("ring", "bijou");
    }

    @PostConstruct
    void init() {
        try {
            env = OrtEnvironment.getEnvironment();
            byte[] modele = lireRessourceBinaire("/models/mobilenetv2.onnx");
            session = env.createSession(modele, new OrtSession.SessionOptions());
            inputName = session.getInputNames().iterator().next();
            labels = lireLabels("/models/imagenet-synset.txt");
            LOG.info("VisionService prêt : MobileNetV2 chargé (" + labels.size() + " classes).");
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, "Échec du chargement du modèle de vision", t);
            session = null;
        }
    }

    @PreDestroy
    void fermer() {
        try { if (session != null) session.close(); } catch (Exception ignore) { }
    }

    @Override
    @Lock(LockType.READ)
    public String motCleDepuisImage(byte[] image) {
        if (session == null || image == null || image.length == 0) return null;
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(image));
            if (img == null) return null;
            float[] donnees = pretraiter(img);
            long[] forme = {1, 3, TAILLE, TAILLE};
            try (OnnxTensor tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(donnees), forme);
                 OrtSession.Result res = session.run(Map.of(inputName, tensor))) {
                float[][] sortie = (float[][]) res.get(0).getValue();
                int meilleur = argmax(sortie[0]);
                if (meilleur < 0 || meilleur >= labels.size()) return null;
                return motCleFr(labels.get(meilleur));
            }
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "Reconnaissance d'image échouée", t);
            return null;
        }
    }

    /** Redimensionne en 224x224, normalise (ImageNet) et remet en NCHW. */
    private float[] pretraiter(BufferedImage source) {
        BufferedImage img = new BufferedImage(TAILLE, TAILLE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, TAILLE, TAILLE, null);
        g.dispose();

        int plan = TAILLE * TAILLE;
        float[] data = new float[3 * plan];
        for (int y = 0; y < TAILLE; y++) {
            for (int x = 0; x < TAILLE; x++) {
                int rgb = img.getRGB(x, y);
                float r = ((rgb >> 16) & 0xff) / 255f;
                float gr = ((rgb >> 8) & 0xff) / 255f;
                float b = (rgb & 0xff) / 255f;
                int idx = y * TAILLE + x;
                data[idx]            = (r  - MEAN[0]) / STD[0];
                data[plan + idx]     = (gr - MEAN[1]) / STD[1];
                data[2 * plan + idx] = (b  - MEAN[2]) / STD[2];
            }
        }
        return data;
    }

    private static int argmax(float[] v) {
        int best = 0;
        for (int i = 1; i < v.length; i++) if (v[i] > v[best]) best = i;
        return best;
    }

    /** Traduit un label ImageNet en mot-cle marchand ; sinon le 1er terme anglais. */
    private static String motCleFr(String label) {
        String bas = label.toLowerCase();
        for (Map.Entry<String, String> e : CORRESPONDANCES.entrySet()) {
            if (bas.contains(e.getKey())) return e.getValue();
        }
        // Repli : premier synonyme du label (avant la virgule).
        int virgule = label.indexOf(',');
        return (virgule > 0 ? label.substring(0, virgule) : label).trim();
    }

    private static byte[] lireRessourceBinaire(String chemin) throws Exception {
        try (InputStream in = VisionServiceImpl.class.getResourceAsStream(chemin)) {
            if (in == null) throw new IllegalStateException("Ressource introuvable : " + chemin);
            return in.readAllBytes();
        }
    }

    private static List<String> lireLabels(String chemin) throws Exception {
        List<String> out = new ArrayList<>();
        try (InputStream in = VisionServiceImpl.class.getResourceAsStream(chemin);
             BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                if (ligne.isBlank()) continue;
                int esp = ligne.indexOf(' ');   // retire l'id synset « n01440764 »
                out.add(esp > 0 ? ligne.substring(esp + 1).trim() : ligne.trim());
            }
        }
        return out;
    }
}

package sn.ucad.nexora.catalogue.infrastructure.vision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class ModeleVisionTest {

    private static final int PLAN = ModeleVision.TAILLE * ModeleVision.TAILLE;

    @Test
    void imageBlancheDonneLaValeurNormaliseeDuBlanc() {
        BufferedImage blanche = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = blanche.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 300, 300);
        g.dispose();
        float[] t = ModeleVision.pretraiter(blanche);
        assertEquals(3 * PLAN, t.length);
        assertEquals((1 - 0.485f) / 0.229f, t[0], 1e-4);
        assertEquals((1 - 0.456f) / 0.224f, t[PLAN], 1e-4);
        assertEquals((1 - 0.406f) / 0.225f, t[2 * PLAN + PLAN - 1], 1e-4);
    }

    @Test
    void imageEnLargeurEstPoseeSurUnCarreBlancSansEtreRognee() {
        BufferedImage noire = new BufferedImage(400, 100, BufferedImage.TYPE_INT_RGB); // noire par défaut
        float[] t = ModeleVision.pretraiter(noire);
        float blanc = (1 - 0.485f) / 0.229f, noir = (0 - 0.485f) / 0.229f;
        assertEquals(blanc, t[0], 1e-3);                                  // coin haut : bande blanche ajoutée
        int milieu = (ModeleVision.TAILLE / 2) * ModeleVision.TAILLE;
        assertEquals(noir, t[milieu], 1e-3);                              // bord gauche au milieu : l'image entière est là
        assertEquals(noir, t[milieu + ModeleVision.TAILLE - 1], 1e-3);    // bord droit aussi
    }

    @Test
    void normaliserDonneUnVecteurDeNormeUn() {
        float[] v = ModeleVision.normaliser(new float[]{3, 4});
        assertEquals(0.6f, v[0], 1e-6);
        assertEquals(0.8f, v[1], 1e-6);
    }

    /**
     * Parité avec le prototype Python qui a servi à mesurer les seuils : lancé seulement si
     * -Dnexora.test.parite=chemin/parite.csv (photo TAB vecteur Python) et le modèle sont présents.
     */
    @Test
    void memesVecteursQueLePrototype() throws Exception {
        String csv = System.getProperty("nexora.test.parite");
        Assumptions.assumeTrue(csv != null && Files.exists(Paths.get(csv)));
        ModeleVision modele = new ModeleVision(System.getProperty("nexora.test.modeles", ""), ModeleVision.URL_PAR_DEFAUT);
        Assumptions.assumeTrue(modele.pret());
        Path dossier = Paths.get(csv).getParent();
        List<String> lignes = Files.readAllLines(Paths.get(csv));
        double pire = 1;
        for (String l : lignes) {
            String[] p = l.split("\t");
            float[] attendu = new float[ModeleVision.DIMENSION];
            String[] nb = p[1].split(",");
            for (int i = 0; i < nb.length; i++) attendu[i] = Float.parseFloat(nb[i]);
            float[] obtenu = modele.vecteur(SourceImages.decoder(Files.readAllBytes(dossier.resolve(p[0]))));
            double c = 0;
            for (int i = 0; i < attendu.length; i++) c += attendu[i] * obtenu[i];
            System.out.printf("parité %.4f  %s%n", c, p[0]);
            pire = Math.min(pire, c);
        }
        assertTrue(pire > 0.99, "cosinus minimal " + pire);
    }
}

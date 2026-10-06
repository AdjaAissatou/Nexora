package sn.ucad.nexora.web.bean;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.primefaces.event.FileUploadEvent;
import sn.ucad.nexora.web.client.CatalogueApiClient;
import sn.ucad.nexora.web.dto.catalogue.RechercheVisuelleDtos.Reponse;
import sn.ucad.nexora.web.dto.catalogue.RechercheVisuelleDtos.Resultat;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.util.FormatBean;

/**
 * Recherche par photo ({@code recherche-photo.xhtml}, §11) : le visiteur choisit ou prend une photo,
 * catalogue-service renvoie les offres qui lui ressemblent, de toutes les boutiques.
 */
@Named
@ViewScoped
public class RecherchePhotoBean implements Serializable {

    /** Au-dessus, l'aperçu est une copie réduite (au-dessous, la photo telle quelle, WebP compris). */
    private static final int APERCU_DIRECT_MAX = 1_500_000;
    /** Ressemblance à partir de laquelle on considère qu'il s'agit probablement du même article. */
    private static final double MEME_ARTICLE = 0.6;

    @Inject
    private transient CatalogueApiClient catalogue;

    @Inject
    private FormatBean format;

    private String apercu;
    private List<Resultat> resultats;
    private String erreur;
    private long imagesEnAttente;

    public void envoyer(FileUploadEvent event) {
        byte[] photo = event.getFile().getContent();
        apercu = apercu(photo, event.getFile().getContentType());
        resultats = null;
        erreur = null;
        try {
            Reponse r = catalogue.rechercherParPhoto(photo, event.getFile().getFileName());
            resultats = r.resultats();
            imagesEnAttente = r.imagesEnAttente();
        } catch (ApiException e) {
            erreur = e.getMessage();
            apercu = null; // une photo refusée (fichier illisible…) ne s'affiche pas en image cassée
        }
    }

    private static String apercu(byte[] photo, String type) {
        if (photo.length <= APERCU_DIRECT_MAX) {
            String mime = type == null || !type.startsWith("image/") ? "image/jpeg" : type;
            return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(photo);
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(photo));
            if (image == null) return null;
            double f = 480.0 / Math.max(image.getWidth(), image.getHeight());
            int l = Math.max(1, (int) (image.getWidth() * f)), h = Math.max(1, (int) (image.getHeight() * f));
            BufferedImage petite = new BufferedImage(l, h, BufferedImage.TYPE_INT_RGB);
            petite.getGraphics().drawImage(image.getScaledInstance(l, h, Image.SCALE_SMOOTH), 0, 0, null);
            ByteArrayOutputStream sortie = new ByteArrayOutputStream();
            ImageIO.write(petite, "jpg", sortie);
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(sortie.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    /** « Des articles très ressemblants sont proposés par 3 espaces, de … à … » ; null s'il n'y en a qu'un. */
    public String getComparaison() {
        if (resultats == null) return null;
        List<Resultat> memes = resultats.stream().filter(r -> r.ressemblance() >= MEME_ARTICLE && r.offre().prix() != null).toList();
        long espaces = memes.stream().map(r -> r.offre().espaceId()).filter(Objects::nonNull).distinct().count();
        if (espaces < 2) return null;
        BigDecimal min = memes.stream().map(r -> r.offre().prix()).min(BigDecimal::compareTo).orElseThrow();
        BigDecimal max = memes.stream().map(r -> r.offre().prix()).max(BigDecimal::compareTo).orElseThrow();
        String prix = min.compareTo(max) == 0 ? "au même prix (" + format.prix(min) + ")" : "de " + format.prix(min) + " à " + format.prix(max);
        return "Des articles très ressemblants sont proposés par " + espaces + " espaces, " + prix + " : comparez avant de vous déplacer.";
    }

    // Filtres des résultats (§11) : appliqués sur place, sans relancer la recherche
    private String tri = "RESSEMBLANCE";
    private java.math.BigDecimal prixMax;
    private String commune;
    private boolean ouvert;
    private boolean verifie;
    private boolean tresRessemblants;

    /** Résultats après filtres et tri. */
    public List<Resultat> getResultatsFiltres() {
        if (resultats == null) return List.of();
        java.util.stream.Stream<Resultat> flux = resultats.stream()
                .filter(r -> prixMax == null || r.offre().prix() == null || r.offre().prix().compareTo(prixMax) <= 0)
                .filter(r -> commune == null || commune.isBlank() || commune.equals(r.offre().commune()))
                .filter(r -> !ouvert || Boolean.TRUE.equals(r.offre().espaceOuvertMaintenant()))
                .filter(r -> !verifie || r.offre().espaceVerifie())
                .filter(r -> !tresRessemblants || r.ressemblance() >= MEME_ARTICLE);
        java.util.Comparator<Resultat> ordre = switch (tri) {
            case "PRIX_ASC" -> java.util.Comparator.comparing(r -> r.offre().prix(), java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
            case "PRIX_DESC" -> java.util.Comparator.comparing((Resultat r) -> r.offre().prix(), java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())).reversed();
            case "NOTE" -> java.util.Comparator.comparing((Resultat r) -> r.offre().espaceNoteMoyenne(), java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())).reversed();
            default -> java.util.Comparator.comparingDouble(Resultat::ressemblance).reversed();
        };
        return flux.sorted(ordre).toList();
    }

    /** Quartiers des résultats, pour le filtre. */
    public List<String> getCommunes() {
        if (resultats == null) return List.of();
        return resultats.stream().map(r -> r.offre().commune()).filter(c -> c != null && !c.isBlank()).distinct().sorted().toList();
    }

    public String getTri() { return tri; }
    public void setTri(String tri) { this.tri = tri == null || tri.isBlank() ? "RESSEMBLANCE" : tri; }
    public java.math.BigDecimal getPrixMax() { return prixMax; }
    public void setPrixMax(java.math.BigDecimal prixMax) { this.prixMax = prixMax; }
    public String getCommune() { return commune; }
    public void setCommune(String commune) { this.commune = commune; }
    public boolean isOuvert() { return ouvert; }
    public void setOuvert(boolean ouvert) { this.ouvert = ouvert; }
    public boolean isVerifie() { return verifie; }
    public void setVerifie(boolean verifie) { this.verifie = verifie; }
    public boolean isTresRessemblants() { return tresRessemblants; }
    public void setTresRessemblants(boolean tresRessemblants) { this.tresRessemblants = tresRessemblants; }

    public boolean isRecherchee() { return resultats != null || erreur != null; }
    public boolean isAucunResultat() { return resultats != null && resultats.isEmpty(); }
    public String getApercu() { return apercu; }
    public List<Resultat> getResultats() { return resultats; }
    public String getErreur() { return erreur; }
    public long getImagesEnAttente() { return imagesEnAttente; }
}

package sn.ucad.nexora.web.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Petites fonctions de formatage exposées à l'EL des pages ({@code #{format.xxx}}). */
@Named("format")
@ApplicationScoped
public class FormatBean implements Serializable {

    private static final String INDICATIF_SENEGAL = "221";

    private String chiffres(String telephone) {
        if (telephone == null) return "";
        String c = telephone.replaceAll("\\D", "");
        if (c.startsWith("00")) c = c.substring(2);
        return c;
    }

    private String international(String telephone) {
        String c = chiffres(telephone);
        return c.length() == 9 ? INDICATIF_SENEGAL + c : c;
    }

    /** `771234567` → `+221 77 123 45 67`. */
    public String telephoneAffiche(String telephone) {
        String intl = international(telephone);
        if (intl.length() == 12 && intl.startsWith(INDICATIF_SENEGAL)) {
            String l = intl.substring(3);
            return "+221 " + l.substring(0, 2) + " " + l.substring(2, 5) + " " + l.substring(5, 7) + " " + l.substring(7);
        }
        return telephone == null ? "" : telephone.trim();
    }

    public String lienTelephone(String telephone) {
        return "tel:+" + international(telephone);
    }

    /** WhatsApp est le canal de contact n°1 au Sénégal. */
    public String lienWhatsApp(String telephone) {
        return "https://wa.me/" + international(telephone);
    }

    public String lienSiteWeb(String site) {
        if (site == null) return "";
        return site.matches("(?i)^https?://.*") ? site : "https://" + site;
    }

    private static final DecimalFormat PRIX_FORMAT;
    static {
        DecimalFormatSymbols symboles = new DecimalFormatSymbols(Locale.FRANCE);
        symboles.setGroupingSeparator(' ');
        PRIX_FORMAT = new DecimalFormat("#,##0", symboles);
    }

    /** `95000.00` → `95 000 FCFA` — pas de décimales, espace fine comme séparateur de milliers. */
    public String prix(BigDecimal montant) {
        if (montant == null) return "";
        return PRIX_FORMAT.format(montant) + " FCFA";
    }

    private static final java.time.format.DateTimeFormatter DATE_HEURE =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
    private static final java.time.format.DateTimeFormatter DATE =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** `2026-09-29T14:05` → `29/09/2026 à 14:05`. */
    public String dateHeure(java.time.LocalDateTime date) {
        return date == null ? "" : DATE_HEURE.format(date);
    }

    /** `2026-09-29T14:05` → `29/09/2026`. */
    /** Note sur 5 en étoiles pleines et vides : 4 → « ★★★★☆ ». */
    public String etoiles(int note) {
        int n = Math.max(0, Math.min(5, note));
        return "★".repeat(n) + "☆".repeat(5 - n);
    }

    public String date(java.time.LocalDateTime date) {
        return date == null ? "" : DATE.format(date);
    }

    /** Taille de fichier lisible : `245 Ko`, `1,2 Mo`. */
    public String taille(Long octets) {
        if (octets == null) return "";
        if (octets < 1024) return octets + " o";
        if (octets < 1024 * 1024) return Math.round(octets / 1024.0) + " Ko";
        return String.format(Locale.FRANCE, "%.1f Mo", octets / (1024.0 * 1024.0));
    }

    public int getAnneeCourante() {
        return java.time.Year.now().getValue();
    }

    /** Lien "obtenir l'itinéraire" vers Google Maps — aucune clé d'API requise, s'ouvre dans l'appli Maps du visiteur. */
    public String lienItineraire(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) return "";
        return String.format(Locale.ROOT, "https://www.google.com/maps/dir/?api=1&destination=%s,%s", latitude, longitude);
    }

    /** Carte embarquée OpenStreetMap (gratuite, sans clé d'API) centrée sur le point avec un repère. */
    public String lienCarteEmbed(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) return "";
        double lat = latitude.doubleValue();
        double lon = longitude.doubleValue();
        double delta = 0.01;
        return String.format(
                Locale.ROOT,
                "https://www.openstreetmap.org/export/embed.html?bbox=%s,%s,%s,%s&layer=mapnik&marker=%s,%s",
                lon - delta, lat - delta, lon + delta, lat + delta, lat, lon);
    }
}

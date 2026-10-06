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
    /**
     * Ressemblance d'une photo (cosinus de 0 à 1, §11), en mots plutôt qu'en pourcentage : le score
     * classe les résultats mais n'est pas une probabilité.
     */
    public String ressemblance(Double score) {
        if (score == null) return "";
        if (score >= 0.6) return "Très ressemblant";
        if (score >= 0.45) return "Ressemblant";
        return "Proche";
    }

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
    /** « samedi 10 octobre » (année ajoutée si ce n'est pas l'année en cours). */
    public String dateLongue(java.time.LocalDate date) {
        if (date == null) return "";
        String motif = date.getYear() == java.time.LocalDate.now().getYear() ? "EEEE d MMMM" : "EEEE d MMMM yyyy";
        return date.format(java.time.format.DateTimeFormatter.ofPattern(motif, java.util.Locale.FRENCH));
    }

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
    /** Un vrai numéro sénégalais (9 chiffres commençant par 7 ou 3, +221 facultatif) ? Une adresse e-mail n'en est pas un. */
    public boolean telephoneValide(String telephone) {
        return telephone != null && telephone.trim().matches("(\\+221|00221)?[ .-]*[37][0-9]([ .-]*[0-9]){7}");
    }

    /** « 77 123 45 67 » → « +221771234567 » (liens tel: et WhatsApp). */
    public String telephoneInternational(String telephone) {
        if (telephone == null) return "";
        String chiffres = telephone.replaceAll("\\D", "").replaceFirst("^00", "");
        return "+" + (chiffres.length() == 9 ? "221" + chiffres : chiffres);
    }

    /** « à 350 m », « à 2,1 km » ; vide sans distance. */
    public String distance(Double km) {
        if (km == null) return "";
        if (km < 1) return "à " + Math.max(10, Math.round(km * 100) * 10) + " m";
        return "à " + String.format(Locale.FRANCE, "%.1f", km) + " km";
    }

    /** Libellé d'un type de lieu public : MARCHE → « Marché ». */
    public String typeLieu(String type) {
        if (type == null) return "";
        return switch (type) {
            case "MARCHE" -> "Marché";
            case "HOPITAL" -> "Hôpital";
            case "ADMINISTRATION" -> "Administration";
            case "MOSQUEE" -> "Mosquée";
            case "MONUMENT" -> "Monument";
            case "GARE_ROUTIERE" -> "Gare routière";
            case "EGLISE" -> "Église";
            case "AEROPORT" -> "Aéroport";
            case "UNIVERSITE" -> "Université";
            case "PHARMACIE" -> "Pharmacie";
            case "PLAGE" -> "Plage";
            case "GARE" -> "Gare";
            case "BANQUE" -> "Banque";
            case "STADE" -> "Stade";
            case "PORT" -> "Port";
            case "QUARTIER" -> "Quartier";
            case "CENTRE_COMMERCIAL" -> "Centre commercial";
            default -> type.charAt(0) + type.substring(1).toLowerCase(Locale.ROOT).replace('_', ' ');
        };
    }

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

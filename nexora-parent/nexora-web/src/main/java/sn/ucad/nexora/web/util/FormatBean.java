package sn.ucad.nexora.web.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;

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

    public int getAnneeCourante() {
        return java.time.Year.now().getValue();
    }
}

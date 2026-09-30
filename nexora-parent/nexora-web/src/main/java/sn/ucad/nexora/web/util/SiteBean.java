package sn.ucad.nexora.web.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.ucad.nexora.web.client.ParametresApiClient;

/**
 * Paramètres publics de Nexora pour toutes les pages ({@code #{site.xxx}}) : bandeau d'annonce, contact
 * du pied de page, inscriptions ouvertes, délai de vérification (§9.12). Relus au plus toutes les
 * minutes : une modification du back-office apparaît donc sur le site en moins d'une minute. Si
 * administration-service ne répond pas, les dernières valeurs connues (ou les défauts) sont gardées.
 */
@Named("site")
@ApplicationScoped
public class SiteBean {

    private static final Logger LOG = LoggerFactory.getLogger(SiteBean.class);
    private static final long DUREE_MS = 60_000;

    @Inject
    private ParametresApiClient client;

    private volatile Map<String, String> valeurs = Map.of();
    private volatile long lu;

    private String valeur(String code) {
        long maintenant = System.currentTimeMillis();
        if (maintenant - lu > DUREE_MS) {
            lu = maintenant;
            try {
                valeurs = client.publics();
            } catch (Exception e) {
                LOG.warn("Paramètres publics indisponibles : {}", e.getMessage());
            }
        }
        String v = valeurs == null ? null : valeurs.get(code);
        return v == null || v.isBlank() ? null : v.trim();
    }

    /** Oublie les valeurs lues : la prochaine page relit les paramètres (après une modification). */
    public void rafraichir() {
        lu = 0;
    }

    public String getBandeau() {
        return valeur("SITE_BANDEAU");
    }

    public String getContactEmail() {
        String v = valeur("SITE_CONTACT_EMAIL");
        return v == null ? "contact@nexora.sn" : v;
    }

    public String getContactTelephone() {
        return valeur("SITE_CONTACT_TELEPHONE");
    }

    public boolean isInscriptionsOuvertes() {
        return !"false".equals(valeur("INSCRIPTIONS_OUVERTES"));
    }

    public int getAvisLongueurMax() {
        return entier(valeur("AVIS_LONGUEUR_MAX"), 1000);
    }

    public int getDelaiVerification() {
        return entier(valeur("VERIFICATION_DELAI_JOURS"), 5);
    }

    private static int entier(String v, int defaut) {
        try {
            return v == null ? defaut : (int) Double.parseDouble(v);
        } catch (NumberFormatException e) {
            return defaut;
        }
    }
}

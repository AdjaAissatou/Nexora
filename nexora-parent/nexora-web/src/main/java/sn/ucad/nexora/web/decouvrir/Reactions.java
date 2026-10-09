package sn.ucad.nexora.web.decouvrir;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import sn.ucad.nexora.web.config.GatewayConfig;
import sn.ucad.nexora.web.session.SessionBean;

/**
 * « J'aime » partout sur Nexora (docs/architecture-acteurs.md §23, §24) : dans Découvrir, sur la fiche
 * d'une offre et sur celle d'un espace, c'est le même signal J_AIME du même visiteur — compté une
 * fois, retiré au même endroit. Le visiteur est « c-&lt;compte&gt; » une fois connecté (son historique
 * anonyme y est rattaché une fois par session), sinon l'identifiant anonyme du cookie, créé au besoin.
 */
public final class Reactions {

    private static final Logger LOG = LoggerFactory.getLogger(Reactions.class);
    static final String COOKIE = "nx_visiteur";
    private static final String FUSION_FAITE = "nexora.decouvrir.fusion";
    private static final RestClient CATALOGUE = RestClient.builder()
            .baseUrl(GatewayConfig.gatewayUrl() + "/catalogue-service/api/v1/decouvrir").build();

    private Reactions() {}

    public static String visiteur(HttpServletRequest requete, HttpServletResponse reponse) {
        String anonyme = null;
        if (requete.getCookies() != null) {
            for (Cookie c : requete.getCookies()) {
                if (COOKIE.equals(c.getName()) && c.getValue() != null && c.getValue().matches("[A-Za-z0-9-]{8,64}")) anonyme = c.getValue();
            }
        }
        if (anonyme == null) {
            anonyme = UUID.randomUUID().toString();
            Cookie cookie = new Cookie(COOKIE, anonyme);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24 * 365);
            cookie.setHttpOnly(true);
            cookie.setAttribute("SameSite", "Lax");
            reponse.addCookie(cookie);
        }
        SessionBean s = session();
        if (s == null || !s.isConnecte() || s.getCompte() == null || s.getCompte().id() == null) return anonyme;
        String compte = "c-" + s.getCompte().id();
        HttpSession httpSession = requete.getSession();
        if (httpSession.getAttribute(FUSION_FAITE) == null) {
            httpSession.setAttribute(FUSION_FAITE, Boolean.TRUE);
            String ancien = anonyme;
            try {
                CATALOGUE.post().uri(u -> u.path("/fusion").queryParam("ancien", ancien).queryParam("nouveau", compte).build())
                        .retrieve().toBodilessEntity();
            } catch (Exception e) {
                LOG.info("Historique anonyme non rattaché : {}", e.getMessage());
            }
        }
        return compte;
    }

    /** Le visiteur de la requête JSF en cours (fiche offre, fiche espace). */
    public static String visiteurCourant() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        return visiteur((HttpServletRequest) ec.getRequest(), (HttpServletResponse) ec.getResponse());
    }

    /** Ce visiteur aime-t-il cette offre (idEspace null) ou cet espace (idOffre null) ? Faux si on ne sait pas. */
    public static boolean aime(String visiteur, Long idOffre, Long idEspace) {
        try {
            Map<?, ?> r = CATALOGUE.get().uri(u -> u.path("/signaux/j-aime").queryParam("visiteur", visiteur)
                    .queryParamIfPresent("idOffre", Optional.ofNullable(idOffre))
                    .queryParamIfPresent("idEspace", Optional.ofNullable(idEspace)).build()).retrieve().body(Map.class);
            return r != null && Boolean.TRUE.equals(r.get("aime"));
        } catch (Exception e) {
            return false;
        }
    }

    /** Aimer ou ne plus aimer ; vrai si c'est enregistré. */
    public static boolean aimer(String visiteur, Long idOffre, Long idEspace, boolean aime) {
        try {
            if (aime) {
                Map<String, Object> corps = new LinkedHashMap<>();
                corps.put("visiteur", visiteur);
                corps.put("idOffre", idOffre);
                corps.put("idEspace", idEspace);
                corps.put("type", "J_AIME");
                CATALOGUE.post().uri("/signaux").contentType(MediaType.APPLICATION_JSON).body(corps).retrieve().toBodilessEntity();
            } else {
                CATALOGUE.delete().uri(u -> u.path("/signaux/j-aime").queryParam("visiteur", visiteur)
                        .queryParamIfPresent("idOffre", Optional.ofNullable(idOffre))
                        .queryParamIfPresent("idEspace", Optional.ofNullable(idEspace)).build()).retrieve().toBodilessEntity();
            }
            return true;
        } catch (Exception e) {
            LOG.info("J'aime non enregistré : {}", e.getMessage());
            return false;
        }
    }

    /**
     * La session du compte, telle que la voient les pages JSF : un bean CDI (Weld), et non Spring —
     * une injection Spring en créerait une autre, vide.
     */
    static SessionBean session() {
        try {
            return CDI.current().select(SessionBean.class).get();
        } catch (RuntimeException e) {
            return null;
        }
    }
}

package sn.ucad.nexora.web.config;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;

/**
 * En-têtes ajoutés à chaque appel du web vers la Gateway. {@code X-Nexora-Client-IP} porte l'adresse
 * du navigateur : sans lui, les services ne verraient que l'adresse du serveur web, et le journal
 * des actions du back-office (nexora-common {@code JournalActions}) ne saurait pas d'où vient
 * l'action. Valeur indicative : un appel direct à la Gateway peut la fixer lui-même.
 */
public final class EnTetesClient {

    public static final String EN_TETE_IP_CLIENT = "X-Nexora-Client-IP";
    /** User-Agent du navigateur (historique des connexions, journal). */
    public static final String EN_TETE_AGENT_CLIENT = "X-Nexora-Client-Agent";

    private EnTetesClient() {}

    public static final ClientHttpRequestInterceptor IP_NAVIGATEUR = (requete, corps, execution) -> {
        FacesContext contexte = FacesContext.getCurrentInstance();
        if (contexte != null && contexte.getExternalContext().getRequest() instanceof HttpServletRequest http) {
            String relais = http.getHeader("X-Forwarded-For");
            String ip = relais == null || relais.isBlank() ? http.getRemoteAddr() : relais.split(",")[0].trim();
            requete.getHeaders().set(EN_TETE_IP_CLIENT, ip);
            String agent = http.getHeader("User-Agent");
            if (agent != null && !agent.isBlank()) requete.getHeaders().set(EN_TETE_AGENT_CLIENT, agent);
        }
        return execution.execute(requete, corps);
    };
}

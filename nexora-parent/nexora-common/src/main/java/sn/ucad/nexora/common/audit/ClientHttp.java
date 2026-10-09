package sn.ucad.nexora.common.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Le navigateur derrière un appel : le web relaie son adresse ({@value #EN_TETE_IP_CLIENT}) et son
 * User-Agent ({@value #EN_TETE_AGENT_CLIENT}) ; sans eux, les services ne verraient que le serveur web.
 * Valeurs indicatives (un appel direct à la Gateway peut les fixer lui-même) : pour le journal et
 * l'historique des connexions, jamais pour une décision de sécurité.
 */
public final class ClientHttp {

    public static final String EN_TETE_IP_CLIENT = "X-Nexora-Client-IP";
    public static final String EN_TETE_AGENT_CLIENT = "X-Nexora-Client-Agent";

    private ClientHttp() {}

    public static HttpServletRequest requeteCourante() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributs
                ? attributs.getRequest() : null;
    }

    public static String adresseIp(HttpServletRequest requete) {
        if (requete == null) return null;
        String ip = requete.getHeader(EN_TETE_IP_CLIENT);
        if (ip == null || ip.isBlank()) {
            String relais = requete.getHeader("X-Forwarded-For");
            ip = relais == null || relais.isBlank() ? requete.getRemoteAddr() : relais.split(",")[0];
        }
        return tronquer(ip == null ? null : ip.trim(), 50);
    }

    public static String agent(HttpServletRequest requete) {
        if (requete == null) return null;
        String agent = requete.getHeader(EN_TETE_AGENT_CLIENT);
        if (agent == null || agent.isBlank()) agent = requete.getHeader("User-Agent");
        return tronquer(agent, 500);
    }

    /** « Chrome · Windows », « Safari · iPhone »… ; « Appareil inconnu » sinon. */
    public static String appareil(String agent) {
        if (agent == null || agent.isBlank()) return "Appareil inconnu";
        String a = agent.toLowerCase();
        String navigateur = a.contains("edg/") ? "Edge" : a.contains("opr/") || a.contains("opera") ? "Opera"
                : a.contains("samsungbrowser") ? "Samsung Internet" : a.contains("firefox") ? "Firefox"
                : a.contains("chrome") ? "Chrome" : a.contains("safari") ? "Safari"
                : a.contains("java") || a.contains("python") || a.contains("curl") ? "Programme" : "Navigateur";
        String systeme = a.contains("iphone") ? "iPhone" : a.contains("ipad") ? "iPad" : a.contains("android") ? "Android"
                : a.contains("windows") ? "Windows" : a.contains("mac os") ? "Mac" : a.contains("linux") ? "Linux" : null;
        return systeme == null ? navigateur : navigateur + " · " + systeme;
    }

    static String tronquer(String valeur, int max) {
        return valeur == null || valeur.length() <= max ? valeur : valeur.substring(0, max);
    }
}

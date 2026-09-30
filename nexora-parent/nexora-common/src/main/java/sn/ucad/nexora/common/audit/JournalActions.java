package sn.ucad.nexora.common.audit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Journal des actions du back-office (table {@code journal_action}, docs/architecture-acteurs.md §9.3).
 *
 * Appelé par chaque service dans la transaction de l'action qu'il journalise
 * ({@link Propagation#MANDATORY}) : si l'action échoue, rien n'est écrit, et une action réussie
 * est toujours tracée. L'auteur est résolu depuis le compte du JWT ({@code utilisateurs.account_id}).
 * L'adresse IP est celle du navigateur transmise par le web ({@value #EN_TETE_IP_CLIENT}), à défaut
 * {@code X-Forwarded-For} puis l'adresse de l'appelant direct.
 */
public class JournalActions {

    public static final String EN_TETE_IP_CLIENT = "X-Nexora-Client-IP";

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.MANDATORY)
    public void enregistrer(UUID accountId, String module, String action, String entite, Long idEntite,
                            String description) {
        HttpServletRequest requete = requeteCourante();
        em.createNativeQuery("""
                INSERT INTO journal_action (id_utilisateur, module, action, entite, id_entite, description,
                                            adresse_ip, user_agent)
                VALUES ((SELECT id_utilisateur FROM utilisateurs WHERE account_id = :compte),
                        :module, :action, :entite, :idEntite, :description, :ip, :agent)
                """)
                .setParameter("compte", accountId)
                .setParameter("module", module)
                .setParameter("action", action)
                .setParameter("entite", entite)
                .setParameter("idEntite", idEntite)
                .setParameter("description", description)
                .setParameter("ip", adresseIp(requete))
                .setParameter("agent", requete == null ? null : tronquer(requete.getHeader("User-Agent"), 500))
                .executeUpdate();
    }

    private static HttpServletRequest requeteCourante() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributs
                ? attributs.getRequest() : null;
    }

    private static String adresseIp(HttpServletRequest requete) {
        if (requete == null) return null;
        String ip = requete.getHeader(EN_TETE_IP_CLIENT);
        if (ip == null || ip.isBlank()) {
            String relais = requete.getHeader("X-Forwarded-For");
            ip = relais == null || relais.isBlank() ? requete.getRemoteAddr() : relais.split(",")[0];
        }
        return tronquer(ip.trim(), 50);
    }

    private static String tronquer(String valeur, int max) {
        return valeur == null || valeur.length() <= max ? valeur : valeur.substring(0, max);
    }
}

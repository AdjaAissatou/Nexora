package sn.ucad.nexora.common.audit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    public static final String EN_TETE_IP_CLIENT = ClientHttp.EN_TETE_IP_CLIENT;

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
                .setParameter("ip", ClientHttp.adresseIp(requete))
                .setParameter("agent", ClientHttp.agent(requete))
                .executeUpdate();
    }

    private static HttpServletRequest requeteCourante() {
        return ClientHttp.requeteCourante();
    }
}

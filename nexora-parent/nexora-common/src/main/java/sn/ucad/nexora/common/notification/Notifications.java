package sn.ucad.nexora.common.notification;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Notifications envoyées à un utilisateur (table {@code notification}). Écrites dans la transaction
 * de l'action qui les motive ({@link Propagation#MANDATORY}) : pas de notification pour une action
 * annulée, et pas d'action réussie sans sa notification.
 */
public class Notifications {

    /** Valeurs de l'énumération SQL {@code type_notification} utilisées par les services. */
    public enum Type { INFO, SUCCES, AVERTISSEMENT, ERREUR, CERTIFICATION }

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.MANDATORY)
    public void envoyer(Long utilisateurId, Type type, String titre, String message, String urlAction) {
        em.createNativeQuery("""
                INSERT INTO notification (id_utilisateur, titre, message, type, url_action)
                VALUES (:id, :titre, :message, CAST(:type AS type_notification), :url)
                """)
                .setParameter("id", utilisateurId)
                .setParameter("titre", titre)
                .setParameter("message", message)
                .setParameter("type", type.name())
                .setParameter("url", urlAction)
                .executeUpdate();
    }
}

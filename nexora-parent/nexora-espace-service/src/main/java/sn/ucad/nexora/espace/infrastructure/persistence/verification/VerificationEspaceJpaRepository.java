package sn.ucad.nexora.espace.infrastructure.persistence.verification;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VerificationEspaceJpaRepository extends JpaRepository<VerificationEspaceJpaEntity, Long> {

    /** Demande la plus récente d'un espace : c'est elle qui détermine l'état affiché. */
    Optional<VerificationEspaceJpaEntity> findFirstByEspaceIdOrderByIdDesc(Long espaceId);

    List<VerificationEspaceJpaEntity> findByEspaceIdOrderByIdDesc(Long espaceId);

    /** File « À traiter » d'un agent : les plus anciennes d'abord, jamais ses propres espaces. */
    @Query(value = """
            SELECT v.* FROM verification_espace v
            JOIN espace_professionnel e ON e.id_espace = v.id_espace
            WHERE v.statut = 'EN_ATTENTE' AND e.id_utilisateur <> :agentId
            ORDER BY v.date_soumission ASC
            """, nativeQuery = true)
    List<VerificationEspaceJpaEntity> fileEnAttente(@Param("agentId") Long agentId);

    @Query(value = """
            SELECT * FROM verification_espace
            WHERE id_agent = :agentId AND CAST(statut AS TEXT) = :statut
            ORDER BY COALESCE(date_decision, date_prise_en_charge, date_soumission) DESC
            """, nativeQuery = true)
    List<VerificationEspaceJpaEntity> parAgentEtStatut(@Param("agentId") Long agentId, @Param("statut") String statut);

    @Query(value = """
            SELECT * FROM verification_espace
            WHERE (CAST(:statut AS TEXT) IS NULL OR CAST(statut AS TEXT) = CAST(:statut AS TEXT))
              AND statut <> 'BROUILLON'
            ORDER BY COALESCE(date_soumission, date_creation) DESC
            LIMIT 500
            """, nativeQuery = true)
    List<VerificationEspaceJpaEntity> toutes(@Param("statut") String statut);

    @Query(value = "SELECT CAST(statut AS TEXT), COUNT(*) FROM verification_espace GROUP BY statut", nativeQuery = true)
    List<Object[]> compterParStatut();

    /**
     * Délai moyen entre l'envoi et la décision de l'agent (approbation ou refus), lu dans l'historique :
     * une révocation ultérieure ne fausse pas la mesure.
     */
    @Query(value = """
            SELECT CAST(AVG(EXTRACT(EPOCH FROM (d.date_decision - s.date_soumission)) / 3600.0) AS DOUBLE PRECISION)
            FROM (SELECT id_verification, MIN(date_evenement) AS date_soumission
                  FROM verification_evenement WHERE type = 'SOUMISSION' GROUP BY id_verification) s
            JOIN (SELECT id_verification, MIN(date_evenement) AS date_decision
                  FROM verification_evenement WHERE type IN ('APPROBATION', 'REFUS') GROUP BY id_verification) d
              ON d.id_verification = s.id_verification
            """, nativeQuery = true)
    Double delaiMoyenDecisionHeures();
}

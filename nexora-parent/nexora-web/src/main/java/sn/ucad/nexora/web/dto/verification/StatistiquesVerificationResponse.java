package sn.ucad.nexora.web.dto.verification;

import java.util.Map;

/** Nombre de demandes par statut et délai moyen entre envoi et décision (heures). */
public record StatistiquesVerificationResponse(Map<String, Long> parStatut, Double delaiMoyenDecisionHeures) {}

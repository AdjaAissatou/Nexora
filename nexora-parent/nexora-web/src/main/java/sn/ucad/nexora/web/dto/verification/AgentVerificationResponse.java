package sn.ucad.nexora.web.dto.verification;

/** Agent de vérification et nombre de demandes qu'il suit encore. */
public record AgentVerificationResponse(Long utilisateurId, String nomComplet, String email, long demandesOuvertes) {}

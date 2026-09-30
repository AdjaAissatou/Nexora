package sn.ucad.nexora.web.dto.verification;

import java.time.LocalDateTime;

/** Entrée de l'historique d'une demande. */
public record EvenementVerificationResponse(String type, String ancienStatut, String nouveauStatut, String roleActeur,
                                            String acteurNom, String commentaire, LocalDateTime date) {}

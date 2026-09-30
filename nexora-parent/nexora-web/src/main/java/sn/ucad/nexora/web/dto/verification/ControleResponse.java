package sn.ucad.nexora.web.dto.verification;

import java.time.LocalDateTime;

/** Point de contrôle de l'agent ; {@code resultat} : NON_FAIT, CONFORME ou NON_CONFORME. */
public record ControleResponse(String code, String libelle, String resultat, String commentaire,
                               String agentNom, LocalDateTime dateControle) {}

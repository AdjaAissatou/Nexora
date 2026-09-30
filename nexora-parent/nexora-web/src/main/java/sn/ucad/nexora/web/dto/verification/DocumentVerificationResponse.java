package sn.ucad.nexora.web.dto.verification;

import java.time.LocalDateTime;

/** Justificatif déposé ; {@code statut} : DEPOSE, ACCEPTE, REJETE ou REMPLACE. */
public record DocumentVerificationResponse(Long id, Long typeJustificatifId, String typeCode, String typeLibelle,
                                           String nomOriginal, String typeMime, Long taille, String empreinteSha256,
                                           String statut, String motif, LocalDateTime dateDepot,
                                           LocalDateTime dateExamen) {}

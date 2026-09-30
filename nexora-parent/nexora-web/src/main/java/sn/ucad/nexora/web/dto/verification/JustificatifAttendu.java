package sn.ucad.nexora.web.dto.verification;

/** Justificatif demandé pour le type de l'espace ; même {@code groupeAlternatif} = un seul suffit. */
public record JustificatifAttendu(Long typeJustificatifId, String code, String libelle, String description,
                                  boolean obligatoire, String groupeAlternatif, boolean fourni) {}

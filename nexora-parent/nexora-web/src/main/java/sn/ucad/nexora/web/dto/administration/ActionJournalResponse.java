package sn.ucad.nexora.web.dto.administration;

import java.time.LocalDateTime;

/** Une action du journal du back-office. */
public record ActionJournalResponse(Long id, LocalDateTime date, String auteurNom, String auteurEmail,
                                    String module, String action, String entite, Long idEntite,
                                    String description, String adresseIp) {}

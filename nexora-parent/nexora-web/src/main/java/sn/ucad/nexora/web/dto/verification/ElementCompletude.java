package sn.ucad.nexora.web.dto.verification;

/** Complétude automatique d'un dossier (≠ contrôle de l'agent). */
public record ElementCompletude(String code, String libelle, boolean obligatoire, boolean complet, String detail) {}

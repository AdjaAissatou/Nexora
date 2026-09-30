package sn.ucad.nexora.web.dto.verification;

/** Justificatif téléchargé depuis espace-service, prêt à être renvoyé au navigateur. */
public record FichierJustificatif(String nom, String typeMime, byte[] contenu) {}

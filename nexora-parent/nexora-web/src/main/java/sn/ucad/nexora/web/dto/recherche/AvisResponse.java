package sn.ucad.nexora.web.dto.recherche;

import java.time.LocalDateTime;

/** Miroir de {@code sn.ucad.nexora.recherche.application.dto.response.AvisResponse}. */
public record AvisResponse(
        Long id,
        Long utilisateurId,
        Long offreId,
        Long espaceId,
        int note,
        String commentaire,
        String reponseFournisseur,
        LocalDateTime dateCreation,
        LocalDateTime dateReponse,
        /** Prénom et initiale du nom (« Aïssatou D. »). */
        String auteur,
        /** Dernière modification par son auteur ; null s'il n'a jamais été modifié. */
        LocalDateTime dateModification) implements java.io.Serializable {}

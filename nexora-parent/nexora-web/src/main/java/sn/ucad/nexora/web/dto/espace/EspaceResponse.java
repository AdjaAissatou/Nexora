package sn.ucad.nexora.web.dto.espace;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Miroir de {@code sn.ucad.nexora.espace.application.dto.response.EspaceResponse}. */
public record EspaceResponse(
        Long id,
        Long utilisateurId,
        Long typeEspaceId,
        String nom,
        String slogan,
        String description,
        String telephone,
        String email,
        boolean ouvert,
        String statut,
        boolean certifie,
        boolean verifie,
        BigDecimal noteMoyenne,
        Integer nombreAvis,
        Long nombreVues,
        Long nombreFavoris,
        LocalDateTime dateCreation) {}

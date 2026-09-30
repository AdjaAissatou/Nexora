package sn.ucad.nexora.web.dto.verification;

import java.math.BigDecimal;

/** Informations de l'espace et de son responsable, telles que l'agent les examine. */
public record EspaceExamine(
        Long id, String nom, String typeEspace, String description, String telephone,
        String telephoneSecondaire, String email, String siteWeb, String numeroNinea,
        String numeroRccm, String registreCommerce, String region, String departement,
        String commune, String quartier, String adresseComplete, BigDecimal latitude,
        BigDecimal longitude, String responsableNom, String responsableEmail,
        String responsableTelephone) {}

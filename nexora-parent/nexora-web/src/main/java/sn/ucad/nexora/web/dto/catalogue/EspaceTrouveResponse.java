package sn.ucad.nexora.web.dto.catalogue;

import java.math.BigDecimal;

/** Miroir de {@code sn.ucad.nexora.catalogue.application.dto.response.EspaceTrouveResponse}. */
public record EspaceTrouveResponse(Long id, String nom, String slogan, String logo, String typeEspace, String commune,
                                   String quartier, boolean verifie, boolean certifie, BigDecimal note, Integer nombreAvis,
                                   long nombreOffres, Boolean ouvertMaintenant) {

    public String initiale() {
        return nom == null || nom.isBlank() ? "?" : nom.substring(0, 1).toUpperCase();
    }

    /** « 4,3 » ; vide sans avis. */
    public String noteTexte() {
        if (note == null || nombreAvis == null || nombreAvis == 0) return "";
        return "⭐ " + note.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString().replace('.', ',') + " · ";
    }

    public String lieu() {
        return quartier != null && !quartier.isBlank() ? quartier : commune;
    }
}

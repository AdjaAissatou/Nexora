package com.nexora.dto;

import com.nexora.domain.catalog.Image;
import com.nexora.domain.catalog.Offre;

/** Convertit une entite {@link Offre} en {@link OffreDTO}. */
public final class OffreMapper {

    private OffreMapper() {
    }

    public static OffreDTO toDto(Offre o) {
        OffreDTO dto = new OffreDTO();
        dto.setId(o.getIdOffre());
        dto.setTitre(o.getTitre());
        dto.setDescription(o.getDescription());
        dto.setPrix(o.getPrix());
        dto.setDisponible(o.isDisponible());
        dto.setStatut(o.getStatut() != null ? o.getStatut().name() : null);
        if (o.getCategorie() != null) {
            dto.setCategorieNom(o.getCategorie().getNom());
        }
        if (o.getEspace() != null) {
            dto.setEspaceNom(o.getEspace().getNomCommercial());
            dto.setEspaceVerifie(o.getEspace().isVerifie());
            dto.setNoteEspace(o.getEspace().getNoteMoyenne());
        }
        o.getImages().stream()
                .filter(Image::isPrincipale)
                .findFirst()
                .ifPresent(img -> dto.setImagePrincipale(img.getUrl()));
        return dto;
    }
}

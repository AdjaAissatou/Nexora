package com.nexora.dto;

import com.nexora.domain.catalog.Image;
import com.nexora.domain.catalog.Offre;
import com.nexora.domain.space.Adresse;
import com.nexora.domain.space.EspaceProfessionnel;

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
        EspaceProfessionnel e = o.getEspace();
        if (e != null) {
            dto.setEspaceNom(e.getNomCommercial());
            dto.setEspaceVerifie(e.isVerifie());
            dto.setEspaceCertifie(Boolean.TRUE.equals(e.getCertifie()));
            dto.setNoteEspace(e.getNoteMoyenne());
            dto.setNombreAvis(e.getNombreAvis());
            dto.setTelephone(e.getTelephonePrincipal());
            // Adresse principale (premiere renseignee) -> libelle + coordonnees.
            e.getAdresses().stream().findFirst().ifPresent((Adresse a) -> {
                dto.setLatitude(a.getLatitude());
                dto.setLongitude(a.getLongitude());
                String quartier = a.getQuartier() != null ? a.getQuartier() : "";
                String ville = a.getVille() != null ? a.getVille() : "";
                dto.setAdresseCourte((quartier + (quartier.isEmpty() || ville.isEmpty() ? "" : ", ") + ville).trim());
            });
        }
        o.getImages().stream()
                .filter(Image::isPrincipale)
                .findFirst()
                .ifPresent(img -> dto.setImagePrincipale(img.getUrl()));
        return dto;
    }
}

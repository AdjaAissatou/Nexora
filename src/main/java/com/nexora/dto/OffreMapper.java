package com.nexora.dto;

import com.nexora.domain.catalog.Image;
import com.nexora.domain.catalog.Offre;
import com.nexora.domain.space.Adresse;
import com.nexora.domain.space.EspaceProfessionnel;

import java.util.Comparator;
import java.util.List;

/** Convertit une entite {@link Offre} en {@link OffreDTO}. */
public final class OffreMapper {

    private OffreMapper() {
    }

    public static OffreDTO toDto(Offre o) {
        OffreDTO dto = new OffreDTO();
        dto.setId(o.getIdOffre());
        dto.setType(o instanceof com.nexora.domain.catalog.ServicePro ? "SERVICE"
                : o instanceof com.nexora.domain.catalog.Produit ? "PRODUIT" : "OFFRE");
        dto.setTitre(o.getTitre());
        dto.setDescription(o.getDescription());
        dto.setPrix(o.getPrix());
        dto.setDisponible(o.isDisponible());
        dto.setStatut(o.getStatut() != null ? o.getStatut().name() : null);
        dto.setMotifRejet(o.getMotifRejet());
        if (o.getCategorie() != null) {
            dto.setCategorieNom(o.getCategorie().getNom());
            dto.setIdCategorie(o.getCategorie().getIdCategorie());
        }
        EspaceProfessionnel e = o.getEspace();
        if (e != null) {
            dto.setEspaceNom(e.getNomCommercial());
            dto.setEspaceId(e.getIdEspace());
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
        // Galerie complete (ordonnee) + image principale avec repli sur la 1re.
        List<String> galerie = o.getImages().stream()
                .sorted(Comparator.comparingInt(Image::getOrdre))
                .map(Image::getUrl)
                .toList();
        dto.setGalerie(galerie);
        o.getImages().stream()
                .filter(Image::isPrincipale)
                .findFirst()
                .map(Image::getUrl)
                .or(() -> galerie.stream().findFirst())
                .ifPresent(dto::setImagePrincipale);
        return dto;
    }
}

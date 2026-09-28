package sn.ucad.nexora.espace.presentation.mapper;

import org.springframework.stereotype.Component;
import sn.ucad.nexora.espace.application.dto.response.TypeEspaceResponse;
import sn.ucad.nexora.espace.domain.entity.TypeEspace;

@Component
public class TypeEspacePresentationMapper {

    public TypeEspaceResponse toResponse(TypeEspace e) {
        TypeEspaceResponse r = new TypeEspaceResponse();
        r.setId(e.getId());
        r.setNom(e.getNom());
        r.setDescription(e.getDescription());
        r.setIcone(e.getIcone());
        r.setCouleur(e.getCouleur());
        r.setOrdreAffichage(e.getOrdreAffichage());
        return r;
    }
}

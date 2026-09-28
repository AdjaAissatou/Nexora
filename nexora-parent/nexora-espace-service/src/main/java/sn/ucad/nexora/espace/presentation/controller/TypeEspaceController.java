package sn.ucad.nexora.espace.presentation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.ucad.nexora.espace.application.dto.response.TypeEspaceResponse;
import sn.ucad.nexora.espace.application.usecase.ListTypesEspaceUseCase;
import sn.ucad.nexora.espace.presentation.mapper.TypeEspacePresentationMapper;

@RestController
@RequestMapping("/api/v1/types-espaces")
public class TypeEspaceController {

    private final ListTypesEspaceUseCase listTypesEspace;
    private final TypeEspacePresentationMapper mapper;

    public TypeEspaceController(ListTypesEspaceUseCase listTypesEspace, TypeEspacePresentationMapper mapper) {
        this.listTypesEspace = listTypesEspace;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<TypeEspaceResponse>> lister() {
        return ResponseEntity.ok(listTypesEspace.lister().stream().map(mapper::toResponse).toList());
    }
}

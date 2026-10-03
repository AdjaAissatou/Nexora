package sn.ucad.nexora.catalogue.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sn.ucad.nexora.catalogue.application.service.vision.RechercheVisuelleService;
import sn.ucad.nexora.catalogue.application.service.vision.RechercheVisuelleService.Reponse;
import sn.ucad.nexora.catalogue.application.service.vision.RechercheVisuelleService.Resultat;

/** Recherche par photo et articles similaires (§11). Publics, comme la recherche par mots. */
@RestController
@RequestMapping("/api/v1/offres")
@Tag(name = "Recherche par photo", description = "Trouver des articles qui ressemblent à une photo")
public class RechercheVisuelleController {

    private final RechercheVisuelleService service;

    public RechercheVisuelleController(RechercheVisuelleService service) {
        this.service = service;
    }

    @PostMapping(value = "/recherche-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Rechercher par photo", description = "Offres visibles dont la photo ressemble à celle envoyée (champ « photo »)")
    public Reponse rechercher(@RequestPart("photo") MultipartFile photo,
                              @RequestParam(required = false) Integer limite) throws IOException {
        return service.rechercher(photo.getBytes(), limite);
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Vous pourriez aussi aimer", description = "Offres proches de celles-ci (offre consultée ou historique), sans le même article vendu ailleurs")
    public List<Resultat> suggestions(@RequestParam List<Long> offres, @RequestParam(required = false) Integer limite) {
        return service.suggestions(offres, limite);
    }

    @GetMapping("/{id:\\d+}/similaires")
    @Operation(summary = "Articles similaires", description = "Offres d'autres espaces dont la photo ressemble à celle de cette offre")
    public List<Resultat> similaires(@PathVariable Long id, @RequestParam(required = false) Integer limite) {
        return service.similaires(id, limite);
    }
}

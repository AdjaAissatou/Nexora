package sn.ucad.nexora.catalogue.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.catalogue.application.dto.request.OffreSearchRequest;
import sn.ucad.nexora.catalogue.application.dto.response.OffreDetailResponse;
import sn.ucad.nexora.catalogue.application.dto.response.OffrePageResponse;
import sn.ucad.nexora.catalogue.application.usecase.GetOffreUseCase;
import sn.ucad.nexora.catalogue.application.usecase.RechercherOffresUseCase;

import java.math.BigDecimal;

/**
 * Controller REST du catalogue NEXORA.
 *
 * GET /api/v1/offres/recherche  — Recherche multicritère paginée
 * GET /api/v1/offres/{id}       — Fiche détaillée d'une offre avec localisation
 */
@RestController
@RequestMapping("/api/v1/offres")
@Tag(name = "Catalogue", description = "Recherche et consultation des offres NEXORA")
public class OffreController {

    private final RechercherOffresUseCase rechercherOffres;
    private final GetOffreUseCase getOffre;

    public OffreController(RechercherOffresUseCase rechercherOffres,
                           GetOffreUseCase getOffre) {
        this.rechercherOffres = rechercherOffres;
        this.getOffre = getOffre;
    }

    /**
     * Recherche multicritère d'offres (produits et services).
     *
     * Exemple : GET /api/v1/offres/recherche?q=téléphone&commune=Dakar&prixMax=50000&tri=PRIX_ASC&page=0&taille=20
     */
    @GetMapping("/recherche")
    @Operation(summary = "Rechercher des offres",
               description = "Recherche plein texte avec filtres : catégorie, localisation, prix, type, promotions...")
    public ResponseEntity<OffrePageResponse> rechercher(

            @Parameter(description = "Texte libre (titre, description, catégorie, commune)")
            @RequestParam(required = false) String q,

            @Parameter(description = "Filtrer par catégorie")
            @RequestParam(required = false) String categorie,

            @Parameter(description = "Filtrer par type d'espace (ex: Boutique, Restaurant)")
            @RequestParam(required = false) String typeEspace,

            @Parameter(description = "Filtrer par commune")
            @RequestParam(required = false) String commune,

            @Parameter(description = "Filtrer par région")
            @RequestParam(required = false) String region,

            @Parameter(description = "Prix minimum")
            @RequestParam(required = false) BigDecimal prixMin,

            @Parameter(description = "Prix maximum")
            @RequestParam(required = false) BigDecimal prixMax,

            @Parameter(description = "true = produits, false = services, absent = tout")
            @RequestParam(required = false) Boolean estProduit,

            @Parameter(description = "Uniquement les offres avec promotion active")
            @RequestParam(required = false) Boolean avecPromotion,

            @Parameter(description = "Uniquement les espaces vérifiés")
            @RequestParam(required = false) Boolean espaceVerifie,

            @Parameter(description = "Tri : PERTINENCE | PRIX_ASC | PRIX_DESC | DATE_DESC | NOTE")
            @RequestParam(defaultValue = "PERTINENCE") String tri,

            @Parameter(description = "Numéro de page (0-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Taille de la page (max 100)")
            @RequestParam(defaultValue = "20") int taille) {

        OffreSearchRequest request = new OffreSearchRequest();
        request.setQ(q);
        request.setCategorie(categorie);
        request.setTypeEspace(typeEspace);
        request.setCommune(commune);
        request.setRegion(region);
        request.setPrixMin(prixMin);
        request.setPrixMax(prixMax);
        request.setEstProduit(estProduit);
        request.setAvecPromotion(avecPromotion);
        request.setEspaceVerifie(espaceVerifie);
        request.setTri(tri);
        request.setPage(page);
        request.setTaille(Math.min(taille, 100));

        return ResponseEntity.ok(rechercherOffres.rechercher(request));
    }

    /**
     * Fiche complète d'une offre avec toutes ses informations :
     * détails produit/service, localisation GPS, espace professionnel, images, tags.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Consulter une offre",
               description = "Retourne la fiche complète d'une offre avec localisation, espace, images et attributs spécifiques")
    public ResponseEntity<OffreDetailResponse> get(
            @Parameter(description = "Identifiant de l'offre")
            @PathVariable Long id) {

        return ResponseEntity.ok(getOffre.get(id));
    }
}

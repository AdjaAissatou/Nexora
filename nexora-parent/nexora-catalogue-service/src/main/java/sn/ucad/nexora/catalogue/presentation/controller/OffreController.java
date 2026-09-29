package sn.ucad.nexora.catalogue.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.catalogue.application.dto.request.CreateOffreRequest;
import sn.ucad.nexora.catalogue.application.dto.request.OffreSearchRequest;
import sn.ucad.nexora.catalogue.application.dto.request.UpdateOffreRequest;
import sn.ucad.nexora.catalogue.application.dto.response.CreateOffreResponse;
import sn.ucad.nexora.catalogue.application.dto.response.OffreDetailResponse;
import sn.ucad.nexora.catalogue.application.dto.response.OffreEditionResponse;
import sn.ucad.nexora.catalogue.application.dto.response.OffrePageResponse;
import sn.ucad.nexora.catalogue.application.usecase.CreateOffreUseCase;
import sn.ucad.nexora.catalogue.application.usecase.GetOffreEditionUseCase;
import sn.ucad.nexora.catalogue.application.usecase.GetOffreUseCase;
import sn.ucad.nexora.catalogue.application.usecase.RechercherOffresUseCase;
import sn.ucad.nexora.catalogue.application.usecase.UpdateOffreUseCase;

import java.math.BigDecimal;
import java.util.UUID;

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
    private final CreateOffreUseCase createOffre;
    private final UpdateOffreUseCase updateOffre;
    private final GetOffreEditionUseCase getOffreEdition;

    public OffreController(RechercherOffresUseCase rechercherOffres,
                           GetOffreUseCase getOffre,
                           CreateOffreUseCase createOffre,
                           UpdateOffreUseCase updateOffre,
                           GetOffreEditionUseCase getOffreEdition) {
        this.rechercherOffres = rechercherOffres;
        this.getOffre = getOffre;
        this.createOffre = createOffre;
        this.updateOffre = updateOffre;
        this.getOffreEdition = getOffreEdition;
    }

    /**
     * Publie une nouvelle offre (produit ou service) pour l'espace professionnel du compte connecté.
     */
    @PostMapping
    @Operation(summary = "Créer une offre",
               description = "Publie un produit ou un service pour l'espace du compte connecté")
    public ResponseEntity<CreateOffreResponse> creer(
            @AuthenticationPrincipal UUID accountId,
            @RequestBody CreateOffreRequest request) {

        return ResponseEntity.status(201).body(createOffre.creer(accountId, request));
    }

    /**
     * Charge une offre pour édition (chaîne de catégories + attributs déjà saisis) —
     * réservé au propriétaire de l'espace, contrairement à GET /{id} qui est public.
     */
    @GetMapping("/{id}/edition")
    @Operation(summary = "Charger une offre pour édition")
    public ResponseEntity<OffreEditionResponse> obtenirEdition(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable Long id) {
        return ResponseEntity.ok(getOffreEdition.obtenir(accountId, id));
    }

    /**
     * Modifie une offre existante pour l'espace professionnel du compte connecté.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Modifier une offre")
    public ResponseEntity<CreateOffreResponse> modifier(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable Long id,
            @RequestBody UpdateOffreRequest request) {
        return ResponseEntity.ok(updateOffre.modifier(accountId, id, request));
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

            @Parameter(description = "Filtrer par catégorie (texte libre, correspondance partielle)")
            @RequestParam(required = false) String categorie,

            @Parameter(description = "Filtrer par catégorie et ses sous-catégories (id de la hiérarchie)")
            @RequestParam(required = false) Long idCategorie,

            @Parameter(description = "Filtrer par type d'espace (ex: Boutique, Restaurant)")
            @RequestParam(required = false) String typeEspace,

            @Parameter(description = "Filtrer par commune")
            @RequestParam(required = false) String commune,

            @Parameter(description = "Filtrer par région")
            @RequestParam(required = false) String region,

            @Parameter(description = "Filtrer par espace professionnel (fiche espace)")
            @RequestParam(required = false) Long idEspace,

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
        request.setIdCategorie(idCategorie);
        request.setTypeEspace(typeEspace);
        request.setCommune(commune);
        request.setRegion(region);
        request.setIdEspace(idEspace);
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

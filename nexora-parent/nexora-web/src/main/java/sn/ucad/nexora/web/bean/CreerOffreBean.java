package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.event.FileUploadEvent;
import sn.ucad.nexora.web.client.CatalogueApiClient;
import sn.ucad.nexora.web.client.EspaceApiClient;
import sn.ucad.nexora.web.config.ImageUploadService;
import sn.ucad.nexora.web.dto.catalogue.AttributResponse;
import sn.ucad.nexora.web.dto.catalogue.AttributValeurRequest;
import sn.ucad.nexora.web.dto.catalogue.ValeurAttributResponse;
import sn.ucad.nexora.web.dto.catalogue.CategorieResponse;
import sn.ucad.nexora.web.dto.catalogue.CreateOffreRequest;
import sn.ucad.nexora.web.dto.catalogue.OffreEditionResponse;
import sn.ucad.nexora.web.dto.catalogue.TypeOffreResponse;
import sn.ucad.nexora.web.dto.catalogue.UpdateOffreRequest;
import sn.ucad.nexora.web.dto.espace.EspaceResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/**
 * Backing bean de {@code creer-offre.xhtml} : sélection en cascade catégorie → sous-catégorie
 * → type d'offre, puis attributs dynamiques (listes déroulantes propres à la catégorie) — pas de
 * saisie libre au-delà des champs génériques (titre, description, prix).
 */
@Named
@ViewScoped
public class CreerOffreBean implements Serializable {

    @Inject
    private transient CatalogueApiClient catalogueApiClient;

    @Inject
    private transient EspaceApiClient espaceApiClient;

    @Inject
    private SessionBean session;

    private EspaceResponse espace;
    private Long idOffreEdition;
    private String erreur;

    private List<CategorieResponse> niveau1;
    private List<CategorieResponse> niveau2;
    private List<CategorieResponse> niveau3;
    private List<TypeOffreResponse> typesOffre;
    private List<AttributResponse> attributs;

    private Long niveau1Id;
    private Long niveau2Id;
    private Long niveau3Id;
    private Long typeOffreId;

    private final Map<Long, String> valeursListe = new HashMap<>();
    private final Map<Long, String> valeursTexte = new HashMap<>();
    private final Map<Long, BigDecimal> valeursNombre = new HashMap<>();
    /** Choix multiples (tailles, couleurs…), indexés par id de valeur proposée. */
    private final Map<Long, Boolean> coches = new HashMap<>();
    /** Parmi les valeurs proposées, celles momentanément épuisées (affichées barrées sur la fiche). */
    private final Map<Long, Boolean> epuises = new HashMap<>();

    // Champs communs
    private String titre;
    private String titreAutoSuggere;
    private String description;
    private BigDecimal prix;
    private boolean negociable;

    // Produit
    private String marque;
    private String modele;
    private String reference;
    private Integer quantiteStock;
    private String garantie;
    private boolean neuf = true;

    // Service
    private Integer dureeEstimee;
    private boolean interventionDomicile;
    private boolean reservation = true;

    private String photosTexte;

    @PostConstruct
    public void charger() {
        try {
            List<EspaceResponse> mesEspaces = espaceApiClient.mesEspaces(session.getAccessToken());
            // "Ajouter une offre" depuis la fiche d'un espace précis transmet idEspace ; sans quoi
            // (ou si l'id ne correspond à aucun espace possédé), on retombe sur le premier.
            Long idEspaceParam = idParametre("idEspace");
            espace = mesEspaces.stream()
                    .filter(e -> e.id().equals(idEspaceParam))
                    .findFirst()
                    .orElse(mesEspaces.isEmpty() ? null : mesEspaces.get(0));
        } catch (ApiException e) {
            erreur = e.getMessage();
            return;
        }
        try {
            niveau1 = catalogueApiClient.categoriesRacines(espace == null ? null : espace.typeEspaceId());
        } catch (ApiException e) {
            erreur = e.getMessage();
            return;
        }

        Long idOffreParam = idParametre("offreId");
        if (idOffreParam != null) {
            chargerPourEdition(idOffreParam);
        }
    }

    private Long idParametre(String nom) {
        String brut = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(nom);
        try {
            return brut == null ? null : Long.valueOf(brut);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void chargerPourEdition(Long idOffre) {
        try {
            OffreEditionResponse r = catalogueApiClient.obtenirEdition(session.getAccessToken(), idOffre);
            idOffreEdition = r.id();

            niveau1Id = r.niveau1Id();
            if (niveau1Id != null) {
                CategorieResponse c = trouver(niveau1, niveau1Id);
                if (c != null && c.aDesEnfants()) niveau2 = catalogueApiClient.sousCategories(niveau1Id);
            }
            niveau2Id = r.niveau2Id();
            if (niveau2Id != null) {
                CategorieResponse c = trouver(niveau2, niveau2Id);
                if (c != null && c.aDesEnfants()) niveau3 = catalogueApiClient.sousCategories(niveau2Id);
            }
            niveau3Id = r.niveau3Id();

            chargerFeuille(r.idCategorie());
            typeOffreId = r.idTypeOffre();

            titre = r.titre();
            titreAutoSuggere = null;
            description = r.description();
            prix = r.prix();
            negociable = r.negociable();
            marque = r.marque();
            modele = r.modele();
            reference = r.reference();
            quantiteStock = r.quantiteStock();
            garantie = r.garantie();
            neuf = r.neuf() == null || r.neuf();
            dureeEstimee = r.dureeEstimee();
            interventionDomicile = r.interventionDomicile() != null && r.interventionDomicile();
            reservation = r.reservation() == null || r.reservation();

            if (r.attributs() != null) {
                for (AttributValeurRequest a : r.attributs()) {
                    if (a.idValeur() != null) {
                        valeursListe.put(a.idAttribut(), String.valueOf(a.idValeur()));
                        coches.put(a.idValeur(), true);
                        if (a.epuise()) epuises.put(a.idValeur(), true);
                    }
                    else if (a.valeurNombre() != null) valeursNombre.put(a.idAttribut(), a.valeurNombre());
                    else if (a.valeurTexte() != null) valeursTexte.put(a.idAttribut(), a.valeurTexte());
                }
            }
            photosTexte = r.images() == null ? null : String.join("\n", r.images());
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    public void onNiveau1Change() {
        niveau2 = null;
        niveau3 = null;
        niveau2Id = null;
        niveau3Id = null;
        typesOffre = null;
        attributs = null;
        typeOffreId = null;
        reinitialiserDetails();
        if (niveau1Id == null) return;

        CategorieResponse c = trouver(niveau1, niveau1Id);
        if (c != null && c.aDesEnfants()) {
            niveau2 = catalogueApiClient.sousCategories(niveau1Id);
        }
        // Une catégorie peut avoir à la fois des sous-catégories à explorer et ses
        // propres types d'offre directement sélectionnables (ex: "Téléphones et
        // tablettes" a une sous-catégorie "Accessoires" mais propose aussi des
        // téléphones directement) — on charge donc toujours les deux.
        chargerFeuille(niveau1Id);
    }

    public void onNiveau2Change() {
        niveau3 = null;
        niveau3Id = null;
        typesOffre = null;
        attributs = null;
        typeOffreId = null;
        reinitialiserDetails();
        if (niveau2Id == null) return;

        CategorieResponse c = trouver(niveau2, niveau2Id);
        if (c != null && c.aDesEnfants()) {
            niveau3 = catalogueApiClient.sousCategories(niveau2Id);
        }
        chargerFeuille(niveau2Id);
    }

    public void onNiveau3Change() {
        typesOffre = null;
        attributs = null;
        typeOffreId = null;
        reinitialiserDetails();
        if (niveau3Id == null) return;
        chargerFeuille(niveau3Id);
    }

    /** true si l'utilisateur peut décrire librement ce qu'il propose (rien dans la liste ne convenait). */
    public boolean isAutrePropose() {
        if (typeOffreId == null || typesOffre == null) return false;
        return typesOffre.stream()
                .filter(t -> t.id().equals(typeOffreId))
                .findFirst()
                .map(t -> "Autre".equals(t.libelle()))
                .orElse(false);
    }

    public void onTypeOffreChange() {
        if (typeOffreId == null || typesOffre == null) return;
        typesOffre.stream()
                .filter(t -> t.id().equals(typeOffreId))
                .findFirst()
                .ifPresent(t -> {
                    // Ne remplace le titre que s'il est vide ou qu'il vient encore de la dernière
                    // suggestion automatique — une saisie manuelle de l'utilisateur n'est jamais écrasée.
                    boolean nonModifie = titre == null || titre.isBlank() || titre.equals(titreAutoSuggere);
                    if ("Autre".equals(t.libelle())) {
                        if (nonModifie) titre = null;
                        titreAutoSuggere = null;
                        return;
                    }
                    if (nonModifie) {
                        titre = t.libelle();
                        titreAutoSuggere = t.libelle();
                    }
                });
    }

    private void reinitialiserDetails() {
        titre = null;
        titreAutoSuggere = null;
        description = null;
        prix = null;
        negociable = false;
        marque = null;
        modele = null;
        quantiteStock = null;
        garantie = null;
        neuf = true;
        dureeEstimee = null;
        interventionDomicile = false;
        reservation = true;
    }

    private void chargerFeuille(Long idCategorie) {
        typesOffre = catalogueApiClient.typesOffre(idCategorie);
        attributs = catalogueApiClient.attributs(idCategorie);
        valeursListe.clear();
        valeursTexte.clear();
        valeursNombre.clear();
        coches.clear();
        epuises.clear();
    }

    private CategorieResponse trouver(List<CategorieResponse> liste, Long id) {
        if (liste == null) return null;
        return liste.stream().filter(c -> c.id().equals(id)).findFirst().orElse(null);
    }

    /** Id de la catégorie feuille effectivement sélectionnée (le niveau le plus profond choisi). */
    public Long getCategorieFeuilleId() {
        if (niveau3Id != null) return niveau3Id;
        if (niveau2Id != null) return niveau2Id;
        return niveau1Id;
    }

    public boolean isTypeOffreService() {
        if (typeOffreId == null || typesOffre == null) return false;
        return typesOffre.stream()
                .filter(t -> t.id().equals(typeOffreId))
                .findFirst()
                .map(t -> "SERVICE".equals(t.principale()))
                .orElse(false);
    }

    public String publier() {
        try {
            List<AttributValeurRequest> valeurs = new ArrayList<>();
            if (attributs != null) {
                for (AttributResponse a : attributs) {
                    if ("Marque".equals(a.nom())) continue; // capturé par le champ dédié creerOffreBean.marque
                    switch (a.typeChamp()) {
                        case "LISTE" -> {
                            String v = valeursListe.get(a.id());
                            if (v != null && !v.isBlank()) {
                                valeurs.add(new AttributValeurRequest(a.id(), null, null, null, Long.valueOf(v), false));
                            }
                        }
                        case "MULTI_LISTE" -> {
                            for (ValeurAttributResponse v : a.valeurs()) {
                                boolean epuise = Boolean.TRUE.equals(epuises.get(v.id()));
                                // Une valeur marquée épuisée reste proposée : elle est affichée, barrée.
                                if (epuise || Boolean.TRUE.equals(coches.get(v.id()))) {
                                    valeurs.add(new AttributValeurRequest(a.id(), null, null, null, v.id(), epuise));
                                }
                            }
                        }
                        case "NOMBRE" -> {
                            BigDecimal v = valeursNombre.get(a.id());
                            if (v != null) {
                                valeurs.add(new AttributValeurRequest(a.id(), null, v, null, null, false));
                            }
                        }
                        default -> {
                            String v = valeursTexte.get(a.id());
                            if (v != null && !v.isBlank()) {
                                valeurs.add(new AttributValeurRequest(a.id(), v, null, null, null, false));
                            }
                        }
                    }
                }
            }

            List<String> images = photosTexte == null || photosTexte.isBlank()
                    ? List.of()
                    : photosTexte.lines().map(String::trim).filter(l -> !l.isBlank()).toList();

            if (idOffreEdition != null) {
                UpdateOffreRequest requete = new UpdateOffreRequest(
                        typeOffreId,
                        getCategorieFeuilleId(),
                        titre,
                        description,
                        prix,
                        negociable,
                        true,
                        marque,
                        modele,
                        reference,
                        quantiteStock,
                        garantie,
                        neuf,
                        dureeEstimee,
                        interventionDomicile,
                        reservation,
                        valeurs,
                        images);
                catalogueApiClient.modifierOffre(session.getAccessToken(), idOffreEdition, requete);
                FacesContext.getCurrentInstance()
                        .addMessage(null, sn.ucad.nexora.web.util.Messages.complet(FacesMessage.SEVERITY_INFO, "Offre mise à jour.", null));
            } else {
                CreateOffreRequest requete = new CreateOffreRequest(
                        espace.id(),
                        typeOffreId,
                        getCategorieFeuilleId(),
                        titre,
                        description,
                        prix,
                        negociable,
                        true,
                        marque,
                        modele,
                        reference,
                        quantiteStock,
                        garantie,
                        neuf,
                        dureeEstimee,
                        interventionDomicile,
                        reservation,
                        valeurs,
                        images);
                catalogueApiClient.creerOffre(session.getAccessToken(), requete);
                FacesContext.getCurrentInstance()
                        .addMessage(null, sn.ucad.nexora.web.util.Messages.complet(FacesMessage.SEVERITY_INFO, "Offre publiée.", null));
            }
            return "mon-espace?faces-redirect=true";
        } catch (ApiException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, sn.ucad.nexora.web.util.Messages.complet(FacesMessage.SEVERITY_ERROR,
                            idOffreEdition != null ? "Mise à jour impossible" : "Publication impossible", e.getMessage()));
            return null;
        }
    }

    public EspaceResponse getEspace() { return espace; }
    public String getErreur() { return erreur; }
    public boolean isModeEdition() { return idOffreEdition != null; }

    public List<CategorieResponse> getNiveau1() { return niveau1; }
    public List<CategorieResponse> getNiveau2() { return niveau2; }
    public List<CategorieResponse> getNiveau3() { return niveau3; }
    public List<TypeOffreResponse> getTypesOffre() { return typesOffre; }
    public List<AttributResponse> getAttributs() { return attributs; }

    /** Attributs à afficher dans "Caractéristiques" — Marque en est exclue, elle a son propre champ dans "Détails de l'offre". */
    public List<AttributResponse> getAutresAttributs() {
        if (attributs == null) return List.of();
        return attributs.stream().filter(a -> !"Marque".equals(a.nom())).toList();
    }

    /** Marques suggérées pour la catégorie choisie (liste vide si aucune définie : le champ Marque reste alors libre). */
    public List<String> getOptionsMarque() {
        if (attributs == null) return List.of();
        return attributs.stream()
                .filter(a -> "Marque".equals(a.nom()))
                .findFirst()
                .map(a -> a.valeurs().stream().map(v -> v.valeur()).toList())
                .orElse(List.of());
    }

    public Long getNiveau1Id() { return niveau1Id; }
    public void setNiveau1Id(Long v) { niveau1Id = v; }
    public Long getNiveau2Id() { return niveau2Id; }
    public void setNiveau2Id(Long v) { niveau2Id = v; }
    public Long getNiveau3Id() { return niveau3Id; }
    public void setNiveau3Id(Long v) { niveau3Id = v; }
    public Long getTypeOffreId() { return typeOffreId; }
    public void setTypeOffreId(Long v) { typeOffreId = v; }

    public Map<Long, String> getValeursListe() { return valeursListe; }
    public Map<Long, String> getValeursTexte() { return valeursTexte; }
    public Map<Long, BigDecimal> getValeursNombre() { return valeursNombre; }
    public Map<Long, Boolean> getCoches() { return coches; }
    public Map<Long, Boolean> getEpuises() { return epuises; }

    public String getTitre() { return titre; }
    public void setTitre(String v) { titre = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal v) { prix = v; }
    public boolean isNegociable() { return negociable; }
    public void setNegociable(boolean v) { negociable = v; }

    public String getMarque() { return marque; }
    public void setMarque(String v) { marque = v; }
    public String getModele() { return modele; }
    public void setModele(String v) { modele = v; }
    public String getReference() { return reference; }
    public void setReference(String v) { reference = v; }
    public Integer getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(Integer v) { quantiteStock = v; }
    public String getGarantie() { return garantie; }
    public void setGarantie(String v) { garantie = v; }
    public boolean isNeuf() { return neuf; }
    public void setNeuf(boolean v) { neuf = v; }

    public Integer getDureeEstimee() { return dureeEstimee; }
    public void setDureeEstimee(Integer v) { dureeEstimee = v; }
    public boolean isInterventionDomicile() { return interventionDomicile; }
    public void setInterventionDomicile(boolean v) { interventionDomicile = v; }
    public boolean isReservation() { return reservation; }
    public void setReservation(boolean v) { reservation = v; }

    public String getPhotosTexte() { return photosTexte; }
    public void setPhotosTexte(String v) { photosTexte = v; }

    public void uploaderPhoto(FileUploadEvent event) {
        try {
            String url = ImageUploadService.enregistrer(event.getFile().getInputStream(), event.getFile().getFileName());
            photosTexte = (photosTexte == null || photosTexte.isBlank()) ? url : photosTexte + "\n" + url;
        } catch (IOException | IllegalArgumentException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, sn.ucad.nexora.web.util.Messages.complet(FacesMessage.SEVERITY_ERROR, "Envoi impossible", e.getMessage()));
        }
    }
}

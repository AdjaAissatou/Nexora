package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import sn.ucad.nexora.web.client.CatalogueAdminApiClient;
import sn.ucad.nexora.web.dto.administration.CatalogueAdminDtos.*;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/**
 * Catégories proposées ({@code admin/propositions.xhtml}), §21 : les catégories écrites par les
 * professionnels qui ont choisi « Autre… », regroupées quand le texte revient au même. Pour chacune :
 * créer la catégorie (sous le rayon actuel ou l'une de ses sous-catégories) et y ranger les offres,
 * ranger les offres dans une catégorie existante, ou écarter la proposition (motif).
 */
@Named
@ViewScoped
public class PropositionsCategoriesBean implements Serializable {

    @Inject
    private transient CatalogueAdminApiClient api;

    @Inject
    private SessionBean session;

    private List<Proposition> propositions = List.of();
    private String erreur;

    private final Map<String, String> noms = new HashMap<>();
    private final Map<String, Long> parents = new HashMap<>();
    private final Map<String, String> natures = new HashMap<>();
    private final Map<String, String> recherches = new HashMap<>();
    private final Map<String, List<Resultat>> resultats = new HashMap<>();
    private final Map<String, String> motifs = new HashMap<>();
    private final Map<Long, List<Etape>> emplacements = new HashMap<>();

    @PostConstruct
    public void charger() {
        try {
            appliquer(api.propositions(session.getAccessToken()));
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    private void appliquer(List<Proposition> liste) {
        propositions = liste;
        for (Proposition p : liste) {
            noms.putIfAbsent(p.cle(), p.libelle());
            parents.putIfAbsent(p.cle(), p.categorieActuelleId());
            natures.putIfAbsent(p.cle(), p.nature());
        }
    }

    /** Où créer la catégorie : le rayon actuel des offres, ou l'une de ses sous-catégories actives. */
    public List<Etape> emplacements(Proposition p) {
        return emplacements.computeIfAbsent(p.categorieActuelleId(), id -> {
            List<Etape> liste = new ArrayList<>();
            liste.add(new Etape(id, p.categorieActuelle(), true));
            try {
                FicheCategorie f = api.fiche(session.getAccessToken(), id);
                f.sousCategories().stream().filter(Noeud::actif)
                        .forEach(n -> liste.add(new Etape(n.id(), p.categorieActuelle() + " › " + n.nom(), true)));
            } catch (ApiException e) {
                // Sans le détail du rayon, on propose au moins le rayon lui-même.
            }
            return liste;
        });
    }

    public void creer(String cle) {
        executer(() -> api.traiterProposition(session.getAccessToken(), "creer",
                        new PropositionCreation(cle, parents.get(cle), noms.get(cle), natures.get(cle))),
                "Catégorie « " + noms.get(cle) + " » créée ; les offres y sont rangées.");
    }

    public void chercher(String cle) {
        String q = recherches.get(cle);
        try {
            resultats.put(cle, api.rechercher(session.getAccessToken(), q == null ? "" : q).stream()
                    .filter(r -> "CATEGORIE".equals(r.genre()) && r.actif()).toList());
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    public void rattacher(String cle, Long categorieId, String chemin) {
        executer(() -> api.traiterProposition(session.getAccessToken(), "rattacher", new PropositionRattachement(cle, categorieId)),
                "Offres rangées dans « " + chemin + " ».");
    }

    public void ecarter(String cle) {
        executer(() -> api.traiterProposition(session.getAccessToken(), "ecarter", new PropositionEcart(cle, motifs.get(cle))),
                "Proposition écartée : les offres restent dans leur rayon.");
    }

    private void executer(Supplier<List<Proposition>> action, String succes) {
        try {
            appliquer(action.get());
            message(FacesMessage.SEVERITY_INFO, succes);
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    private static void message(FacesMessage.Severity gravite, String texte) {
        FacesContext.getCurrentInstance().addMessage(null, sn.ucad.nexora.web.util.Messages.complet(gravite, texte, null));
    }

    public List<Proposition> getPropositions() { return propositions; }
    public String getErreur() { return erreur; }
    public Map<String, String> getNoms() { return noms; }
    public Map<String, Long> getParents() { return parents; }
    public Map<String, String> getNatures() { return natures; }
    public Map<String, String> getRecherches() { return recherches; }
    public Map<String, List<Resultat>> getResultats() { return resultats; }
    public Map<String, String> getMotifs() { return motifs; }
}

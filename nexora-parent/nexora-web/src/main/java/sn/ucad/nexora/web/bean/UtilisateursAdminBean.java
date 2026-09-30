package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import sn.ucad.nexora.web.client.AdminComptesApiClient;
import sn.ucad.nexora.web.dto.administration.AdminComptesDtos.PageComptes;
import sn.ucad.nexora.web.dto.administration.AdminComptesDtos.RoleInfo;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/** Liste des comptes du back-office ({@code admin/utilisateurs.xhtml}) : filtres dans l'URL, pagination. */
@Named
@ViewScoped
public class UtilisateursAdminBean implements Serializable {

    @Inject
    private transient AdminComptesApiClient adminComptesApiClient;

    @Inject
    private SessionBean session;

    private String recherche;
    private String role;
    private String etat;
    private int page;
    private PageComptes resultat;
    private List<RoleInfo> roles = List.of();
    private String erreur;

    @PostConstruct
    public void charger() {
        Map<String, String> p = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        recherche = p.get("recherche");
        role = p.get("role");
        etat = p.get("etat");
        try {
            page = p.get("page") == null ? 0 : Math.max(0, Integer.parseInt(p.get("page")));
        } catch (NumberFormatException e) {
            page = 0;
        }
        try {
            resultat = adminComptesApiClient.rechercher(session.getAccessToken(), recherche, role, etat, page);
            roles = adminComptesApiClient.rolesEtPermissions(session.getAccessToken()).roles();
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    public String filtrer() {
        return "/admin/utilisateurs?faces-redirect=true" + param("recherche", recherche) + param("role", role)
                + param("etat", etat);
    }

    private static String param(String nom, String valeur) {
        return valeur == null || valeur.isBlank() ? "" : "&" + nom + "=" + URLEncoder.encode(valeur.trim(), StandardCharsets.UTF_8);
    }

    public PageComptes getResultat() { return resultat; }
    public List<RoleInfo> getRoles() { return roles; }
    public String getErreur() { return erreur; }
    public int getPage() { return page; }
    public String getRecherche() { return recherche; }
    public void setRecherche(String recherche) { this.recherche = recherche; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
}

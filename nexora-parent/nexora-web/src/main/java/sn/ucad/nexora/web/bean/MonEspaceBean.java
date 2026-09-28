package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import sn.ucad.nexora.web.client.EspaceApiClient;
import sn.ucad.nexora.web.client.UserApiClient;
import sn.ucad.nexora.web.dto.espace.EspaceResponse;
import sn.ucad.nexora.web.dto.user.UserResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/** Backing bean de {@code mon-espace.xhtml} — profil du compte connecté et son espace professionnel. */
@Named
@ViewScoped
public class MonEspaceBean implements Serializable {

    @Inject
    private transient UserApiClient userApiClient;

    @Inject
    private transient EspaceApiClient espaceApiClient;

    @Inject
    private SessionBean session;

    private UserResponse profil;
    private EspaceResponse espace;
    private String erreur;

    @PostConstruct
    public void charger() {
        try {
            profil = userApiClient.obtenir(session.getAccessToken(), session.getCompte().id());
        } catch (ApiException e) {
            erreur = e.getMessage();
            return;
        }
        try {
            List<EspaceResponse> mesEspaces = espaceApiClient.mesEspaces(session.getAccessToken());
            espace = mesEspaces.isEmpty() ? null : mesEspaces.get(0);
        } catch (ApiException e) {
            // Pas encore de profil utilisateur exploitable côté espace-service : pas une erreur bloquante,
            // l'utilisateur voit simplement la proposition de créer son espace.
            espace = null;
        }
    }

    /**
     * Nommé sans "is + deux majuscules" à dessein : {@code isAEspace()} exposerait la propriété EL
     * "AEspace" (majuscule conservée) et non "aEspace", par la règle de décapitalisation de
     * {@link java.beans.Introspector} — {@code #{monEspaceBean.aEspace}} échouerait silencieusement.
     */
    public boolean isPossedeEspace() {
        return espace != null;
    }

    public UserResponse getProfil() {
        return profil;
    }

    public EspaceResponse getEspace() {
        return espace;
    }

    public String getErreur() {
        return erreur;
    }
}

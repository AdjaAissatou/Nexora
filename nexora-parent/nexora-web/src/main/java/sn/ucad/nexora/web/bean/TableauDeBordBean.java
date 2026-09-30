package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sn.ucad.nexora.web.client.AdministrationApiClient;
import sn.ucad.nexora.web.dto.administration.TableauDeBordResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/** Tableau de bord du back-office ({@code admin/index.xhtml}). */
@Named
@ViewScoped
public class TableauDeBordBean implements Serializable {

    @Inject
    private transient AdministrationApiClient administrationApiClient;

    @Inject
    private SessionBean session;

    private TableauDeBordResponse tableau;
    private String erreur;

    @PostConstruct
    public void charger() {
        try {
            tableau = administrationApiClient.tableauDeBord(session.getAccessToken());
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    public TableauDeBordResponse getTableau() {
        return tableau;
    }

    public String getErreur() {
        return erreur;
    }
}

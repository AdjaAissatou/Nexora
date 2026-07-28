package com.nexora.web.bean;

import com.nexora.dto.EspaceRequest;
import com.nexora.dto.EspaceViewDTO;
import com.nexora.service.EspaceService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

/**
 * Paramètres / infos de l'espace, éditables dans le back-office du fournisseur
 * (nom, nature, description, contacts, réseaux, adresse). Persistance réelle
 * via {@link EspaceService}.
 */
@Named("parametresBean")
@ViewScoped
public class ParametresBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private EspaceService espaceService;
    @Inject private SessionBean session;

    private EspaceRequest form = new EspaceRequest();

    @PostConstruct
    public void init() {
        if (session.getEspaceId() != null) {
            this.form = espaceService.parametres(session.getEspaceId());
        }
    }

    public void enregistrer() {
        if (session.getEspaceId() == null) return;
        if (form.getNomCommercial() == null || form.getNomCommercial().isBlank()) {
            message(FacesMessage.SEVERITY_WARN, "Le nom commercial est obligatoire.");
            return;
        }
        try {
            EspaceViewDTO vue = espaceService.modifier(session.getEspaceId(), form);
            session.setEspace(vue, form.getVille());
            message(FacesMessage.SEVERITY_INFO, "Paramètres enregistrés.");
        } catch (Exception ex) {
            message(FacesMessage.SEVERITY_WARN, "Enregistrement impossible : " + ex.getMessage());
        }
    }

    private void message(FacesMessage.Severity s, String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(s, m, null));
    }

    public EspaceRequest getForm()          { return form; }
    public void setForm(EspaceRequest form)  { this.form = form; }
}

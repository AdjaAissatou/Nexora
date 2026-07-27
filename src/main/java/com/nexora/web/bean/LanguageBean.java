package com.nexora.web.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Locale;

/**
 * Langue de l'interface (FR / EN). Applique la locale a la vue avant chaque
 * rendu, de sorte que les libelles {@code #{msg.*}} soient traduits.
 */
@Named("lang")
@SessionScoped
public class LanguageBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Locale locale = Locale.FRENCH;

    public Locale getLocale() { return locale; }

    public String getCode() { return "en".equals(locale.getLanguage()) ? "EN" : "FR"; }

    /** Bascule FR <-> EN (postback complet : toute la page est retraduite). */
    public String basculer() {
        locale = "fr".equals(locale.getLanguage()) ? Locale.ENGLISH : Locale.FRENCH;
        return null;
    }

    /** Applique la locale a la vue courante (appele en preRenderView). */
    public void appliquer(ComponentSystemEvent e) {
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    }
}

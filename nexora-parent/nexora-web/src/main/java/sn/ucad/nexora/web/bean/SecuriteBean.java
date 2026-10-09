package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Map;
import sn.ucad.nexora.web.client.AuthApiClient;
import sn.ucad.nexora.web.dto.auth.SecuriteDtos.Resultat;
import sn.ucad.nexora.web.dto.auth.SecuriteDtos.Securite;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/**
 * Sécurité du compte ({@code securite.xhtml}), docs/architecture-acteurs.md §25 : mot de passe, email de
 * connexion (code envoyé à la nouvelle adresse), appareils connectés, historique et tentatives échouées.
 */
@Named
@ViewScoped
public class SecuriteBean implements Serializable {

    private static final Map<String, String> MOTIFS = Map.of(
            "DECONNEXION", "Déconnexion",
            "REVOQUEE", "Déconnecté depuis un autre appareil",
            "MOT_DE_PASSE", "Fermée : mot de passe changé",
            "EMAIL", "Fermée : email changé",
            "AUTRES_APPAREILS", "Fermée : « déconnecter les autres appareils »");

    @Inject
    private transient AuthApiClient auth;

    @Inject
    private SessionBean session;

    private Securite securite;
    private String erreur;

    private String actuel, nouveau, confirmation;
    private String nouvelEmail, motDePasseEmail, code;

    @PostConstruct
    public void charger() {
        try {
            securite = auth.securite(session.getAccessToken());
            erreur = null;
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    public void changerMotDePasse() {
        executer("motDePasseForm", () -> auth.changerMotDePasse(session.getAccessToken(), actuel, nouveau, confirmation), r -> {
            actuel = nouveau = confirmation = null;
            return r.message();
        });
    }

    public void demanderEmail() {
        executer("emailForm", () -> auth.demanderChangementEmail(session.getAccessToken(), nouvelEmail, motDePasseEmail), r -> {
            motDePasseEmail = null;
            return r.message();
        });
    }

    public void confirmerEmail() {
        executer("emailForm", () -> auth.confirmerChangementEmail(session.getAccessToken(), code), r -> {
            session.majEmail(r.message());
            code = nouvelEmail = null;
            return "C'est fait : vous vous connectez désormais avec " + r.message() + "."
                    + (r.sessionsFermees() > 0 ? " Vos autres appareils ont été déconnectés." : "");
        });
    }

    public void terminer(String sid) {
        executer("appareilsForm", () -> auth.terminerSession(session.getAccessToken(), sid), Resultat::message);
    }

    public void terminerAutres() {
        executer("appareilsForm", () -> auth.terminerAutresSessions(session.getAccessToken()), Resultat::message);
    }

    private void executer(String formulaire, java.util.function.Supplier<Resultat> action,
                          java.util.function.Function<Resultat, String> succes) {
        try {
            Resultat r = action.get();
            String texte = succes.apply(r);
            charger();
            message(formulaire, FacesMessage.SEVERITY_INFO, texte);
        } catch (ApiException e) {
            message(formulaire, FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    private static void message(String formulaire, FacesMessage.Severity gravite, String texte) {
        FacesContext.getCurrentInstance().addMessage(formulaire, new FacesMessage(gravite, texte, null));
    }

    public String motif(String code) {
        return code == null ? "" : MOTIFS.getOrDefault(code, code);
    }

    public Securite getSecurite() { return securite; }
    public String getErreur() { return erreur; }
    public String getActuel() { return actuel; }
    public void setActuel(String v) { actuel = v; }
    public String getNouveau() { return nouveau; }
    public void setNouveau(String v) { nouveau = v; }
    public String getConfirmation() { return confirmation; }
    public void setConfirmation(String v) { confirmation = v; }
    public String getNouvelEmail() { return nouvelEmail; }
    public void setNouvelEmail(String v) { nouvelEmail = v; }
    public String getMotDePasseEmail() { return motDePasseEmail; }
    public void setMotDePasseEmail(String v) { motDePasseEmail = v; }
    public String getCode() { return code; }
    public void setCode(String v) { code = v; }
}

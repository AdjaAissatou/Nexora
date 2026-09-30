package sn.ucad.nexora.web.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import sn.ucad.nexora.web.client.EspaceApiClient;
import sn.ucad.nexora.web.dto.espace.HorairesDtos.ExceptionRequest;
import sn.ucad.nexora.web.dto.espace.HorairesDtos.HorairesResponse;
import sn.ucad.nexora.web.dto.espace.HorairesDtos.JourResponse;
import sn.ucad.nexora.web.dto.espace.HorairesDtos.PlageDto;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/**
 * Onglet « Horaires » de Mon espace (docs/architecture-acteurs.md §10) : la semaine type et les
 * jours exceptionnels. Les heures se saisissent au format HH:mm (champs {@code type="time"}).
 */
@Named
@ViewScoped
public class HorairesEspaceBean implements Serializable {

    /** Une ligne éditable de la semaine. */
    public static class Ligne implements Serializable {
        private final String jour;
        private final String libelle;
        private boolean ouvert;
        private boolean ouvert24h;
        private String ouverture;
        private String fermeture;
        private String pauseDebut;
        private String pauseFin;

        Ligne(JourResponse j) {
            jour = j.jour();
            libelle = j.libelleJour();
            ouvert = j.ouvert();
            ouvert24h = j.ouvert24h();
            ouverture = texte(j.ouverture());
            fermeture = texte(j.fermeture());
            pauseDebut = texte(j.pauseDebut());
            pauseFin = texte(j.pauseFin());
        }

        void copier(Ligne modele) {
            ouvert = modele.ouvert;
            ouvert24h = modele.ouvert24h;
            ouverture = modele.ouverture;
            fermeture = modele.fermeture;
            pauseDebut = modele.pauseDebut;
            pauseFin = modele.pauseFin;
        }

        PlageDto versPlage() {
            return new PlageDto(jour, ouvert, ouvert24h, heure(ouverture), heure(fermeture), heure(pauseDebut), heure(pauseFin));
        }

        public String getJour() { return jour; }
        public String getLibelle() { return libelle; }
        public boolean isOuvert() { return ouvert; }
        public void setOuvert(boolean ouvert) { this.ouvert = ouvert; }
        public boolean isOuvert24h() { return ouvert24h; }
        public void setOuvert24h(boolean ouvert24h) { this.ouvert24h = ouvert24h; }
        public String getOuverture() { return ouverture; }
        public void setOuverture(String v) { this.ouverture = v; }
        public String getFermeture() { return fermeture; }
        public void setFermeture(String v) { this.fermeture = v; }
        public String getPauseDebut() { return pauseDebut; }
        public void setPauseDebut(String v) { this.pauseDebut = v; }
        public String getPauseFin() { return pauseFin; }
        public void setPauseFin(String v) { this.pauseFin = v; }
    }

    @Inject
    private transient EspaceApiClient espaceApiClient;

    @Inject
    private SessionBean session;

    private Long espaceId;
    private HorairesResponse horaires;
    private List<Ligne> lignes = new ArrayList<>();

    // Nouvelle exception
    private String dateException;
    private boolean fermeException = true;
    private String ouvertureException;
    private String fermetureException;
    private String motifException;

    public void initialiser(Long espaceId) {
        this.espaceId = espaceId;
        try {
            appliquer(espaceApiClient.horaires(session.getAccessToken(), espaceId));
        } catch (ApiException e) {
            horaires = null;
        }
    }

    private void appliquer(HorairesResponse r) {
        horaires = r;
        lignes = new ArrayList<>(r.semaine().stream().map(Ligne::new).toList());
        // Semaine jamais renseignée : on propose 8 h – 18 h du lundi au samedi, dimanche fermé, à ajuster.
        if (!r.renseignes()) {
            for (Ligne l : lignes) {
                l.ouvert = !"DIMANCHE".equals(l.jour);
                l.ouverture = l.ouvert ? "08:00" : null;
                l.fermeture = l.ouvert ? "18:00" : null;
            }
        }
    }

    public void enregistrer() {
        executer(() -> appliquer(espaceApiClient.enregistrerHoraires(session.getAccessToken(), espaceId,
                lignes.stream().map(Ligne::versPlage).toList())), "Horaires enregistrés.");
    }

    /** Recopie les horaires du lundi sur les jours du mardi au samedi (sans enregistrer). */
    public void copierLundi() {
        if (lignes.isEmpty()) return;
        Ligne lundi = lignes.get(0);
        for (int i = 1; i < 6 && i < lignes.size(); i++) lignes.get(i).copier(lundi);
        message(FacesMessage.SEVERITY_INFO, "Horaires du lundi recopiés du mardi au samedi : vérifiez puis enregistrez.");
    }

    public void effacer() {
        executer(() -> appliquer(espaceApiClient.enregistrerHoraires(session.getAccessToken(), espaceId, List.of())),
                "Horaires effacés : votre fiche n'indique plus si vous êtes ouvert.");
    }

    public void ajouterException() {
        LocalDate date;
        try {
            date = dateException == null || dateException.isBlank() ? null : LocalDate.parse(dateException);
        } catch (java.time.format.DateTimeParseException e) {
            date = null;
        }
        LocalDate d = date;
        executer(() -> {
            appliquer(espaceApiClient.ajouterExceptionHoraire(session.getAccessToken(), espaceId, new ExceptionRequest(d,
                    fermeException, fermeException ? null : heure(ouvertureException), fermeException ? null : heure(fermetureException),
                    motifException)));
            dateException = null;
            fermeException = true;
            ouvertureException = null;
            fermetureException = null;
            motifException = null;
        }, "Jour exceptionnel enregistré.");
    }

    public void supprimerException(Long id) {
        executer(() -> appliquer(espaceApiClient.supprimerExceptionHoraire(session.getAccessToken(), espaceId, id)),
                "Jour exceptionnel supprimé.");
    }

    private void executer(Runnable action, String succes) {
        try {
            action.run();
            message(FacesMessage.SEVERITY_INFO, succes);
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    private static void message(FacesMessage.Severity gravite, String texte) {
        FacesContext.getCurrentInstance().addMessage("horairesForm", new FacesMessage(gravite, texte, null));
    }

    static String texte(LocalTime t) {
        return t == null ? null : t.toString().substring(0, 5);
    }

    static LocalTime heure(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalTime.parse(s.trim().length() == 5 ? s.trim() : s.trim().substring(0, 5));
        } catch (RuntimeException e) {
            return null;
        }
    }

    public HorairesResponse getHoraires() { return horaires; }
    public List<Ligne> getLignes() { return lignes; }
    public String getAujourdhui() { return LocalDate.now(java.time.ZoneId.of("Africa/Dakar")).toString(); }
    public String getDateException() { return dateException; }
    public void setDateException(String dateException) { this.dateException = dateException; }
    public boolean isFermeException() { return fermeException; }
    public void setFermeException(boolean fermeException) { this.fermeException = fermeException; }
    public String getOuvertureException() { return ouvertureException; }
    public void setOuvertureException(String v) { this.ouvertureException = v; }
    public String getFermetureException() { return fermetureException; }
    public void setFermetureException(String v) { this.fermetureException = v; }
    public String getMotifException() { return motifException; }
    public void setMotifException(String v) { this.motifException = v; }
}

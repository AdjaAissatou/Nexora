package sn.ucad.nexora.espace.domain.verification;

/** Un document REMPLACE est conservé (fichier compris) pour que l'historique reste exact. */
public enum StatutDocument {
    DEPOSE("déposé"), ACCEPTE("accepté"), REJETE("rejeté"), REMPLACE("remplacé");

    private final String libelle;

    StatutDocument(String libelle) {
        this.libelle = libelle;
    }

    /** Document encore pris en compte dans le dossier. */
    public boolean isActif() {
        return this != REMPLACE;
    }

    /** Libellé écrit dans l'historique. */
    public String getLibelle() {
        return libelle;
    }
}

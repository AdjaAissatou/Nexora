package sn.ucad.nexora.espace.domain.verification;

/** Un document REMPLACE est conservé (fichier compris) pour que l'historique reste exact. */
public enum StatutDocument {
    DEPOSE, ACCEPTE, REJETE, REMPLACE;

    /** Document encore pris en compte dans le dossier. */
    public boolean isActif() {
        return this != REMPLACE;
    }
}

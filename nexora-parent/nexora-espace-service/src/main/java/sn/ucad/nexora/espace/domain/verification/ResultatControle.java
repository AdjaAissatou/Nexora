package sn.ucad.nexora.espace.domain.verification;

public enum ResultatControle {
    NON_FAIT("à contrôler"), CONFORME("conforme"), NON_CONFORME("non conforme");

    private final String libelle;

    ResultatControle(String libelle) {
        this.libelle = libelle;
    }

    /** Libellé écrit dans l'historique. */
    public String getLibelle() {
        return libelle;
    }
}

package sn.ucad.nexora.espace.domain.verification;

/** Points que l'agent contrôle ; tous doivent être CONFORME pour approuver. */
public enum CodeControle {
    IDENTITE("Identité du responsable"),
    ACTIVITE("Existence de l'activité"),
    ADRESSE("Adresse"),
    COORDONNEES("Coordonnées"),
    DOCUMENTS("Documents fournis");

    private final String libelle;

    CodeControle(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}

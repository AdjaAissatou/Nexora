package sn.ucad.nexora.espace.domain.verification;

/** Cycle de vie d'une demande de vérification (docs/architecture-acteurs.md §8.2). */
public enum StatutVerification {
    BROUILLON, EN_ATTENTE, EN_COURS, A_COMPLETER, APPROUVEE, REFUSEE, ANNULEE, REVOQUEE;

    /** Une demande ouverte bloque la création d'une autre demande pour le même espace. */
    public boolean isOuverte() {
        return this == BROUILLON || this == EN_ATTENTE || this == EN_COURS || this == A_COMPLETER;
    }
}

package sn.ucad.nexora.espace.domain.verification;

/** Types d'entrées de l'historique d'une demande ({@code verification_evenement}). */
public enum TypeEvenement {
    CREATION, DOCUMENT_DEPOSE, DOCUMENT_RETIRE, SOUMISSION, RESOUMISSION, PRISE_EN_CHARGE,
    DOCUMENT_EXAMINE, CONTROLE, INFOS_DEMANDEES, APPROBATION, REFUS, ANNULATION,
    REATTRIBUTION, REVOCATION, MODIFICATION_ESPACE, REPRISE
}

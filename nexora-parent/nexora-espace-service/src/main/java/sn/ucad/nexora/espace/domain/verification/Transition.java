package sn.ucad.nexora.espace.domain.verification;

/** Changement d'état produit par une action ; chaque transition devient un événement de l'historique. */
public record Transition(TypeEvenement type, StatutVerification ancien, StatutVerification nouveau) {
}

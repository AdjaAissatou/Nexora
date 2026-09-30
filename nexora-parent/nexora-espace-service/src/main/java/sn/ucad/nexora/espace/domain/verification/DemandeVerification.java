package sn.ucad.nexora.espace.domain.verification;

import java.time.LocalDateTime;
import java.util.List;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.UnauthorizedException;

/**
 * Demande de vérification d'un espace professionnel : la machine à états du §8.2 de
 * docs/architecture-acteurs.md. Toute action hors du schéma est refusée ; chaque action
 * acceptée renvoie la {@link Transition} que le service enregistre dans l'historique.
 *
 * <pre>
 * BROUILLON → EN_ATTENTE → EN_COURS → APPROUVEE → REVOQUEE
 *                 ↑            ├──→ REFUSEE
 *                 │            └──→ A_COMPLETER → (resoumission) EN_COURS, même agent
 * BROUILLON / EN_ATTENTE → ANNULEE (retrait du pro) ; toute demande ouverte → ANNULEE (admin)
 * </pre>
 *
 * Aucune dépendance technique : les règles sont testées seules (DemandeVerificationTest).
 */
public class DemandeVerification {

    private Long id;
    private Long espaceId;
    private Long demandeurId;
    private Long agentId;
    private StatutVerification statut;
    private String motif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateSoumission;
    private LocalDateTime datePriseEnCharge;
    private LocalDateTime dateDecision;

    /** Nouvelle demande en préparation, créée au premier justificatif déposé. */
    public static DemandeVerification nouvelle(Long espaceId, Long demandeurId) {
        DemandeVerification d = new DemandeVerification();
        d.espaceId = espaceId;
        d.demandeurId = demandeurId;
        d.statut = StatutVerification.BROUILLON;
        d.dateCreation = LocalDateTime.now();
        return d;
    }

    public boolean peutRecevoirDocuments() {
        return statut == StatutVerification.BROUILLON || statut == StatutVerification.A_COMPLETER;
    }

    // ---------------------------------------------------------------- professionnel

    /**
     * Envoi du dossier. Depuis A_COMPLETER, la demande revient directement à l'agent qui la
     * suivait (EN_COURS) ; s'il a été retiré entre-temps, elle repart dans la file d'attente.
     */
    public Transition soumettre(List<String> elementsManquants) {
        exiger(peutRecevoirDocuments(), "Cette demande ne peut pas être envoyée dans son état actuel");
        if (elementsManquants != null && !elementsManquants.isEmpty()) {
            throw new BusinessException("Dossier incomplet : " + String.join(", ", elementsManquants));
        }
        StatutVerification ancien = statut;
        if (ancien == StatutVerification.BROUILLON) {
            statut = StatutVerification.EN_ATTENTE;
            dateSoumission = LocalDateTime.now();
            return new Transition(TypeEvenement.SOUMISSION, ancien, statut);
        }
        statut = agentId != null ? StatutVerification.EN_COURS : StatutVerification.EN_ATTENTE;
        motif = null;
        return new Transition(TypeEvenement.RESOUMISSION, ancien, statut);
    }

    /** Le professionnel retire sa demande tant qu'aucun agent ne l'a prise en charge. */
    public Transition retirer() {
        exiger(statut == StatutVerification.BROUILLON || statut == StatutVerification.EN_ATTENTE,
                "La demande est déjà en cours d'examen : elle ne peut plus être retirée");
        return changer(TypeEvenement.ANNULATION, StatutVerification.ANNULEE, "Demande retirée par le professionnel", true);
    }

    // ---------------------------------------------------------------- agent

    /**
     * @param agentEstProprietaire vrai si l'agent possède l'espace : conflit d'intérêts, toujours refusé.
     */
    public Transition prendreEnCharge(Long agent, boolean agentEstProprietaire) {
        exigerPasProprietaire(agentEstProprietaire);
        exiger(statut == StatutVerification.EN_ATTENTE, "Seule une demande en attente peut être prise en charge");
        StatutVerification ancien = statut;
        agentId = agent;
        statut = StatutVerification.EN_COURS;
        datePriseEnCharge = LocalDateTime.now();
        return new Transition(TypeEvenement.PRISE_EN_CHARGE, ancien, statut);
    }

    /** Vérifie que l'agent est bien celui qui suit la demande, en cours d'examen. */
    public void exigerExamenPar(Long agent) {
        exiger(statut == StatutVerification.EN_COURS, "La demande n'est pas en cours d'examen");
        if (agentId == null || !agentId.equals(agent)) {
            throw new UnauthorizedException("Cette demande est suivie par un autre agent");
        }
    }

    public Transition demanderInformations(Long agent, String motifDemande) {
        exigerExamenPar(agent);
        exigerMotif(motifDemande, "Précisez les informations à fournir");
        return changer(TypeEvenement.INFOS_DEMANDEES, StatutVerification.A_COMPLETER, motifDemande, false);
    }

    /**
     * @param pointsBloquants ce qui empêche l'approbation (contrôles non conformes, documents non
     *                        acceptés…), calculé par le service ; doit être vide.
     */
    public Transition approuver(Long agent, List<String> pointsBloquants) {
        exigerExamenPar(agent);
        if (pointsBloquants != null && !pointsBloquants.isEmpty()) {
            throw new BusinessException("Approbation impossible : " + String.join(", ", pointsBloquants));
        }
        return changer(TypeEvenement.APPROBATION, StatutVerification.APPROUVEE, null, true);
    }

    public Transition refuser(Long agent, String motifRefus) {
        exigerExamenPar(agent);
        exigerMotif(motifRefus, "Le motif du refus est obligatoire");
        return changer(TypeEvenement.REFUS, StatutVerification.REFUSEE, motifRefus, true);
    }

    // ---------------------------------------------------------------- administrateur / système

    public Transition annuler(String motifAnnulation) {
        exiger(statut.isOuverte(), "Seule une demande ouverte peut être annulée");
        exigerMotif(motifAnnulation, "Le motif de l'annulation est obligatoire");
        return changer(TypeEvenement.ANNULATION, StatutVerification.ANNULEE, motifAnnulation, true);
    }

    /** Retire une vérification accordée : fraude constatée (admin) ou information vérifiée modifiée (système). */
    public Transition revoquer(String motifRevocation) {
        exiger(statut == StatutVerification.APPROUVEE, "Seule une vérification approuvée peut être révoquée");
        exigerMotif(motifRevocation, "Le motif de la révocation est obligatoire");
        return changer(TypeEvenement.REVOCATION, StatutVerification.REVOQUEE, motifRevocation, true);
    }

    /**
     * Confie la demande à un autre agent, ou la remet dans la file d'attente ({@code nouvelAgent} nul).
     */
    public Transition reattribuer(Long nouvelAgent, boolean agentEstProprietaire) {
        exiger(statut == StatutVerification.EN_ATTENTE || statut == StatutVerification.EN_COURS
                        || statut == StatutVerification.A_COMPLETER,
                "Seule une demande envoyée et non décidée peut être réattribuée");
        StatutVerification ancien = statut;
        if (nouvelAgent == null) {
            agentId = null;
            if (statut == StatutVerification.EN_COURS) statut = StatutVerification.EN_ATTENTE;
        } else {
            exigerPasProprietaire(agentEstProprietaire);
            agentId = nouvelAgent;
            if (statut == StatutVerification.EN_ATTENTE) {
                statut = StatutVerification.EN_COURS;
                datePriseEnCharge = LocalDateTime.now();
            }
        }
        return new Transition(TypeEvenement.REATTRIBUTION, ancien, statut);
    }

    // ---------------------------------------------------------------- outils

    private Transition changer(TypeEvenement type, StatutVerification nouveau, String nouveauMotif, boolean decision) {
        StatutVerification ancien = statut;
        statut = nouveau;
        motif = nouveauMotif;
        if (decision) dateDecision = LocalDateTime.now();
        return new Transition(type, ancien, nouveau);
    }

    private static void exiger(boolean condition, String message) {
        if (!condition) throw new BusinessException(message);
    }

    private static void exigerMotif(String valeur, String message) {
        if (valeur == null || valeur.isBlank()) throw new BusinessException(message);
    }

    private static void exigerPasProprietaire(boolean agentEstProprietaire) {
        if (agentEstProprietaire) {
            throw new UnauthorizedException("Un agent ne peut pas traiter la vérification de son propre espace");
        }
    }

    // ---------------------------------------------------------------- accès (reconstitution depuis la base)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEspaceId() { return espaceId; }
    public void setEspaceId(Long espaceId) { this.espaceId = espaceId; }
    public Long getDemandeurId() { return demandeurId; }
    public void setDemandeurId(Long demandeurId) { this.demandeurId = demandeurId; }
    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }
    public StatutVerification getStatut() { return statut; }
    public void setStatut(StatutVerification statut) { this.statut = statut; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public LocalDateTime getDateSoumission() { return dateSoumission; }
    public void setDateSoumission(LocalDateTime dateSoumission) { this.dateSoumission = dateSoumission; }
    public LocalDateTime getDatePriseEnCharge() { return datePriseEnCharge; }
    public void setDatePriseEnCharge(LocalDateTime datePriseEnCharge) { this.datePriseEnCharge = datePriseEnCharge; }
    public LocalDateTime getDateDecision() { return dateDecision; }
    public void setDateDecision(LocalDateTime dateDecision) { this.dateDecision = dateDecision; }
}

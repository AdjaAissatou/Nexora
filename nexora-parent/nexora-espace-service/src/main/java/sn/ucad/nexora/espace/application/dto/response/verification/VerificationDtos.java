package sn.ucad.nexora.espace.application.dto.response.verification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Réponses de l'API de vérification (docs/architecture-acteurs.md §8.6). Regroupées ici : ce sont
 * de simples records de transport, lus par le web.
 */
public final class VerificationDtos {

    private VerificationDtos() {}

    /** Ce que voit le professionnel : état déduit de la dernière demande, complétude, demande courante. */
    public record EtatVerificationResponse(
            Long espaceId,
            String etat,
            boolean verifie,
            LocalDateTime dateVerification,
            boolean peutDeposerDocuments,
            boolean peutSoumettre,
            boolean peutRetirer,
            List<ElementCompletude> completude,
            List<JustificatifAttendu> justificatifsAttendus,
            VerificationDetailResponse demande,
            List<VerificationResumeResponse> demandesPrecedentes) {}

    /** Complétude automatique (≠ contrôle de l'agent). */
    public record ElementCompletude(String code, String libelle, boolean obligatoire, boolean complet, String detail) {}

    public record JustificatifAttendu(Long typeJustificatifId, String code, String libelle, String description,
                                      boolean obligatoire, String groupeAlternatif, boolean fourni) {}

    public record VerificationResumeResponse(
            Long id, Long espaceId, String espaceNom, String typeEspace, String commune,
            String demandeurNom, String statut, String motif, String agentNom, boolean urgent,
            LocalDateTime dateCreation, LocalDateTime dateSoumission,
            LocalDateTime datePriseEnCharge, LocalDateTime dateDecision) {}

    public record VerificationDetailResponse(
            VerificationResumeResponse resume,
            EspaceExamine espace,
            List<ElementCompletude> completude,
            List<DocumentResponse> documents,
            List<ControleResponse> controles,
            List<EvenementResponse> historique,
            List<String> pointsBloquants) {}

    /** Informations de l'espace et du responsable présentées à l'agent. */
    public record EspaceExamine(
            Long id, String nom, String typeEspace, String description, String telephone,
            String telephoneSecondaire, String email, String siteWeb, String numeroNinea,
            String numeroRccm, String registreCommerce, String region, String departement,
            String commune, String quartier, String adresseComplete, BigDecimal latitude,
            BigDecimal longitude, String responsableNom, String responsableEmail,
            String responsableTelephone) {}

    public record DocumentResponse(Long id, Long typeJustificatifId, String typeCode, String typeLibelle,
                                   String nomOriginal, String typeMime, Long taille, String empreinteSha256,
                                   String statut, String motif, LocalDateTime dateDepot, LocalDateTime dateExamen) {}

    public record ControleResponse(String code, String libelle, String resultat, String commentaire,
                                   String agentNom, LocalDateTime dateControle) {}

    public record EvenementResponse(String type, String ancienStatut, String nouveauStatut, String roleActeur,
                                    String acteurNom, String commentaire, LocalDateTime date) {}

    public record StatistiquesVerificationResponse(java.util.Map<String, Long> parStatut,
                                                   Double delaiMoyenDecisionHeures) {}
}

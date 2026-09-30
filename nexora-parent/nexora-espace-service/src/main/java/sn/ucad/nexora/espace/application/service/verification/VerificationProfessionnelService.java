package sn.ucad.nexora.espace.application.service.verification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;
import sn.ucad.nexora.common.exception.UnauthorizedException;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.ElementCompletude;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.EtatVerificationResponse;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.VerificationDetailResponse;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.VerificationResumeResponse;
import sn.ucad.nexora.espace.domain.verification.DemandeVerification;
import sn.ucad.nexora.espace.domain.verification.RoleActeur;
import sn.ucad.nexora.espace.domain.verification.StatutDocument;
import sn.ucad.nexora.espace.domain.verification.StatutVerification;
import sn.ucad.nexora.espace.domain.verification.Transition;
import sn.ucad.nexora.espace.domain.verification.TypeEvenement;
import sn.ucad.nexora.espace.infrastructure.persistence.UtilisateurLookupRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.entity.EspaceJpaEntity;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.TypeJustificatifJpaEntity;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.VerificationDocumentJpaEntity;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.VerificationEspaceJpaEntity;
import sn.ucad.nexora.espace.infrastructure.storage.StockageJustificatifs;
import sn.ucad.nexora.espace.infrastructure.storage.StockageJustificatifs.FichierStocke;

/**
 * Côté professionnel : suivre l'état de vérification de son espace, déposer les justificatifs,
 * envoyer ou retirer la demande. Toujours limité aux espaces du compte connecté.
 */
@Service
public class VerificationProfessionnelService {

    private static final Set<StatutDocument> COMPTENT_POUR_ENVOI = Set.of(StatutDocument.DEPOSE, StatutDocument.ACCEPTE);

    private final VerificationSupport support;
    private final UtilisateurLookupRepository utilisateurs;
    private final StockageJustificatifs stockage;

    public VerificationProfessionnelService(VerificationSupport support, UtilisateurLookupRepository utilisateurs,
                                            StockageJustificatifs stockage) {
        this.support = support;
        this.utilisateurs = utilisateurs;
        this.stockage = stockage;
    }

    @Transactional(readOnly = true)
    public EtatVerificationResponse etat(UUID accountId, Long espaceId) {
        Long utilisateurId = utilisateurId(accountId);
        EspaceJpaEntity espace = espaceDuProprietaire(espaceId, utilisateurId);

        List<VerificationEspaceJpaEntity> historique = support.demandes.findByEspaceIdOrderByIdDesc(espaceId);
        VerificationEspaceJpaEntity derniere = historique.isEmpty() ? null : historique.get(0);
        StatutVerification statut = derniere != null ? StatutVerification.valueOf(derniere.getStatut()) : null;

        // La complétude porte sur la demande en préparation / à compléter ; sinon sur un dossier vide.
        boolean dossierModifiable = statut == StatutVerification.BROUILLON || statut == StatutVerification.A_COMPLETER;
        List<VerificationDocumentJpaEntity> docs = dossierModifiable ? support.documentsActifs(derniere.getId()) : List.of();
        List<ElementCompletude> completude = support.completude(espace, docs, COMPTENT_POUR_ENVOI);

        boolean peutDeposer = statut == null || dossierModifiable || statut == StatutVerification.REFUSEE
                || statut == StatutVerification.ANNULEE || statut == StatutVerification.REVOQUEE;
        boolean peutSoumettre = dossierModifiable && VerificationSupport.manquants(completude).isEmpty();
        boolean peutRetirer = statut == StatutVerification.BROUILLON || statut == StatutVerification.EN_ATTENTE;

        VerificationDetailResponse demande = derniere != null ? support.detail(derniere) : null;
        List<VerificationResumeResponse> precedentes = historique.size() > 1
                ? support.resumes(historique.subList(1, historique.size())) : List.of();

        return new EtatVerificationResponse(espaceId, etatAffiche(statut), Boolean.TRUE.equals(espace.getVerifie()),
                espace.getDateVerification(), peutDeposer, peutSoumettre, peutRetirer, completude,
                support.justificatifsAttendus(espace, docs), demande, precedentes);
    }

    /** État présenté au professionnel, déduit de la dernière demande (§8.2). */
    static String etatAffiche(StatutVerification statut) {
        if (statut == null) return "NON_VERIFIE";
        return switch (statut) {
            case BROUILLON, ANNULEE -> "NON_VERIFIE";
            case EN_ATTENTE, EN_COURS -> "EN_COURS";
            case A_COMPLETER -> "A_COMPLETER";
            case APPROUVEE -> "VERIFIE";
            case REFUSEE -> "REFUSEE";
            case REVOQUEE -> "REVOQUEE";
        };
    }

    /**
     * Dépôt d'un justificatif. Crée la demande (BROUILLON) au premier dépôt ; un justificatif du même
     * type précédemment rejeté est marqué REMPLACE (conservé, jamais effacé).
     */
    @Transactional
    public EtatVerificationResponse deposerDocument(UUID accountId, Long espaceId, Long typeJustificatifId,
                                                    String nomOriginal, byte[] contenu) {
        Long utilisateurId = utilisateurId(accountId);
        EspaceJpaEntity espace = espaceDuProprietaire(espaceId, utilisateurId);
        TypeJustificatifJpaEntity type = typeJustificatifId == null ? null
                : support.typesJustificatif.findById(typeJustificatifId).orElse(null);
        if (type == null || !Boolean.TRUE.equals(type.getActif())) {
            throw new BusinessException("Type de justificatif inconnu");
        }

        VerificationEspaceJpaEntity entite = demandeModifiable(espace, utilisateurId);
        FichierStocke fichier = stockage.enregistrer(entite.getId(), contenu);

        for (VerificationDocumentJpaEntity ancien : support.documentsActifs(entite.getId())) {
            if (ancien.getTypeJustificatifId().equals(type.getId()) && StatutDocument.REJETE.name().equals(ancien.getStatut())) {
                ancien.setStatut(StatutDocument.REMPLACE.name());
                support.documents.save(ancien);
            }
        }
        VerificationDocumentJpaEntity doc = new VerificationDocumentJpaEntity();
        doc.setVerificationId(entite.getId());
        doc.setTypeJustificatifId(type.getId());
        doc.setNomOriginal(nomNettoye(nomOriginal));
        doc.setCheminStockage(fichier.chemin());
        doc.setTypeMime(fichier.typeMime());
        doc.setTaille(fichier.taille());
        doc.setEmpreinteSha256(fichier.empreinteSha256());
        doc.setStatut(StatutDocument.DEPOSE.name());
        doc.setDateDepot(LocalDateTime.now());
        support.documents.save(doc);
        support.historiser(entite.getId(), TypeEvenement.DOCUMENT_DEPOSE, null, null, utilisateurId,
                RoleActeur.PROFESSIONNEL, type.getLibelle() + " : " + doc.getNomOriginal());
        return etat(accountId, espaceId);
    }

    /** Retrait d'un justificatif pas encore examiné, tant que le dossier est modifiable (conservé en REMPLACE). */
    @Transactional
    public EtatVerificationResponse retirerDocument(UUID accountId, Long espaceId, Long documentId) {
        Long utilisateurId = utilisateurId(accountId);
        espaceDuProprietaire(espaceId, utilisateurId);
        VerificationDocumentJpaEntity doc = support.documents.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document introuvable"));
        VerificationEspaceJpaEntity entite = support.demandeEntite(doc.getVerificationId());
        if (!entite.getEspaceId().equals(espaceId)) {
            throw new ResourceNotFoundException("Document introuvable");
        }
        if (!VerificationSupport.versDomaine(entite).peutRecevoirDocuments()
                || !StatutDocument.DEPOSE.name().equals(doc.getStatut())) {
            throw new BusinessException("Ce document ne peut plus être retiré");
        }
        doc.setStatut(StatutDocument.REMPLACE.name());
        support.documents.save(doc);
        support.historiser(entite.getId(), TypeEvenement.DOCUMENT_RETIRE, null, null, utilisateurId,
                RoleActeur.PROFESSIONNEL, doc.getNomOriginal());
        return etat(accountId, espaceId);
    }

    @Transactional
    public EtatVerificationResponse soumettre(UUID accountId, Long espaceId) {
        Long utilisateurId = utilisateurId(accountId);
        EspaceJpaEntity espace = espaceDuProprietaire(espaceId, utilisateurId);
        VerificationEspaceJpaEntity entite = demandeOuverte(espaceId)
                .orElseThrow(() -> new BusinessException("Ajoutez d'abord vos justificatifs"));
        DemandeVerification demande = VerificationSupport.versDomaine(entite);
        List<String> manquants = VerificationSupport.manquants(
                support.completude(espace, support.documentsActifs(entite.getId()), COMPTENT_POUR_ENVOI));
        Transition t = demande.soumettre(manquants);
        support.enregistrer(demande, entite);
        support.historiser(entite.getId(), t, utilisateurId, RoleActeur.PROFESSIONNEL, null);
        support.appliquerEffets(demande, t);
        return etat(accountId, espaceId);
    }

    @Transactional
    public EtatVerificationResponse retirer(UUID accountId, Long espaceId) {
        Long utilisateurId = utilisateurId(accountId);
        espaceDuProprietaire(espaceId, utilisateurId);
        VerificationEspaceJpaEntity entite = demandeOuverte(espaceId)
                .orElseThrow(() -> new BusinessException("Aucune demande en cours"));
        DemandeVerification demande = VerificationSupport.versDomaine(entite);
        Transition t = demande.retirer();
        support.enregistrer(demande, entite);
        support.historiser(entite.getId(), t, utilisateurId, RoleActeur.PROFESSIONNEL, demande.getMotif());
        support.appliquerEffets(demande, t);
        return etat(accountId, espaceId);
    }

    // ------------------------------------------------------------------ outils

    private VerificationEspaceJpaEntity demandeModifiable(EspaceJpaEntity espace, Long utilisateurId) {
        var ouverte = demandeOuverte(espace.getId());
        if (ouverte.isPresent()) {
            if (!VerificationSupport.versDomaine(ouverte.get()).peutRecevoirDocuments()) {
                throw new BusinessException(
                        "Votre demande est en cours d'examen : vous pourrez ajouter des documents si l'agent le demande");
            }
            return ouverte.get();
        }
        if (Boolean.TRUE.equals(espace.getVerifie())) {
            throw new BusinessException("Cet espace est déjà vérifié");
        }
        DemandeVerification nouvelle = DemandeVerification.nouvelle(espace.getId(), utilisateurId);
        VerificationEspaceJpaEntity entite = support.enregistrer(nouvelle, null);
        support.historiser(entite.getId(), TypeEvenement.CREATION, null, StatutVerification.BROUILLON, utilisateurId,
                RoleActeur.PROFESSIONNEL, null);
        return entite;
    }

    private java.util.Optional<VerificationEspaceJpaEntity> demandeOuverte(Long espaceId) {
        return support.demandes.findFirstByEspaceIdOrderByIdDesc(espaceId)
                .filter(v -> StatutVerification.valueOf(v.getStatut()).isOuverte());
    }

    private Long utilisateurId(UUID accountId) {
        if (accountId == null) throw new UnauthorizedException("Compte authentifié obligatoire");
        return utilisateurs.findIdByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Profil utilisateur introuvable"));
    }

    private EspaceJpaEntity espaceDuProprietaire(Long espaceId, Long utilisateurId) {
        EspaceJpaEntity espace = support.espace(espaceId);
        if (!utilisateurId.equals(espace.getUtilisateurId())) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à gérer la vérification de cet espace");
        }
        return espace;
    }

    private static String nomNettoye(String nom) {
        if (nom == null || nom.isBlank()) return "document";
        String simple = nom.replace('\\', '/');
        simple = simple.substring(simple.lastIndexOf('/') + 1);
        return simple.length() > 200 ? simple.substring(simple.length() - 200) : simple;
    }
}

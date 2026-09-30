package sn.ucad.nexora.espace.application.service.verification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;
import sn.ucad.nexora.common.exception.UnauthorizedException;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.VerificationDetailResponse;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.VerificationResumeResponse;
import sn.ucad.nexora.espace.domain.verification.CodeControle;
import sn.ucad.nexora.espace.domain.verification.DemandeVerification;
import sn.ucad.nexora.espace.domain.verification.ResultatControle;
import sn.ucad.nexora.espace.domain.verification.RoleActeur;
import sn.ucad.nexora.espace.domain.verification.StatutDocument;
import sn.ucad.nexora.espace.domain.verification.StatutVerification;
import sn.ucad.nexora.espace.domain.verification.Transition;
import sn.ucad.nexora.espace.domain.verification.TypeEvenement;
import sn.ucad.nexora.espace.infrastructure.persistence.UtilisateurLookupRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.entity.EspaceJpaEntity;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.VerificationControleJpaEntity;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.VerificationDocumentJpaEntity;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.VerificationEspaceJpaEntity;

/**
 * Côté agent de vérification (rôle AGENT_VERIFICATION, contrôlé dans SecurityConfig) : files de
 * travail, examen, contrôles, décision. Un agent ne voit ni ne traite jamais ses propres espaces.
 */
@Service
public class VerificationAgentService {

    private final VerificationSupport support;
    private final UtilisateurLookupRepository utilisateurs;

    public VerificationAgentService(VerificationSupport support, UtilisateurLookupRepository utilisateurs) {
        this.support = support;
        this.utilisateurs = utilisateurs;
    }

    /**
     * EN_ATTENTE : la file commune « À traiter » (hors ses propres espaces). Autres statuts : les
     * demandes suivies ou décidées par cet agent (En cours, Informations demandées, Approuvées…).
     */
    @Transactional(readOnly = true)
    public List<VerificationResumeResponse> file(UUID accountId, String statut) {
        Long agent = agentId(accountId);
        StatutVerification s = statut == null || statut.isBlank() ? StatutVerification.EN_ATTENTE : statutValide(statut);
        List<VerificationEspaceJpaEntity> liste = s == StatutVerification.EN_ATTENTE
                ? support.demandes.fileEnAttente(agent)
                : support.demandes.parAgentEtStatut(agent, s.name());
        return support.resumes(liste);
    }

    @Transactional(readOnly = true)
    public VerificationDetailResponse detail(UUID accountId, Long verificationId) {
        Long agent = agentId(accountId);
        VerificationEspaceJpaEntity entite = support.demandeEntite(verificationId);
        exigerAcces(entite, agent);
        return support.detail(entite);
    }

    @Transactional
    public VerificationDetailResponse prendre(UUID accountId, Long verificationId) {
        Long agent = agentId(accountId);
        VerificationEspaceJpaEntity entite = support.demandeEntite(verificationId);
        DemandeVerification demande = VerificationSupport.versDomaine(entite);
        Transition t = demande.prendreEnCharge(agent, estProprietaire(entite, agent));
        return appliquer(entite, demande, t, agent, null);
    }

    @Transactional
    public VerificationDetailResponse controler(UUID accountId, Long verificationId, String code,
                                                String resultat, String commentaire) {
        Long agent = agentId(accountId);
        VerificationEspaceJpaEntity entite = support.demandeEntite(verificationId);
        VerificationSupport.versDomaine(entite).exigerExamenPar(agent);
        CodeControle codeControle = enumValide(CodeControle.class, code, "Point de contrôle inconnu");
        ResultatControle r = enumValide(ResultatControle.class, resultat, "Résultat de contrôle inconnu");
        if (r == ResultatControle.NON_CONFORME && (commentaire == null || commentaire.isBlank())) {
            throw new BusinessException("Expliquez pourquoi ce point n'est pas conforme");
        }
        VerificationControleJpaEntity c = support.controles.findByVerificationIdAndCode(verificationId, codeControle.name())
                .orElseGet(VerificationControleJpaEntity::new);
        c.setVerificationId(verificationId);
        c.setCode(codeControle.name());
        c.setResultat(r.name());
        c.setCommentaire(commentaire);
        c.setAgentId(agent);
        c.setDateControle(LocalDateTime.now());
        support.controles.save(c);
        support.historiser(verificationId, TypeEvenement.CONTROLE, null, null, agent, RoleActeur.AGENT,
                codeControle.getLibelle() + " : " + r.name() + (commentaire == null || commentaire.isBlank() ? "" : " — " + commentaire));
        return support.detail(support.demandeEntite(verificationId));
    }

    @Transactional
    public VerificationDetailResponse examinerDocument(UUID accountId, Long verificationId, Long documentId,
                                                       String decision, String motif) {
        Long agent = agentId(accountId);
        VerificationEspaceJpaEntity entite = support.demandeEntite(verificationId);
        VerificationSupport.versDomaine(entite).exigerExamenPar(agent);
        VerificationDocumentJpaEntity doc = support.documents.findById(documentId)
                .filter(d -> d.getVerificationId().equals(verificationId))
                .orElseThrow(() -> new ResourceNotFoundException("Document introuvable"));
        if (!StatutDocument.valueOf(doc.getStatut()).isActif()) {
            throw new BusinessException("Ce document a été remplacé");
        }
        StatutDocument d = enumValide(StatutDocument.class, decision, "Décision inconnue : ACCEPTE ou REJETE");
        if (d != StatutDocument.ACCEPTE && d != StatutDocument.REJETE) {
            throw new BusinessException("Décision inconnue : ACCEPTE ou REJETE");
        }
        if (d == StatutDocument.REJETE && (motif == null || motif.isBlank())) {
            throw new BusinessException("Le motif du rejet est obligatoire");
        }
        doc.setStatut(d.name());
        doc.setMotif(d == StatutDocument.REJETE ? motif : null);
        doc.setAgentId(agent);
        doc.setDateExamen(LocalDateTime.now());
        support.documents.save(doc);
        support.historiser(verificationId, TypeEvenement.DOCUMENT_EXAMINE, null, null, agent, RoleActeur.AGENT,
                doc.getNomOriginal() + " : " + d.name() + (d == StatutDocument.REJETE ? " — " + motif : ""));
        return support.detail(support.demandeEntite(verificationId));
    }

    @Transactional
    public VerificationDetailResponse demanderInformations(UUID accountId, Long verificationId, String motif) {
        Long agent = agentId(accountId);
        VerificationEspaceJpaEntity entite = support.demandeEntite(verificationId);
        DemandeVerification demande = VerificationSupport.versDomaine(entite);
        return appliquer(entite, demande, demande.demanderInformations(agent, motif), agent, motif);
    }

    @Transactional
    public VerificationDetailResponse approuver(UUID accountId, Long verificationId) {
        Long agent = agentId(accountId);
        VerificationEspaceJpaEntity entite = support.demandeEntite(verificationId);
        DemandeVerification demande = VerificationSupport.versDomaine(entite);
        EspaceJpaEntity espace = support.espace(entite.getEspaceId());
        List<String> bloquants = support.pointsBloquants(entite, espace);
        return appliquer(entite, demande, demande.approuver(agent, bloquants), agent, null);
    }

    @Transactional
    public VerificationDetailResponse refuser(UUID accountId, Long verificationId, String motif) {
        Long agent = agentId(accountId);
        VerificationEspaceJpaEntity entite = support.demandeEntite(verificationId);
        DemandeVerification demande = VerificationSupport.versDomaine(entite);
        return appliquer(entite, demande, demande.refuser(agent, motif), agent, motif);
    }

    // ------------------------------------------------------------------ outils

    private VerificationDetailResponse appliquer(VerificationEspaceJpaEntity entite, DemandeVerification demande,
                                                 Transition t, Long agent, String commentaire) {
        support.enregistrer(demande, entite);
        support.historiser(entite.getId(), t, agent, RoleActeur.AGENT, commentaire);
        support.appliquerEffets(demande, t);
        return support.detail(support.demandeEntite(entite.getId()));
    }

    /** Lecture : demande en attente (pour décider de la prendre) ou suivie par cet agent. */
    private void exigerAcces(VerificationEspaceJpaEntity entite, Long agent) {
        if (estProprietaire(entite, agent)) {
            throw new UnauthorizedException("Un agent ne peut pas traiter la vérification de son propre espace");
        }
        boolean enAttente = StatutVerification.EN_ATTENTE.name().equals(entite.getStatut());
        if (!enAttente && !agent.equals(entite.getAgentId())) {
            throw new UnauthorizedException("Cette demande est suivie par un autre agent");
        }
    }

    private boolean estProprietaire(VerificationEspaceJpaEntity entite, Long agent) {
        return agent.equals(support.espace(entite.getEspaceId()).getUtilisateurId());
    }

    private Long agentId(UUID accountId) {
        if (accountId == null) throw new UnauthorizedException("Compte authentifié obligatoire");
        return utilisateurs.findIdByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Profil utilisateur introuvable"));
    }

    static StatutVerification statutValide(String statut) {
        return enumValide(StatutVerification.class, statut, "Statut de vérification inconnu : " + statut);
    }

    static <E extends Enum<E>> E enumValide(Class<E> type, String valeur, String message) {
        try {
            return Enum.valueOf(type, valeur == null ? "" : valeur.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(message);
        }
    }
}

package sn.ucad.nexora.espace.application.service.verification;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.UnauthorizedException;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.StatistiquesVerificationResponse;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.VerificationDetailResponse;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.VerificationResumeResponse;
import sn.ucad.nexora.espace.domain.verification.DemandeVerification;
import sn.ucad.nexora.espace.domain.verification.RoleActeur;
import sn.ucad.nexora.espace.domain.verification.StatutVerification;
import sn.ucad.nexora.espace.domain.verification.Transition;
import sn.ucad.nexora.espace.infrastructure.persistence.UtilisateurLookupRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.VerificationEspaceJpaEntity;

/**
 * Supervision (rôle ADMIN / SUPER_ADMIN, contrôlé dans SecurityConfig) : toutes les demandes,
 * réattribution, annulation, révocation, statistiques. L'administrateur ne décide pas à la place
 * de l'agent (§8.1) : ni approbation ni refus ici.
 */
@Service
public class VerificationAdminService {

    private final VerificationSupport support;
    private final UtilisateurLookupRepository utilisateurs;

    public VerificationAdminService(VerificationSupport support, UtilisateurLookupRepository utilisateurs) {
        this.support = support;
        this.utilisateurs = utilisateurs;
    }

    @Transactional(readOnly = true)
    public List<VerificationResumeResponse> toutes(String statut) {
        String filtre = statut == null || statut.isBlank() ? null : VerificationAgentService.statutValide(statut).name();
        return support.resumes(support.demandes.toutes(filtre));
    }

    @Transactional(readOnly = true)
    public VerificationDetailResponse detail(Long verificationId) {
        return support.detail(support.demandeEntite(verificationId));
    }

    @Transactional
    public VerificationDetailResponse reattribuer(UUID accountId, Long verificationId, Long nouvelAgent, String motif) {
        Long admin = utilisateurId(accountId);
        VerificationEspaceJpaEntity entite = support.demandeEntite(verificationId);
        if (nouvelAgent != null && !support.lookup.aLeRole(nouvelAgent, "AGENT_VERIFICATION")) {
            throw new BusinessException("Cet utilisateur n'est pas agent de vérification");
        }
        boolean proprietaire = nouvelAgent != null
                && nouvelAgent.equals(support.espace(entite.getEspaceId()).getUtilisateurId());
        DemandeVerification demande = VerificationSupport.versDomaine(entite);
        Transition t = demande.reattribuer(nouvelAgent, proprietaire);
        String nomAgent = nouvelAgent == null ? "file d'attente"
                : support.lookup.personnes(List.of(nouvelAgent)).get(nouvelAgent).nomComplet();
        String commentaire = "Confiée à : " + nomAgent + (motif == null || motif.isBlank() ? "" : " — " + motif);
        return appliquer(entite, demande, t, admin, commentaire);
    }

    @Transactional
    public VerificationDetailResponse annuler(UUID accountId, Long verificationId, String motif) {
        Long admin = utilisateurId(accountId);
        VerificationEspaceJpaEntity entite = support.demandeEntite(verificationId);
        DemandeVerification demande = VerificationSupport.versDomaine(entite);
        return appliquer(entite, demande, demande.annuler(motif), admin, motif);
    }

    @Transactional
    public VerificationDetailResponse revoquer(UUID accountId, Long verificationId, String motif) {
        Long admin = utilisateurId(accountId);
        VerificationEspaceJpaEntity entite = support.demandeEntite(verificationId);
        DemandeVerification demande = VerificationSupport.versDomaine(entite);
        return appliquer(entite, demande, demande.revoquer(motif), admin, motif);
    }

    @Transactional(readOnly = true)
    public StatistiquesVerificationResponse statistiques() {
        Map<String, Long> parStatut = new LinkedHashMap<>();
        for (StatutVerification s : StatutVerification.values()) parStatut.put(s.name(), 0L);
        for (Object[] ligne : support.demandes.compterParStatut()) {
            parStatut.put((String) ligne[0], ((Number) ligne[1]).longValue());
        }
        return new StatistiquesVerificationResponse(parStatut, support.demandes.delaiMoyenDecisionHeures());
    }

    private VerificationDetailResponse appliquer(VerificationEspaceJpaEntity entite, DemandeVerification demande,
                                                 Transition t, Long admin, String commentaire) {
        support.enregistrer(demande, entite);
        support.historiser(entite.getId(), t, admin, RoleActeur.ADMIN, commentaire);
        support.appliquerEffets(demande, t);
        return support.detail(support.demandeEntite(entite.getId()));
    }

    private Long utilisateurId(UUID accountId) {
        if (accountId == null) throw new UnauthorizedException("Compte authentifié obligatoire");
        return utilisateurs.findIdByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Profil utilisateur introuvable"));
    }
}

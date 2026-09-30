package sn.ucad.nexora.espace.application.service.verification;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;
import sn.ucad.nexora.common.exception.UnauthorizedException;
import sn.ucad.nexora.espace.domain.verification.StatutVerification;
import sn.ucad.nexora.espace.infrastructure.persistence.UtilisateurLookupRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.VerificationDocumentJpaEntity;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.VerificationEspaceJpaEntity;
import sn.ucad.nexora.espace.infrastructure.storage.StockageJustificatifs;

/**
 * Seule porte de sortie d'un justificatif : le propriétaire de l'espace, l'agent qui suit la demande
 * (ou tout agent tant qu'elle est en attente, hors ses propres espaces) et les administrateurs.
 */
@Service
public class JustificatifAccesService {

    public record Fichier(String nom, String typeMime, long taille, InputStream contenu) {}

    private final VerificationSupport support;
    private final UtilisateurLookupRepository utilisateurs;
    private final StockageJustificatifs stockage;

    public JustificatifAccesService(VerificationSupport support, UtilisateurLookupRepository utilisateurs,
                                    StockageJustificatifs stockage) {
        this.support = support;
        this.utilisateurs = utilisateurs;
        this.stockage = stockage;
    }

    @Transactional(readOnly = true)
    public Fichier ouvrir(UUID accountId, boolean agent, boolean admin, Long verificationId, Long documentId) {
        if (accountId == null) throw new UnauthorizedException("Compte authentifié obligatoire");
        Long utilisateurId = utilisateurs.findIdByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Profil utilisateur introuvable"));
        VerificationEspaceJpaEntity demande = support.demandeEntite(verificationId);
        VerificationDocumentJpaEntity doc = support.documents.findById(documentId)
                .filter(d -> d.getVerificationId().equals(verificationId))
                .orElseThrow(() -> new ResourceNotFoundException("Document introuvable"));

        Long proprietaire = support.espace(demande.getEspaceId()).getUtilisateurId();
        boolean autorise = utilisateurId.equals(proprietaire)
                || admin
                || (agent && (utilisateurId.equals(demande.getAgentId())
                        || StatutVerification.EN_ATTENTE.name().equals(demande.getStatut())));
        if (!autorise) {
            throw new UnauthorizedException("Accès à ce justificatif refusé");
        }
        try {
            return new Fichier(doc.getNomOriginal(), doc.getTypeMime(), doc.getTaille(), stockage.lire(doc.getCheminStockage()));
        } catch (IOException e) {
            throw new ResourceNotFoundException("Fichier du justificatif introuvable");
        }
    }
}

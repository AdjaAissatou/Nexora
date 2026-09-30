package sn.ucad.nexora.espace.application.service.verification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.espace.domain.verification.DemandeVerification;
import sn.ucad.nexora.espace.domain.verification.RoleActeur;
import sn.ucad.nexora.espace.domain.verification.StatutVerification;
import sn.ucad.nexora.espace.domain.verification.Transition;
import sn.ucad.nexora.espace.domain.verification.TypeEvenement;

/**
 * Règle §8.4.4 : modifier une information vérifiée (nom, téléphone principal, adresse, NINEA,
 * RCCM, registre du commerce) retire le badge — sinon un espace pourrait être vérifié avec de
 * vraies coordonnées puis les changer. Appelé par UpdateEspaceService, dans sa transaction.
 */
@Service
public class VerificationModificationService {

    /** Valeurs, avant et après modification, des informations couvertes par la vérification. */
    public record InformationsVerifiees(String nom, String telephone, String numeroNinea, String numeroRccm,
                                        String registreCommerce, String region, String departement,
                                        String commune, String quartier, String adresseComplete) {

        /** Libellés des informations qui diffèrent (casse et espaces ignorés). */
        public List<String> differences(InformationsVerifiees apres) {
            List<String> champs = new ArrayList<>();
            if (!egal(nom, apres.nom)) champs.add("nom");
            if (!egal(telephone, apres.telephone)) champs.add("téléphone");
            if (!egal(numeroNinea, apres.numeroNinea)) champs.add("NINEA");
            if (!egal(numeroRccm, apres.numeroRccm) || !egal(registreCommerce, apres.registreCommerce)) champs.add("RCCM");
            if (!egal(region, apres.region) || !egal(departement, apres.departement) || !egal(commune, apres.commune)
                    || !egal(quartier, apres.quartier) || !egal(adresseComplete, apres.adresseComplete)) {
                champs.add("adresse");
            }
            return champs;
        }

        private static boolean egal(String a, String b) {
            return Objects.equals(normaliser(a), normaliser(b));
        }

        private static String normaliser(String s) {
            return s == null ? "" : s.trim().replaceAll("\\s+", " ").toLowerCase();
        }
    }

    private final VerificationSupport support;

    public VerificationModificationService(VerificationSupport support) {
        this.support = support;
    }

    /**
     * @return vrai si le badge a été retiré.
     */
    @Transactional
    public boolean surModification(Long espaceId, Long utilisateurId, List<String> champsModifies) {
        if (champsModifies.isEmpty()) return false;
        var derniere = support.demandes.findFirstByEspaceIdOrderByIdDesc(espaceId).orElse(null);
        if (derniere == null) return false;
        String liste = String.join(", ", champsModifies);
        StatutVerification statut = StatutVerification.valueOf(derniere.getStatut());

        if (statut == StatutVerification.APPROUVEE) {
            DemandeVerification demande = VerificationSupport.versDomaine(derniere);
            Transition t = demande.revoquer("Informations vérifiées modifiées après la vérification : " + liste);
            support.enregistrer(demande, derniere);
            support.historiser(derniere.getId(), t, null, RoleActeur.SYSTEME, demande.getMotif());
            support.appliquerEffets(demande, t);
            return true;
        }
        if (statut == StatutVerification.EN_ATTENTE || statut == StatutVerification.EN_COURS
                || statut == StatutVerification.A_COMPLETER) {
            // Demande en cours : pas de décision automatique, mais l'agent doit le voir dans l'historique.
            support.historiser(derniere.getId(), TypeEvenement.MODIFICATION_ESPACE, null, null, utilisateurId,
                    RoleActeur.PROFESSIONNEL, "Informations modifiées pendant l'examen : " + liste);
        }
        return false;
    }
}

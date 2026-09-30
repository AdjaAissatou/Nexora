package sn.ucad.nexora.espace.domain.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.UnauthorizedException;

class DemandeVerificationTest {

    private static final Long AGENT = 50L;
    private static final Long AUTRE_AGENT = 51L;

    private static DemandeVerification enCours() {
        DemandeVerification d = DemandeVerification.nouvelle(1L, 10L);
        d.soumettre(List.of());
        d.prendreEnCharge(AGENT, false);
        return d;
    }

    @Test
    void parcoursComplet_jusqua_approbation() {
        DemandeVerification d = DemandeVerification.nouvelle(1L, 10L);
        assertThat(d.getStatut()).isEqualTo(StatutVerification.BROUILLON);
        assertThat(d.peutRecevoirDocuments()).isTrue();

        Transition t = d.soumettre(List.of());
        assertThat(t).isEqualTo(new Transition(TypeEvenement.SOUMISSION, StatutVerification.BROUILLON, StatutVerification.EN_ATTENTE));
        assertThat(d.getDateSoumission()).isNotNull();
        assertThat(d.peutRecevoirDocuments()).isFalse();

        d.prendreEnCharge(AGENT, false);
        assertThat(d.getStatut()).isEqualTo(StatutVerification.EN_COURS);
        assertThat(d.getAgentId()).isEqualTo(AGENT);

        t = d.approuver(AGENT, List.of());
        assertThat(t.nouveau()).isEqualTo(StatutVerification.APPROUVEE);
        assertThat(d.getDateDecision()).isNotNull();
        assertThat(d.getStatut().isOuverte()).isFalse();
    }

    @Test
    void dossierIncomplet_ne_peut_pas_etre_envoye() {
        DemandeVerification d = DemandeVerification.nouvelle(1L, 10L);
        assertThatThrownBy(() -> d.soumettre(List.of("Pièce d'identité du responsable")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Pièce d'identité");
        assertThat(d.getStatut()).isEqualTo(StatutVerification.BROUILLON);
    }

    @Test
    void informationsDemandees_puis_resoumission_revient_au_meme_agent() {
        DemandeVerification d = enCours();
        d.demanderInformations(AGENT, "Photo de la pièce d'identité illisible");
        assertThat(d.getStatut()).isEqualTo(StatutVerification.A_COMPLETER);
        assertThat(d.getMotif()).contains("illisible");
        assertThat(d.peutRecevoirDocuments()).isTrue();

        Transition t = d.soumettre(List.of());
        assertThat(t.type()).isEqualTo(TypeEvenement.RESOUMISSION);
        assertThat(d.getStatut()).isEqualTo(StatutVerification.EN_COURS);
        assertThat(d.getAgentId()).isEqualTo(AGENT);
        assertThat(d.getMotif()).isNull();
    }

    @Test
    void resoumission_sans_agent_repart_dans_la_file() {
        DemandeVerification d = enCours();
        d.demanderInformations(AGENT, "Justificatif d'adresse manquant");
        d.reattribuer(null, false);
        d.soumettre(List.of());
        assertThat(d.getStatut()).isEqualTo(StatutVerification.EN_ATTENTE);
    }

    @Test
    void refus_et_demande_d_informations_exigent_un_motif() {
        DemandeVerification d = enCours();
        assertThatThrownBy(() -> d.refuser(AGENT, " ")).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> d.demanderInformations(AGENT, null)).isInstanceOf(BusinessException.class);
        d.refuser(AGENT, "Documents falsifiés");
        assertThat(d.getStatut()).isEqualTo(StatutVerification.REFUSEE);
        assertThat(d.getMotif()).isEqualTo("Documents falsifiés");
    }

    @Test
    void seul_l_agent_assigne_decide() {
        DemandeVerification d = enCours();
        assertThatThrownBy(() -> d.approuver(AUTRE_AGENT, List.of())).isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> d.refuser(AUTRE_AGENT, "motif")).isInstanceOf(UnauthorizedException.class);
        assertThat(d.getStatut()).isEqualTo(StatutVerification.EN_COURS);
    }

    @Test
    void approbation_bloquee_tant_qu_il_reste_des_points() {
        DemandeVerification d = enCours();
        assertThatThrownBy(() -> d.approuver(AGENT, List.of("Contrôle « Adresse » non conforme")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Adresse");
        assertThat(d.getStatut()).isEqualTo(StatutVerification.EN_COURS);
    }

    @Test
    void un_agent_ne_traite_jamais_son_propre_espace() {
        DemandeVerification d = DemandeVerification.nouvelle(1L, 10L);
        d.soumettre(List.of());
        assertThatThrownBy(() -> d.prendreEnCharge(AGENT, true)).isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> d.reattribuer(AGENT, true)).isInstanceOf(UnauthorizedException.class);
        assertThat(d.getStatut()).isEqualTo(StatutVerification.EN_ATTENTE);
    }

    @Test
    void on_ne_decide_pas_sans_prise_en_charge() {
        DemandeVerification d = DemandeVerification.nouvelle(1L, 10L);
        d.soumettre(List.of());
        assertThatThrownBy(() -> d.approuver(AGENT, List.of())).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> d.soumettre(List.of())).isInstanceOf(BusinessException.class);
    }

    @Test
    void retrait_possible_seulement_avant_prise_en_charge() {
        DemandeVerification brouillon = DemandeVerification.nouvelle(1L, 10L);
        assertThat(brouillon.retirer().nouveau()).isEqualTo(StatutVerification.ANNULEE);

        DemandeVerification enAttente = DemandeVerification.nouvelle(1L, 10L);
        enAttente.soumettre(List.of());
        assertThat(enAttente.retirer().nouveau()).isEqualTo(StatutVerification.ANNULEE);

        DemandeVerification d = enCours();
        assertThatThrownBy(d::retirer).isInstanceOf(BusinessException.class);
    }

    @Test
    void revocation_seulement_apres_approbation() {
        DemandeVerification d = enCours();
        assertThatThrownBy(() -> d.revoquer("Fraude")).isInstanceOf(BusinessException.class);
        d.approuver(AGENT, List.of());
        assertThatThrownBy(() -> d.revoquer("")).isInstanceOf(BusinessException.class);
        Transition t = d.revoquer("Nom de l'espace modifié après vérification");
        assertThat(t).isEqualTo(new Transition(TypeEvenement.REVOCATION, StatutVerification.APPROUVEE, StatutVerification.REVOQUEE));
    }

    @Test
    void annulation_admin_seulement_sur_demande_ouverte_et_motivee() {
        DemandeVerification d = enCours();
        assertThatThrownBy(() -> d.annuler(null)).isInstanceOf(BusinessException.class);
        d.annuler("Doublon");
        assertThat(d.getStatut()).isEqualTo(StatutVerification.ANNULEE);
        assertThatThrownBy(() -> d.annuler("Encore")).isInstanceOf(BusinessException.class);
    }

    @Test
    void reattribution_vers_un_agent_ou_vers_la_file() {
        DemandeVerification d = DemandeVerification.nouvelle(1L, 10L);
        d.soumettre(List.of());
        d.reattribuer(AUTRE_AGENT, false);
        assertThat(d.getStatut()).isEqualTo(StatutVerification.EN_COURS);
        assertThat(d.getAgentId()).isEqualTo(AUTRE_AGENT);

        d.reattribuer(null, false);
        assertThat(d.getStatut()).isEqualTo(StatutVerification.EN_ATTENTE);
        assertThat(d.getAgentId()).isNull();

        DemandeVerification brouillon = DemandeVerification.nouvelle(1L, 10L);
        assertThatThrownBy(() -> brouillon.reattribuer(AGENT, false)).isInstanceOf(BusinessException.class);
    }
}

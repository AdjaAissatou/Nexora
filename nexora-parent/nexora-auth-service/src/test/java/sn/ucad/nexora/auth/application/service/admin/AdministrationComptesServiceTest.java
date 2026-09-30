package sn.ucad.nexora.auth.application.service.admin;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sn.ucad.nexora.auth.application.dto.admin.AdminComptesDtos.CompteResume;
import sn.ucad.nexora.auth.infrastructure.persistance.admin.ComptesAdminRepository;
import sn.ucad.nexora.common.audit.JournalActions;
import sn.ucad.nexora.common.exception.BusinessException;

class AdministrationComptesServiceTest {

    private final UUID admin = UUID.randomUUID();
    private final UUID cible = UUID.randomUUID();
    private ComptesAdminRepository comptes;
    private JournalActions journal;
    private AdministrationComptesService service;

    @BeforeEach
    void preparer() {
        comptes = mock(ComptesAdminRepository.class);
        journal = mock(JournalActions.class);
        service = new AdministrationComptesService(comptes, journal);
    }

    private void compte(UUID id, String etat, String... roles) {
        when(comptes.compte(id)).thenReturn(Optional.of(new CompteResume(id, 42L, "Awa", "Diop", "awa@test.sn",
                "770000000", List.of(roles), etat, null, 0)));
    }

    @Test
    void on_ne_suspend_jamais_son_propre_compte() {
        assertThatThrownBy(() -> service.suspendre(admin, true, admin, "test"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("votre propre compte");
        verify(comptes, never()).definirSuspension(any(), anyBoolean());
    }

    @Test
    void le_motif_est_obligatoire() {
        assertThatThrownBy(() -> service.suspendre(admin, true, cible, " ")).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.reactiver(admin, cible, null)).isInstanceOf(BusinessException.class);
    }

    @Test
    void un_admin_ne_suspend_pas_un_super_admin() {
        compte(cible, "ACTIF", "SUPER_ADMIN", "UTILISATEUR");
        assertThatThrownBy(() -> service.suspendre(admin, false, cible, "fraude"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("super administrateur");
        verify(comptes, never()).definirSuspension(any(), anyBoolean());
    }

    @Test
    void jamais_sans_super_admin_actif() {
        compte(cible, "ACTIF", "SUPER_ADMIN");
        when(comptes.comptesActifsAvecRole("SUPER_ADMIN")).thenReturn(1L);
        assertThatThrownBy(() -> service.suspendre(admin, true, cible, "départ"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("dernier");
        assertThatThrownBy(() -> service.retirerRole(admin, cible, "SUPER_ADMIN", "départ"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("dernier");
    }

    @Test
    void suspension_ecrit_le_compte_et_le_journal() {
        compte(cible, "ACTIF", "UTILISATEUR");
        service.suspendre(admin, false, cible, "Usurpation d'identité");
        verify(comptes).definirSuspension(cible, true);
        verify(journal).enregistrer(any(), anyString(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void seuls_les_roles_administratifs_se_donnent_ici() {
        compte(cible, "ACTIF", "UTILISATEUR");
        assertThatThrownBy(() -> service.donnerRole(admin, cible, "FOURNISSEUR", "test"))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.donnerRole(admin, cible, "AGENT_VERIFICATION", "test"))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.donnerRole(admin, admin, "ADMIN", "test"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("votre propre compte");
    }

    @Test
    void le_super_admin_garde_ses_permissions_vitales() {
        when(comptes.roleExiste("SUPER_ADMIN")).thenReturn(true);
        when(comptes.permissionExiste("GERER_PERMISSIONS")).thenReturn(true);
        assertThatThrownBy(() -> service.modifierPermission(admin, "SUPER_ADMIN", "GERER_PERMISSIONS", false, "test"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("ne peut pas être retirée");
        verify(comptes, never()).delierPermission(anyString(), anyString());
    }
}

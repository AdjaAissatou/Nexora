package sn.ucad.nexora.espace.application.service.verification;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import sn.ucad.nexora.espace.application.service.verification.VerificationModificationService.InformationsVerifiees;

class InformationsVerifieesTest {

    private static InformationsVerifiees infos(String nom, String tel, String quartier) {
        return new InformationsVerifiees(nom, tel, "NINEA-1", null, null, "Dakar", "Dakar", "Plateau", quartier, null);
    }

    @Test
    void aucune_difference_si_seules_la_casse_et_les_espaces_changent() {
        assertThat(infos("AdjaShop", "771234567", "Médina")
                .differences(infos("  adjashop ", "771234567", "médina"))).isEmpty();
    }

    @Test
    void null_et_vide_sont_equivalents() {
        InformationsVerifiees a = new InformationsVerifiees("A", null, null, null, null, null, null, null, null, null);
        InformationsVerifiees b = new InformationsVerifiees("A", "", " ", null, "", null, null, null, null, "");
        assertThat(a.differences(b)).isEmpty();
    }

    @Test
    void liste_chaque_information_verifiee_modifiee() {
        assertThat(infos("AdjaShop", "771234567", "Médina")
                .differences(infos("Adja Boutique", "781234567", "Fann")))
                .containsExactly("nom", "téléphone", "adresse");
    }
}

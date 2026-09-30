package sn.ucad.nexora.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AutoritesJwtTest {

    @Test
    void roles_et_permissions_deviennent_des_autorites_prefixees() {
        Map<String, Object> claims = Map.of(
                "type", "ACCESS",
                "roles", List.of("ADMIN", "UTILISATEUR"),
                "permissions", List.of("CONSULTER_UTILISATEURS", "GERER_JOURNAL"));
        assertThat(AutoritesJwt.depuis(claims)).containsExactlyInAnyOrder(
                "ROLE_ADMIN", "ROLE_UTILISATEUR", "PERM_CONSULTER_UTILISATEURS", "PERM_GERER_JOURNAL");
        assertThat(AutoritesJwt.estJetonAcces(claims)).isTrue();
    }

    @Test
    void jeton_sans_claims_ou_de_rafraichissement() {
        Map<String, Object> claims = Map.of("type", "REFRESH");
        assertThat(AutoritesJwt.depuis(claims)).isEmpty();
        assertThat(AutoritesJwt.estJetonAcces(claims)).isFalse();
    }
}

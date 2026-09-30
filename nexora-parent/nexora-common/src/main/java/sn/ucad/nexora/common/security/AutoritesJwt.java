package sn.ucad.nexora.common.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Autorités Spring Security déduites d'un jeton d'accès, identiques dans tous les services
 * (docs/architecture-acteurs.md §6) :
 * <ul>
 *   <li>{@code ROLE_<code>} pour chaque rôle du compte (claim {@code roles}) ;</li>
 *   <li>{@code PERM_<code>} pour chaque permission effective (claim {@code permissions}).</li>
 * </ul>
 * Les routes d'administration vérifient une permission ({@code hasAuthority("PERM_...")}), pas une
 * liste de rôles : modifier les permissions d'un rôle change les droits sans toucher au code.
 */
public final class AutoritesJwt {

    public static final String PREFIXE_ROLE = "ROLE_";
    public static final String PREFIXE_PERMISSION = "PERM_";

    private AutoritesJwt() {}

    /** @param claims le contenu du JWT ({@code io.jsonwebtoken.Claims} est une {@code Map}). */
    public static List<String> depuis(Map<String, ?> claims) {
        List<String> autorites = new ArrayList<>();
        ajouter(autorites, claims.get("roles"), PREFIXE_ROLE);
        ajouter(autorites, claims.get("permissions"), PREFIXE_PERMISSION);
        return autorites;
    }

    /** Vrai si le jeton est un jeton d'accès (un jeton de rafraîchissement n'ouvre aucune route). */
    public static boolean estJetonAcces(Map<String, ?> claims) {
        return "ACCESS".equals(claims.get("type"));
    }

    private static void ajouter(List<String> autorites, Object valeurs, String prefixe) {
        if (valeurs instanceof Collection<?> liste) {
            for (Object v : liste) {
                if (v != null && !v.toString().isBlank()) autorites.add(prefixe + v);
            }
        }
    }
}

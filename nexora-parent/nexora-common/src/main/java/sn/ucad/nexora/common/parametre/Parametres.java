package sn.ucad.nexora.common.parametre;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;

/**
 * Lecture des paramètres de Nexora (table {@code parametre}, docs/architecture-acteurs.md §9.12),
 * modifiables depuis le back-office. Lus à chaque appel : une modification s'applique aussitôt.
 * Un paramètre absent ou inactif prend la valeur par défaut fournie par l'appelant.
 */
public class Parametres {

    @PersistenceContext
    private EntityManager em;

    public String texte(String code, String defaut) {
        Object[] l = ligne(code);
        return l == null || l[0] == null ? defaut : (String) l[0];
    }

    public int entier(String code, int defaut) {
        Object[] l = ligne(code);
        return l == null || l[1] == null ? defaut : ((BigDecimal) l[1]).intValue();
    }

    public boolean booleen(String code, boolean defaut) {
        Object[] l = ligne(code);
        return l == null || l[2] == null ? defaut : (Boolean) l[2];
    }

    /** [valeur_texte, valeur_numerique, valeur_booleenne] du paramètre actif, ou null. */
    @SuppressWarnings("unchecked")
    private Object[] ligne(String code) {
        List<Object[]> r = em.createNativeQuery(
                "SELECT valeur_texte, valeur_numerique, valeur_booleenne FROM parametre WHERE code = :c AND actif")
                .setParameter("c", code).getResultList();
        return r.isEmpty() ? null : r.get(0);
    }
}

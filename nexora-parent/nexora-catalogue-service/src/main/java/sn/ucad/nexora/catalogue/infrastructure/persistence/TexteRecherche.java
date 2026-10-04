package sn.ucad.nexora.catalogue.infrastructure.persistence;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Découpe le texte d'une recherche en mots utiles, sans accents, ramenés à leur racine :
 * « Plombier » → « plomb » (qui trouve aussi « plomberie »), « canalisations » → « canalis ».
 * Les mots vides (« le », « de », « pour »…) sont ignorés.
 */
public final class TexteRecherche {

    private static final Set<String> MOTS_VIDES = Set.of(
            "le", "la", "les", "l", "un", "une", "des", "de", "du", "d", "au", "aux", "a", "et", "ou", "en",
            "pour", "par", "sur", "avec", "sans", "dans", "chez", "mon", "ma", "mes", "je", "cherche", "veux");

    /** Terminaisons retirées (la plus longue d'abord) : métier, lieu, pluriel… */
    private static final List<String> TERMINAISONS = List.of(
            "ements", "ations", "ieres", "eries", "euses", "ement", "ation", "iere", "iers", "erie", "euse",
            "eurs", "ages", "ette", "ier", "eur", "age", "es", "s", "x", "e");

    private static final int RACINE_MIN = 4;

    /** Accents retirés et minuscules ; même transformation que {@link #ACCENTS} côté SQL. */
    public static final String ACCENTS = "éèêëàâäîïôöûüùçœ";
    public static final String SANS_ACCENTS = "eeeeaaaiioouuuco";

    private TexteRecherche() {}

    public static String normaliser(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s.toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    /** Mots utiles, normalisés (sans racine), sans doublon, dans l'ordre de saisie. */
    public static List<String> mots(String q) {
        Set<String> mots = new LinkedHashSet<>();
        for (String m : normaliser(q).split("[^a-z0-9]+")) {
            if (m.length() >= 2 && !MOTS_VIDES.contains(m)) mots.add(m);
        }
        return new ArrayList<>(mots);
    }

    /** Racine d'un mot normalisé : « plombier » → « plomb » ; un mot court reste entier. */
    public static String racine(String mot) {
        for (String t : TERMINAISONS) {
            if (mot.endsWith(t) && mot.length() - t.length() >= RACINE_MIN) {
                return mot.substring(0, mot.length() - t.length());
            }
        }
        return mot;
    }
}

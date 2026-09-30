package sn.ucad.nexora.administration.application.service.parametre;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.common.audit.JournalActions;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;

/**
 * Paramètres de Nexora (docs/architecture-acteurs.md §9.12). Seuls les paramètres « connus » ici
 * se modifient : chacun est lu par le code d'un service, et ses bornes sont vérifiées à la saisie.
 * La permission GERER_PARAMETRES est vérifiée par SecurityConfig ; chaque modification est motivée
 * et journalisée (module PARAMETRES) avec l'ancienne et la nouvelle valeur.
 */
@Service
public class ParametresService {

    public enum Genre { TEXTE, EMAIL, NOMBRE, BOOLEEN }

    /** Définition d'un paramètre : sa nature, ses bornes, et s'il est lisible publiquement (par le web). */
    public record Definition(Genre genre, int min, int max, boolean publique) {}

    public static final Map<String, Definition> CONNUS = Map.of(
            "SITE_BANDEAU", new Definition(Genre.TEXTE, 0, 300, true),
            "SITE_CONTACT_EMAIL", new Definition(Genre.EMAIL, 0, 150, true),
            "SITE_CONTACT_TELEPHONE", new Definition(Genre.TEXTE, 0, 30, true),
            "INSCRIPTIONS_OUVERTES", new Definition(Genre.BOOLEEN, 0, 0, true),
            "ESPACES_MAX_PAR_COMPTE", new Definition(Genre.NOMBRE, 1, 50, false),
            "AVIS_LONGUEUR_MAX", new Definition(Genre.NOMBRE, 100, 5000, true),
            "VERIFICATION_DELAI_JOURS", new Definition(Genre.NOMBRE, 1, 60, true));

    public record Parametre(String code, String libelle, String description, String categorie, String genre, int min, int max,
                            String valeur, LocalDateTime dateModification) {}

    public record ModificationRequest(String valeur, String motif) {}

    @PersistenceContext
    private EntityManager em;

    private final JournalActions journal;

    public ParametresService(JournalActions journal) {
        this.journal = journal;
    }

    @Transactional(readOnly = true)
    public List<Parametre> tous() {
        return lignes().stream().filter(p -> CONNUS.containsKey(p.code())).toList();
    }

    /** Les paramètres dont le web a besoin pour tous ses visiteurs : code → valeur. */
    @Transactional(readOnly = true)
    public Map<String, String> publics() {
        Map<String, String> m = new LinkedHashMap<>();
        tous().stream().filter(p -> CONNUS.get(p.code()).publique()).forEach(p -> m.put(p.code(), p.valeur()));
        return m;
    }

    @Transactional
    public Parametre modifier(UUID auteur, String code, String valeur, String motif) {
        if (motif == null || motif.isBlank()) throw new BusinessException("Le motif est obligatoire");
        Definition d = CONNUS.get(code);
        Parametre avant = lignes().stream().filter(p -> p.code().equals(code)).findFirst().orElse(null);
        if (d == null || avant == null) throw new ResourceNotFoundException("Paramètre inconnu : " + code);
        String v = valeur == null ? "" : valeur.trim();
        String colonne;
        Object nouvelle;
        switch (d.genre()) {
            case BOOLEEN -> {
                if (!v.equals("true") && !v.equals("false")) throw new BusinessException("Valeur attendue : oui ou non");
                colonne = "valeur_booleenne";
                nouvelle = Boolean.valueOf(v);
            }
            case NOMBRE -> {
                int n;
                try {
                    n = Integer.parseInt(v);
                } catch (NumberFormatException e) {
                    throw new BusinessException("Un nombre entier est attendu");
                }
                if (n < d.min() || n > d.max()) throw new BusinessException("Valeur attendue entre " + d.min() + " et " + d.max());
                colonne = "valeur_numerique";
                nouvelle = BigDecimal.valueOf(n);
            }
            default -> {
                if (v.length() > d.max()) throw new BusinessException("Texte trop long (" + d.max() + " caractères au plus)");
                if (d.genre() == Genre.EMAIL && !v.isEmpty() && !v.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
                    throw new BusinessException("Adresse e-mail invalide");
                }
                colonne = "valeur_texte";
                nouvelle = v.isEmpty() ? null : v;
            }
        }
        String ancienneAffichee = affichage(avant.valeur());
        String nouvelleAffichee = affichage(nouvelle == null ? null : nouvelle.toString());
        if (ancienneAffichee.equals(nouvelleAffichee)) throw new BusinessException("La valeur n'a pas changé");
        em.createNativeQuery("UPDATE parametre SET " + colonne + " = :v, date_modification = NOW() WHERE code = :c")
                .setParameter("v", nouvelle).setParameter("c", code).executeUpdate();
        journal.enregistrer(auteur, "PARAMETRES", "MODIFIER_PARAMETRE", "parametre", null,
                "« " + avant.libelle() + " » : " + ancienneAffichee + " → " + nouvelleAffichee + " (" + motif.trim() + ")");
        return lignes().stream().filter(p -> p.code().equals(code)).findFirst().orElseThrow();
    }

    private static String affichage(String v) {
        if (v == null || v.isBlank()) return "(vide)";
        if (v.equals("true")) return "oui";
        if (v.equals("false")) return "non";
        return v.endsWith(".00") ? v.substring(0, v.length() - 3) : v;
    }

    @SuppressWarnings("unchecked")
    private List<Parametre> lignes() {
        List<Object[]> r = em.createNativeQuery("""
                SELECT code, libelle, description, categorie,
                       COALESCE(valeur_texte, CAST(CAST(valeur_numerique AS INTEGER) AS TEXT), CAST(valeur_booleenne AS TEXT)),
                       date_modification
                FROM parametre WHERE actif ORDER BY categorie, libelle
                """).getResultList();
        return r.stream().map(l -> {
            String code = (String) l[0];
            Definition d = CONNUS.get(code);
            return new Parametre(code, (String) l[1], (String) l[2], (String) l[3], d == null ? null : d.genre().name(),
                    d == null ? 0 : d.min(), d == null ? 0 : d.max(), (String) l[4],
                    l[5] instanceof Timestamp t ? t.toLocalDateTime() : (LocalDateTime) l[5]);
        }).toList();
    }
}

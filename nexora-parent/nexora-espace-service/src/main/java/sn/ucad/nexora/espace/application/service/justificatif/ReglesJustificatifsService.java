package sn.ucad.nexora.espace.application.service.justificatif;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
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
 * Règles des justificatifs de la vérification (docs/architecture-acteurs.md §8.3 et §9.12) : les
 * types de justificatif, et ce qui est demandé à chaque type d'espace (règles générales + règles
 * propres au type). Permission GERER_PARAMETRES (SecurityConfig). Les règles s'appliquent aussitôt
 * à toutes les demandes non décidées : la complétude est recalculée à chaque lecture.
 */
@Service
public class ReglesJustificatifsService {

    private static final String MODULE = "PARAMETRES";

    public record TypeJustificatif(Long id, String code, String libelle, String description, boolean actif, long regles, long documents) {}

    public record TypeEspace(Long id, String nom) {}

    /** {@code typeEspaceId} null : la règle vaut pour tous les types d'espace. */
    public record Regle(Long id, Long typeEspaceId, String typeEspace, Long typeJustificatifId, String code, String libelle,
                        boolean obligatoire, String groupeAlternatif) {}

    public record Vue(List<TypeJustificatif> types, List<TypeEspace> typesEspace, List<Regle> regles) {}

    public record TypeRequest(String code, String libelle, String description) {}

    public record RegleRequest(Long typeEspaceId, Long typeJustificatifId, Boolean obligatoire, String groupeAlternatif) {}

    public record MotifRequest(String motif) {}

    @PersistenceContext
    private EntityManager em;

    private final JournalActions journal;

    public ReglesJustificatifsService(JournalActions journal) {
        this.journal = journal;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Vue vue() {
        List<Object[]> t = em.createNativeQuery("""
                SELECT tj.id_type_justificatif, tj.code, tj.libelle, tj.description, tj.actif,
                       (SELECT COUNT(*) FROM justificatif_requis r WHERE r.id_type_justificatif = tj.id_type_justificatif),
                       (SELECT COUNT(*) FROM verification_document d WHERE d.id_type_justificatif = tj.id_type_justificatif)
                FROM type_justificatif tj ORDER BY tj.id_type_justificatif
                """).getResultList();
        List<Object[]> te = em.createNativeQuery("SELECT id_type_espace, nom FROM type_espace ORDER BY ordre_affichage, nom").getResultList();
        List<Object[]> r = em.createNativeQuery("""
                SELECT jr.id_justificatif_requis, jr.id_type_espace, te.nom, tj.id_type_justificatif, tj.code, tj.libelle,
                       jr.obligatoire, jr.groupe_alternatif
                FROM justificatif_requis jr
                JOIN type_justificatif tj ON tj.id_type_justificatif = jr.id_type_justificatif
                LEFT JOIN type_espace te ON te.id_type_espace = jr.id_type_espace
                ORDER BY te.nom NULLS FIRST, jr.obligatoire DESC, tj.id_type_justificatif
                """).getResultList();
        return new Vue(
                t.stream().map(l -> new TypeJustificatif(n(l[0]), (String) l[1], (String) l[2], (String) l[3], Boolean.TRUE.equals(l[4]),
                        n(l[5]), n(l[6]))).toList(),
                te.stream().map(l -> new TypeEspace(n(l[0]), (String) l[1])).toList(),
                r.stream().map(l -> new Regle(n(l[0]), l[1] == null ? null : n(l[1]), (String) l[2], n(l[3]), (String) l[4], (String) l[5],
                        Boolean.TRUE.equals(l[6]), (String) l[7])).toList());
    }

    // ------------------------------------------------------------------ types de justificatif

    @Transactional
    public Vue creerType(UUID auteur, TypeRequest r) {
        String libelle = obligatoire(r.libelle(), "Le libellé", 150);
        String code = r.code() == null || r.code().isBlank() ? code(libelle) : code(r.code());
        if (compter("SELECT COUNT(*) FROM type_justificatif WHERE code = :c OR LOWER(libelle) = LOWER(:l)", Map.of("c", code, "l", libelle)) > 0) {
            throw new BusinessException("Ce type de justificatif existe déjà (même code ou même libellé)");
        }
        executer("INSERT INTO type_justificatif (code, libelle, description, actif) VALUES (:c, :l, :d, TRUE)",
                p("c", code, "l", libelle, "d", facultatif(r.description())));
        journal.enregistrer(auteur, MODULE, "CREER_TYPE_JUSTIFICATIF", "type_justificatif", null, "Type de justificatif « " + libelle + " » (" + code + ") créé");
        return vue();
    }

    @Transactional
    public Vue modifierType(UUID auteur, Long id, TypeRequest r) {
        Object[] t = type(id);
        String libelle = obligatoire(r.libelle(), "Le libellé", 150);
        if (compter("SELECT COUNT(*) FROM type_justificatif WHERE LOWER(libelle) = LOWER(:l) AND id_type_justificatif <> :id", Map.of("l", libelle, "id", id)) > 0) {
            throw new BusinessException("Un autre type de justificatif porte déjà ce libellé");
        }
        executer("UPDATE type_justificatif SET libelle = :l, description = :d WHERE id_type_justificatif = :id",
                p("l", libelle, "d", facultatif(r.description()), "id", id));
        journal.enregistrer(auteur, MODULE, "MODIFIER_TYPE_JUSTIFICATIF", "type_justificatif", id, "Type de justificatif « " + t[1] + " » modifié");
        return vue();
    }

    @Transactional
    public Vue activerType(UUID auteur, Long id, boolean actif, String motif) {
        exigerMotif(motif);
        Object[] t = type(id);
        if (Boolean.TRUE.equals(t[2]) == actif) throw new BusinessException(actif ? "Ce type est déjà actif" : "Ce type est déjà désactivé");
        if (!actif && compter("SELECT COUNT(*) FROM justificatif_requis WHERE id_type_justificatif = :id", Map.of("id", id)) > 0) {
            throw new BusinessException("Des règles demandent encore ce justificatif : retirez-les d'abord");
        }
        executer("UPDATE type_justificatif SET actif = :a WHERE id_type_justificatif = :id", p("a", actif, "id", id));
        journal.enregistrer(auteur, MODULE, actif ? "ACTIVER_TYPE_JUSTIFICATIF" : "DESACTIVER_TYPE_JUSTIFICATIF", "type_justificatif", id,
                "Type de justificatif « " + t[1] + " » " + (actif ? "réactivé" : "désactivé") + " : " + motif.trim());
        return vue();
    }

    // ------------------------------------------------------------------ règles

    @Transactional
    public Vue ajouterRegle(UUID auteur, RegleRequest r) {
        if (r.typeJustificatifId() == null) throw new BusinessException("Choisissez un justificatif");
        Object[] t = type(r.typeJustificatifId());
        if (!Boolean.TRUE.equals(t[2])) throw new BusinessException("Ce type de justificatif est désactivé");
        String typeEspace = r.typeEspaceId() == null ? "tous les espaces" : typeEspace(r.typeEspaceId());
        // Une règle générale et une règle propre au type sur le même justificatif se doubleraient.
        long doublons = compter("""
                SELECT COUNT(*) FROM justificatif_requis WHERE id_type_justificatif = :tj
                  AND (id_type_espace IS NULL OR CAST(CAST(:te AS TEXT) AS BIGINT) IS NULL
                       OR id_type_espace = CAST(CAST(:te AS TEXT) AS BIGINT))""",
                p("tj", r.typeJustificatifId(), "te", r.typeEspaceId() == null ? null : r.typeEspaceId().toString()));
        if (doublons > 0) throw new BusinessException("Ce justificatif est déjà demandé (règle générale ou propre à ce type d'espace)");
        String groupe = groupe(r.groupeAlternatif());
        boolean obligatoire = !Boolean.FALSE.equals(r.obligatoire());
        executer("""
                INSERT INTO justificatif_requis (id_type_espace, id_type_justificatif, obligatoire, groupe_alternatif)
                VALUES (CAST(CAST(:te AS TEXT) AS BIGINT), :tj, :o, :g)""",
                p("te", r.typeEspaceId() == null ? null : r.typeEspaceId().toString(), "tj", r.typeJustificatifId(), "o", obligatoire, "g", groupe));
        journal.enregistrer(auteur, MODULE, "AJOUTER_REGLE_JUSTIFICATIF", "type_justificatif", r.typeJustificatifId(),
                "« " + t[1] + " » demandé pour " + typeEspace + (obligatoire ? " (obligatoire" : " (facultatif")
                        + (groupe == null ? ")" : ", au choix dans le groupe " + groupe + ")"));
        return vue();
    }

    @Transactional
    public Vue modifierRegle(UUID auteur, Long id, RegleRequest r) {
        Object[] regle = regle(id);
        boolean obligatoire = !Boolean.FALSE.equals(r.obligatoire());
        String groupe = groupe(r.groupeAlternatif());
        if (!obligatoire) exigerAutreObligatoireGenerale(id, regle);
        executer("UPDATE justificatif_requis SET obligatoire = :o, groupe_alternatif = :g WHERE id_justificatif_requis = :id",
                p("o", obligatoire, "g", groupe, "id", id));
        journal.enregistrer(auteur, MODULE, "MODIFIER_REGLE_JUSTIFICATIF", "type_justificatif", n(regle[1]),
                "« " + regle[2] + " » pour " + (regle[0] == null ? "tous les espaces" : regle[3]) + " : "
                        + (obligatoire ? "obligatoire" : "facultatif") + (groupe == null ? "" : ", groupe " + groupe));
        return vue();
    }

    @Transactional
    public Vue supprimerRegle(UUID auteur, Long id, String motif) {
        exigerMotif(motif);
        Object[] regle = regle(id);
        exigerAutreObligatoireGenerale(id, regle);
        executer("DELETE FROM justificatif_requis WHERE id_justificatif_requis = :id", p("id", id));
        journal.enregistrer(auteur, MODULE, "RETIRER_REGLE_JUSTIFICATIF", "type_justificatif", n(regle[1]),
                "« " + regle[2] + " » n'est plus demandé pour " + (regle[0] == null ? "tous les espaces" : regle[3]) + " : " + motif.trim());
        return vue();
    }

    /** Il reste toujours au moins un justificatif obligatoire pour tous les espaces. */
    private void exigerAutreObligatoireGenerale(Long id, Object[] regle) {
        if (regle[0] == null && Boolean.TRUE.equals(regle[4])
                && compter("SELECT COUNT(*) FROM justificatif_requis WHERE id_type_espace IS NULL AND obligatoire AND id_justificatif_requis <> :id",
                Map.of("id", id)) == 0) {
            throw new BusinessException("Il doit rester au moins un justificatif obligatoire pour tous les espaces");
        }
    }

    // ------------------------------------------------------------------ outils

    /** [id, libelle, actif] */
    private Object[] type(Long id) {
        return ligne("SELECT id_type_justificatif, libelle, actif FROM type_justificatif WHERE id_type_justificatif = :id", id)
                .orElseThrow(() -> new ResourceNotFoundException("Type de justificatif introuvable"));
    }

    /** [id_type_espace, id_type_justificatif, libelle justificatif, nom type espace, obligatoire] */
    private Object[] regle(Long id) {
        return ligne("""
                SELECT jr.id_type_espace, jr.id_type_justificatif, tj.libelle, te.nom, jr.obligatoire
                FROM justificatif_requis jr JOIN type_justificatif tj ON tj.id_type_justificatif = jr.id_type_justificatif
                LEFT JOIN type_espace te ON te.id_type_espace = jr.id_type_espace WHERE jr.id_justificatif_requis = :id""", id)
                .orElseThrow(() -> new ResourceNotFoundException("Règle introuvable"));
    }

    private String typeEspace(Long id) {
        return ligne("SELECT nom, id_type_espace FROM type_espace WHERE id_type_espace = :id", id).map(l -> (String) l[0])
                .orElseThrow(() -> new ResourceNotFoundException("Type d'espace introuvable"));
    }

    @SuppressWarnings("unchecked")
    private java.util.Optional<Object[]> ligne(String sql, Long id) {
        List<Object[]> r = em.createNativeQuery(sql).setParameter("id", id).getResultList();
        return r.stream().findFirst();
    }

    private long compter(String sql, Map<String, Object> parametres) {
        Query q = em.createNativeQuery(sql);
        parametres.forEach(q::setParameter);
        return ((Number) q.getSingleResult()).longValue();
    }

    private void executer(String sql, Map<String, Object> parametres) {
        Query q = em.createNativeQuery(sql);
        parametres.forEach(q::setParameter);
        q.executeUpdate();
    }

    private static String code(String s) {
        String c = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toUpperCase().replaceAll("[^A-Z0-9]+", "_").replaceAll("^_+|_+$", "");
        if (c.isEmpty()) throw new BusinessException("Code invalide");
        return c.length() > 50 ? c.substring(0, 50) : c;
    }

    /** Code de groupe : « justificatif d'adresse » → JUSTIFICATIF_D_ADRESSE ; vide → pas de groupe. */
    private static String groupe(String s) {
        return s == null || s.isBlank() ? null : code(s);
    }

    private static String obligatoire(String s, String quoi, int max) {
        if (s == null || s.isBlank()) throw new BusinessException(quoi + " est obligatoire");
        if (s.trim().length() > max) throw new BusinessException(quoi + " est trop long");
        return s.trim();
    }

    private static String facultatif(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static void exigerMotif(String motif) {
        if (motif == null || motif.isBlank()) throw new BusinessException("Le motif est obligatoire");
    }

    private static long n(Object o) {
        return ((Number) o).longValue();
    }

    private static Map<String, Object> p(Object... cv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < cv.length; i += 2) m.put((String) cv[i], cv[i + 1]);
        return m;
    }
}

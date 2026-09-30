package sn.ucad.nexora.espace.infrastructure.persistence.horaire;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.Date;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Repository;
import sn.ucad.nexora.espace.domain.horaire.Horaires;

/** Tables {@code horaire} et {@code horaire_exception}, en SQL natif (§10). */
@Repository
public class HoraireRepository {

    private static final List<String> JOURS = List.of("LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI", "DIMANCHE");

    @PersistenceContext
    private EntityManager em;

    public static DayOfWeek jour(String code) {
        int i = JOURS.indexOf(code);
        if (i < 0) throw new IllegalArgumentException("Jour inconnu : " + code);
        return DayOfWeek.of(i + 1);
    }

    public static String code(DayOfWeek jour) {
        return JOURS.get(jour.getValue() - 1);
    }

    @SuppressWarnings("unchecked")
    public List<Horaires.Plage> semaine(Long espaceId) {
        List<Object[]> r = em.createNativeQuery("""
                SELECT jour_semaine, ouvert, ouvert_24h, heure_ouverture, heure_fermeture, pause_debut, pause_fin
                FROM horaire WHERE id_espace = :id
                """).setParameter("id", espaceId).getResultList();
        return r.stream().map(l -> new Horaires.Plage(jour((String) l[0]), Boolean.TRUE.equals(l[1]), Boolean.TRUE.equals(l[2]),
                heure(l[3]), heure(l[4]), heure(l[5]), heure(l[6]))).toList();
    }

    /** Exceptions à partir d'une date (la veille comprise, pour la nuit qui déborde). */
    @SuppressWarnings("unchecked")
    public List<Object[]> exceptions(Long espaceId, LocalDate depuis) {
        return em.createNativeQuery("""
                SELECT id_exception, date_exception, ferme, heure_ouverture, heure_fermeture, motif
                FROM horaire_exception WHERE id_espace = :id AND date_exception >= :depuis ORDER BY date_exception
                """).setParameter("id", espaceId).setParameter("depuis", depuis).getResultList();
    }

    public static Horaires.Exception exception(Object[] l) {
        return new Horaires.Exception(date(l[1]), Boolean.TRUE.equals(l[2]), heure(l[3]), heure(l[4]), (String) l[5]);
    }

    public void remplacerSemaine(Long espaceId, List<Horaires.Plage> plages) {
        em.createNativeQuery("DELETE FROM horaire WHERE id_espace = :id").setParameter("id", espaceId).executeUpdate();
        for (Horaires.Plage p : plages) {
            boolean horaires = p.ouvert() && !p.ouvert24h();
            em.createNativeQuery("""
                    INSERT INTO horaire (id_espace, jour_semaine, ouvert, ouvert_24h, heure_ouverture, heure_fermeture, pause_debut, pause_fin)
                    VALUES (:id, :jour, :ouvert, :h24, CAST(:ouv AS TIME), CAST(:ferm AS TIME), CAST(:pd AS TIME), CAST(:pf AS TIME))
                    """)
                    .setParameter("id", espaceId).setParameter("jour", code(p.jour()))
                    .setParameter("ouvert", p.ouvert()).setParameter("h24", p.ouvert() && p.ouvert24h())
                    .setParameter("ouv", horaires ? texte(p.ouverture()) : null)
                    .setParameter("ferm", horaires ? texte(p.fermeture()) : null)
                    .setParameter("pd", horaires ? texte(p.pauseDebut()) : null)
                    .setParameter("pf", horaires ? texte(p.pauseFin()) : null)
                    .executeUpdate();
        }
    }

    /** Une exception par date : la nouvelle remplace l'ancienne. */
    public void enregistrerException(Long espaceId, Horaires.Exception e) {
        em.createNativeQuery("""
                INSERT INTO horaire_exception (id_espace, date_exception, heure_ouverture, heure_fermeture, ferme, motif)
                VALUES (:id, :date, CAST(:ouv AS TIME), CAST(:ferm AS TIME), :ferme, :motif)
                ON CONFLICT (id_espace, date_exception) DO UPDATE SET heure_ouverture = EXCLUDED.heure_ouverture,
                    heure_fermeture = EXCLUDED.heure_fermeture, ferme = EXCLUDED.ferme, motif = EXCLUDED.motif
                """)
                .setParameter("id", espaceId).setParameter("date", e.date())
                .setParameter("ouv", e.ferme() ? null : texte(e.ouverture()))
                .setParameter("ferm", e.ferme() ? null : texte(e.fermeture()))
                .setParameter("ferme", e.ferme()).setParameter("motif", e.motif())
                .executeUpdate();
    }

    public int supprimerException(Long espaceId, Long exceptionId) {
        return em.createNativeQuery("DELETE FROM horaire_exception WHERE id_espace = :e AND id_exception = :x")
                .setParameter("e", espaceId).setParameter("x", exceptionId).executeUpdate();
    }

    private static String texte(LocalTime t) {
        return t == null ? null : t.toString();
    }

    private static LocalTime heure(Object o) {
        if (o instanceof LocalTime t) return t;
        if (o instanceof Time t) return t.toLocalTime();
        return null;
    }

    private static LocalDate date(Object o) {
        if (o instanceof LocalDate d) return d;
        if (o instanceof Date d) return d.toLocalDate();
        return null;
    }
}

package sn.ucad.nexora.espace.domain.horaire;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Horaires d'un espace (docs/architecture-acteurs.md §10) : la semaine type, les exceptions, et
 * l'état « ouvert / fermé » à un instant donné, avec son libellé (« Ouvert · ferme à 19 h »).
 *
 * Règles (identiques à la fonction SQL {@code espace_ouvert_a}) :
 * <ul>
 *   <li>un jour est fermé, ouvert 24 h/24, ou ouvert sur [ouverture, fermeture) avec une pause
 *       facultative [pauseDebut, pauseFin) ;</li>
 *   <li>une fermeture ≤ ouverture signifie que la plage passe minuit (18 h – 2 h) ;</li>
 *   <li>une exception remplace la semaine type pour sa date (fermé, ou une plage dans la journée),
 *       sans annuler la nuit qui déborde de la veille ;</li>
 *   <li>tout est à l'heure de Dakar.</li>
 * </ul>
 */
public final class Horaires {

    public static final ZoneId DAKAR = ZoneId.of("Africa/Dakar");

    /** Une journée de la semaine type. */
    public record Plage(DayOfWeek jour, boolean ouvert, boolean ouvert24h, LocalTime ouverture, LocalTime fermeture,
                        LocalTime pauseDebut, LocalTime pauseFin) {

        public boolean passeMinuit() {
            return ouvert && !ouvert24h && ouverture != null && fermeture != null && !fermeture.isAfter(ouverture);
        }

        public boolean avecPause() {
            return pauseDebut != null && pauseFin != null;
        }
    }

    /** Un jour précis qui remplace la semaine type. */
    public record Exception(LocalDate date, boolean ferme, LocalTime ouverture, LocalTime fermeture, String motif) {}

    /** {@code ouvert} : null si les horaires ne sont pas renseignés. */
    public record Etat(Boolean ouvert, String libelle) {}

    private record Intervalle(LocalDateTime debut, LocalDateTime fin) {}

    private final Map<DayOfWeek, Plage> semaine = new EnumMap<>(DayOfWeek.class);
    private final Map<LocalDate, Exception> exceptions;

    public Horaires(List<Plage> plages, List<Exception> exceptions) {
        plages.forEach(p -> semaine.put(p.jour(), p));
        this.exceptions = new java.util.HashMap<>();
        exceptions.forEach(e -> this.exceptions.put(e.date(), e));
    }

    public boolean isRenseignes() {
        return !semaine.isEmpty();
    }

    public static LocalDateTime maintenant() {
        return LocalDateTime.now(DAKAR);
    }

    // ------------------------------------------------------------------ état

    public Etat etat(boolean ouvertParLeProfessionnel, LocalDateTime instant) {
        if (!ouvertParLeProfessionnel) return new Etat(false, "Fermé temporairement");
        if (!isRenseignes()) return new Etat(null, null);
        List<Intervalle> intervalles = intervalles(instant.toLocalDate().minusDays(1), 9);
        Optional<Intervalle> courant = intervalles.stream()
                .filter(i -> !i.debut().isAfter(instant) && i.fin().isAfter(instant)).findFirst();
        if (courant.isPresent()) {
            LocalDateTime fin = courant.get().fin();
            if (fin.isAfter(instant.plusDays(6))) return new Etat(true, "Ouvert 24 h/24");
            return new Etat(true, "Ouvert · ferme " + quand(instant, fin));
        }
        return intervalles.stream().filter(i -> i.debut().isAfter(instant)).findFirst()
                .map(i -> new Etat(false, "Fermé · ouvre " + quand(instant, i.debut())))
                .orElse(new Etat(false, "Fermé"));
    }

    public boolean estOuvert(LocalDateTime instant) {
        return isRenseignes() && intervalles(instant.toLocalDate().minusDays(1), 3).stream()
                .anyMatch(i -> !i.debut().isAfter(instant) && i.fin().isAfter(instant));
    }

    /** Intervalles d'ouverture de {@code jours} jours à partir de {@code depuis}, fusionnés et triés. */
    private List<Intervalle> intervalles(LocalDate depuis, int jours) {
        List<Intervalle> bruts = new ArrayList<>();
        for (int k = 0; k < jours; k++) {
            LocalDate d = depuis.plusDays(k);
            Exception ex = exceptions.get(d);
            if (ex != null) {
                if (!ex.ferme() && ex.ouverture() != null && ex.fermeture() != null && ex.fermeture().isAfter(ex.ouverture())) {
                    bruts.add(new Intervalle(d.atTime(ex.ouverture()), d.atTime(ex.fermeture())));
                }
                continue;
            }
            Plage p = semaine.get(d.getDayOfWeek());
            if (p == null || !p.ouvert()) continue;
            if (p.ouvert24h()) {
                bruts.add(new Intervalle(d.atStartOfDay(), d.plusDays(1).atStartOfDay()));
                continue;
            }
            if (p.ouverture() == null || p.fermeture() == null) continue;
            LocalDateTime debut = d.atTime(p.ouverture());
            LocalDateTime fin = p.passeMinuit() ? d.plusDays(1).atTime(p.fermeture()) : d.atTime(p.fermeture());
            if (p.avecPause()) {
                LocalDateTime pd = d.atTime(p.pauseDebut());
                LocalDateTime pf = d.atTime(p.pauseFin());
                if (pd.isAfter(debut) && pf.isAfter(pd) && pf.isBefore(fin)) {
                    bruts.add(new Intervalle(debut, pd));
                    bruts.add(new Intervalle(pf, fin));
                    continue;
                }
            }
            bruts.add(new Intervalle(debut, fin));
        }
        bruts.sort(Comparator.comparing(Intervalle::debut));
        List<Intervalle> fusion = new ArrayList<>();
        for (Intervalle i : bruts) {
            Intervalle dernier = fusion.isEmpty() ? null : fusion.get(fusion.size() - 1);
            if (dernier != null && !i.debut().isAfter(dernier.fin())) {
                fusion.set(fusion.size() - 1, new Intervalle(dernier.debut(), i.fin().isAfter(dernier.fin()) ? i.fin() : dernier.fin()));
            } else {
                fusion.add(i);
            }
        }
        return fusion;
    }

    // ------------------------------------------------------------------ libellés

    private static final String[] JOURS = {"lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche"};

    /** « à 19 h », « à minuit », « demain à 8 h », « lundi à 8 h 30 ». */
    static String quand(LocalDateTime maintenant, LocalDateTime moment) {
        LocalDate aujourdhui = maintenant.toLocalDate();
        if (moment.toLocalTime().equals(LocalTime.MIDNIGHT)) {
            // Minuit appartient à la journée qui se termine : « vendredi à minuit », pas « samedi à 0 h ».
            LocalDate veille = moment.toLocalDate().minusDays(1);
            if (veille.equals(aujourdhui)) return "à minuit";
            if (veille.equals(aujourdhui.plusDays(1))) return "demain à minuit";
            return JOURS[veille.getDayOfWeek().getValue() - 1] + " à minuit";
        }
        String heure = "à " + heure(moment.toLocalTime());
        if (moment.toLocalDate().equals(aujourdhui)) return heure;
        if (moment.toLocalDate().equals(aujourdhui.plusDays(1))) return "demain " + heure;
        return JOURS[moment.getDayOfWeek().getValue() - 1] + " " + heure;
    }

    /** 19:00 → « 19 h », 08:30 → « 8 h 30 ». */
    public static String heure(LocalTime t) {
        return t.getHour() + " h" + (t.getMinute() == 0 ? "" : " " + String.format("%02d", t.getMinute()));
    }

    /** Résumé d'une journée : « Fermé », « 24 h/24 », « 8 h – 19 h (pause 13 h – 15 h) ». */
    public static String resume(Plage p) {
        if (p == null || !p.ouvert()) return "Fermé";
        if (p.ouvert24h()) return "24 h/24";
        if (p.ouverture() == null || p.fermeture() == null) return "Fermé";
        String r = heure(p.ouverture()) + " – " + heure(p.fermeture());
        return p.avecPause() ? r + " (pause " + heure(p.pauseDebut()) + " – " + heure(p.pauseFin()) + ")" : r;
    }

    public static String resume(Exception e) {
        if (e.ferme() || e.ouverture() == null || e.fermeture() == null) return "Fermé";
        return heure(e.ouverture()) + " – " + heure(e.fermeture());
    }

    public static String nomJour(DayOfWeek jour) {
        String j = JOURS[jour.getValue() - 1];
        return Character.toUpperCase(j.charAt(0)) + j.substring(1);
    }

    // ------------------------------------------------------------------ validation

    /** Erreurs de saisie de la semaine type (vide si tout est cohérent). */
    public static List<String> erreurs(List<Plage> plages) {
        List<String> erreurs = new ArrayList<>();
        java.util.Set<DayOfWeek> vus = java.util.EnumSet.noneOf(DayOfWeek.class);
        for (Plage p : plages) {
            if (p.jour() == null) { erreurs.add("Jour manquant"); continue; }
            String j = nomJour(p.jour());
            if (!vus.add(p.jour())) erreurs.add(j + " : jour en double");
            if (!p.ouvert() || p.ouvert24h()) continue;
            if (p.ouverture() == null || p.fermeture() == null) { erreurs.add(j + " : indiquez l'heure d'ouverture et de fermeture"); continue; }
            if (p.ouverture().equals(p.fermeture())) erreurs.add(j + " : l'ouverture et la fermeture sont identiques (cochez 24 h/24 si c'est le cas)");
            if ((p.pauseDebut() == null) != (p.pauseFin() == null)) { erreurs.add(j + " : indiquez le début et la fin de la pause"); continue; }
            if (p.avecPause()) {
                boolean dansLaJournee = p.pauseDebut().isAfter(p.ouverture()) && p.pauseFin().isAfter(p.pauseDebut())
                        && (p.passeMinuit() || p.pauseFin().isBefore(p.fermeture()));
                if (!dansLaJournee) erreurs.add(j + " : la pause doit se situer entre l'ouverture et la fermeture");
            }
        }
        return erreurs;
    }

    public static List<String> erreurs(Exception e, LocalDate aujourdhui) {
        List<String> erreurs = new ArrayList<>();
        if (e.date() == null) erreurs.add("Indiquez la date");
        else if (e.date().isBefore(aujourdhui)) erreurs.add("La date est déjà passée");
        if (!e.ferme()) {
            if (e.ouverture() == null || e.fermeture() == null) erreurs.add("Indiquez les heures, ou cochez « fermé toute la journée »");
            else if (!e.fermeture().isAfter(e.ouverture())) erreurs.add("La fermeture doit suivre l'ouverture (une exception tient dans la journée)");
        }
        if (e.motif() != null && e.motif().length() > 255) erreurs.add("Motif trop long (255 caractères au plus)");
        return erreurs;
    }
}

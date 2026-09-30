package sn.ucad.nexora.espace.domain.horaire;

import static java.time.DayOfWeek.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import sn.ucad.nexora.espace.domain.horaire.Horaires.Plage;

class HorairesTest {

    // Semaine du lundi 5 octobre 2026.
    private static final LocalDate LUNDI_5 = LocalDate.of(2026, 10, 5);

    private static LocalTime h(String t) { return LocalTime.parse(t); }
    private static LocalDateTime le(int jourOctobre, String heure) { return LocalDate.of(2026, 10, jourOctobre).atTime(h(heure)); }

    private static Plage plage(DayOfWeek j, String ouv, String ferm) { return new Plage(j, true, false, h(ouv), h(ferm), null, null); }
    private static Plage plage(DayOfWeek j, String ouv, String ferm, String pd, String pf) { return new Plage(j, true, false, h(ouv), h(ferm), h(pd), h(pf)); }
    private static Plage ferme(DayOfWeek j) { return new Plage(j, false, false, null, null, null, null); }
    private static Plage h24(DayOfWeek j) { return new Plage(j, true, true, null, null, null, null); }

    /** Boutique : 8 h – 19 h avec pause 13 h – 15 h du lundi au samedi, fermée le dimanche. */
    private static Horaires boutique(List<Horaires.Exception> exceptions) {
        List<Plage> p = new ArrayList<>();
        for (DayOfWeek j : List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY)) p.add(plage(j, "08:00", "19:00", "13:00", "15:00"));
        p.add(ferme(SUNDAY));
        return new Horaires(p, exceptions);
    }

    @Test
    void journee_avec_pause() {
        Horaires b = boutique(List.of());
        assertThat(b.etat(true, le(5, "10:00"))).isEqualTo(new Horaires.Etat(true, "Ouvert · ferme à 13 h"));
        assertThat(b.etat(true, le(5, "14:00"))).isEqualTo(new Horaires.Etat(false, "Fermé · ouvre à 15 h"));
        assertThat(b.etat(true, le(5, "19:00"))).isEqualTo(new Horaires.Etat(false, "Fermé · ouvre demain à 8 h"));
        assertThat(b.etat(true, le(5, "07:59")).ouvert()).isFalse();
        assertThat(b.estOuvert(le(5, "08:00"))).isTrue();
    }

    @Test
    void dimanche_ferme_on_annonce_le_lundi() {
        assertThat(boutique(List.of()).etat(true, le(10, "20:00"))).isEqualTo(new Horaires.Etat(false, "Fermé · ouvre lundi à 8 h"));
    }

    @Test
    void plage_qui_passe_minuit_et_exception_du_lendemain() {
        // Restaurant : 18 h – 2 h le vendredi ; samedi 10 octobre fermé exceptionnellement.
        Horaires r = new Horaires(List.of(plage(FRIDAY, "18:00", "02:00"), ferme(SATURDAY)),
                List.of(new Horaires.Exception(LocalDate.of(2026, 10, 10), true, null, null, "Inventaire")));
        assertThat(r.etat(true, le(9, "23:00"))).isEqualTo(new Horaires.Etat(true, "Ouvert · ferme demain à 2 h"));
        // La nuit de vendredi continue malgré l'exception du samedi.
        assertThat(r.etat(true, le(10, "01:30"))).isEqualTo(new Horaires.Etat(true, "Ouvert · ferme à 2 h"));
        assertThat(r.estOuvert(le(10, "02:00"))).isFalse();
        assertThat(r.etat(true, le(10, "12:00"))).isEqualTo(new Horaires.Etat(false, "Fermé · ouvre vendredi à 18 h"));
    }

    @Test
    void exception_avec_des_heures() {
        Horaires b = boutique(List.of(new Horaires.Exception(LUNDI_5, false, h("10:00"), h("12:00"), "Tabaski")));
        assertThat(b.etat(true, le(5, "09:00"))).isEqualTo(new Horaires.Etat(false, "Fermé · ouvre à 10 h"));
        assertThat(b.etat(true, le(5, "11:00"))).isEqualTo(new Horaires.Etat(true, "Ouvert · ferme à 12 h"));
        assertThat(b.estOuvert(le(5, "16:00"))).isFalse();
    }

    @Test
    void ouvert_24h() {
        List<Plage> tous = new ArrayList<>();
        for (DayOfWeek j : DayOfWeek.values()) tous.add(h24(j));
        assertThat(new Horaires(tous, List.of()).etat(true, le(6, "03:00"))).isEqualTo(new Horaires.Etat(true, "Ouvert 24 h/24"));
        List<Plage> semaine = new ArrayList<>();
        for (DayOfWeek j : List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)) semaine.add(h24(j));
        assertThat(new Horaires(semaine, List.of()).etat(true, le(5, "10:00")))
                .isEqualTo(new Horaires.Etat(true, "Ouvert · ferme vendredi à minuit"));
        assertThat(new Horaires(List.of(plage(MONDAY, "08:00", "00:00")), List.of()).etat(true, le(5, "22:00")))
                .isEqualTo(new Horaires.Etat(true, "Ouvert · ferme à minuit"));
    }

    @Test
    void fermeture_manuelle_et_horaires_absents() {
        assertThat(boutique(List.of()).etat(false, le(5, "10:00"))).isEqualTo(new Horaires.Etat(false, "Fermé temporairement"));
        assertThat(new Horaires(List.of(), List.of()).etat(true, le(5, "10:00"))).isEqualTo(new Horaires.Etat(null, null));
    }

    @Test
    void resumes() {
        assertThat(Horaires.resume(plage(MONDAY, "08:30", "19:00", "13:00", "15:00"))).isEqualTo("8 h 30 – 19 h (pause 13 h – 15 h)");
        assertThat(Horaires.resume(h24(MONDAY))).isEqualTo("24 h/24");
        assertThat(Horaires.resume(ferme(MONDAY))).isEqualTo("Fermé");
    }

    @Test
    void validation() {
        assertThat(Horaires.erreurs(List.of(plage(MONDAY, "08:00", "19:00", "07:00", "09:00")))).containsExactly(
                "Lundi : la pause doit se situer entre l'ouverture et la fermeture");
        assertThat(Horaires.erreurs(List.of(plage(TUESDAY, "08:00", "08:00")))).hasSize(1);
        assertThat(Horaires.erreurs(List.of(new Plage(WEDNESDAY, true, false, h("08:00"), h("19:00"), h("13:00"), null)))).hasSize(1);
        assertThat(Horaires.erreurs(List.of(plage(MONDAY, "08:00", "19:00"), plage(MONDAY, "09:00", "18:00")))).containsExactly("Lundi : jour en double");
        assertThat(Horaires.erreurs(List.of(plage(FRIDAY, "18:00", "02:00", "23:00", "23:30")))).isEmpty();
        assertThat(Horaires.erreurs(new Horaires.Exception(LUNDI_5.minusDays(1), false, h("10:00"), h("09:00"), null), LUNDI_5)).hasSize(2);
    }
}

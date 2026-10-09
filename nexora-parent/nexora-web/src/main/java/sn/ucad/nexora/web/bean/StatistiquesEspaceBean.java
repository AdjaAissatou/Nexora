package sn.ucad.nexora.web.bean;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.ToLongFunction;
import sn.ucad.nexora.web.client.CatalogueApiClient;
import sn.ucad.nexora.web.dto.catalogue.StatistiquesDtos.Compteurs;
import sn.ucad.nexora.web.dto.catalogue.StatistiquesDtos.Semaine;
import sn.ucad.nexora.web.dto.catalogue.StatistiquesDtos.StatOffre;
import sn.ucad.nexora.web.dto.catalogue.StatistiquesDtos.StatistiquesEspace;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/**
 * Onglet « Statistiques » de Mon espace (docs/architecture-acteurs.md §26) : indicateurs de la période
 * comparés à la période précédente, évolution par semaine (vues, j'aime, favoris, contacts), contacts
 * par canal (Appeler, WhatsApp, Itinéraire, Partager) et tableau par offre.
 */
@Named
@ViewScoped
public class StatistiquesEspaceBean implements Serializable {

    /** Une tuile : valeur de la période, évolution, et la courbe des semaines. */
    public record Indicateur(String code, String libelle, String valeur, String aide, String evolution, String tendance,
                             String courbe) implements Serializable {}

    /** Une barre « contacts par canal ». */
    public record Canal(String libelle, long nombre, int largeur, String part) implements Serializable {}

    /** Une ligne du tableau par offre. */
    public record LigneOffre(StatOffre offre, String taux, double tauxValeur) implements Serializable {}

    private static final DateTimeFormatter JOUR = DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH);
    private static final DateTimeFormatter JOUR_ANNEE = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);

    @Inject
    private transient CatalogueApiClient api;

    @Inject
    private SessionBean session;

    @Inject
    private MonEspaceBean monEspace;

    private int semaines = 12;
    private StatistiquesEspace stats;
    private boolean charge;
    private String erreur;

    public StatistiquesEspace getStats() {
        if (!charge) charger();
        return stats;
    }

    private void charger() {
        charge = true;
        if (monEspace.getEspace() == null) return;
        try {
            stats = api.statistiques(session.getAccessToken(), monEspace.getEspace().id(), semaines);
            erreur = null;
        } catch (ApiException e) {
            stats = null;
            erreur = e.getMessage();
        }
    }

    public void periode(int duree) {
        semaines = duree;
        charger();
    }

    public List<Indicateur> getIndicateurs() {
        StatistiquesEspace s = getStats();
        if (s == null) return List.of();
        Compteurs p = s.periode();
        Compteurs a = s.precedente();
        List<Indicateur> liste = new ArrayList<>();
        liste.add(tuile("vues", "Vues", Compteurs::vues, "Fiches de l'espace et des offres, et cartes regardées dans Découvrir"));
        liste.add(tuile("jaime", "J'aime", Compteurs::jaime, "J'aime reçus dans Découvrir et sur vos fiches"));
        liste.add(tuile("favoris", "Favoris", Compteurs::favoris, "Offres et espace enregistrés par les visiteurs"));
        liste.add(tuile("contacts", "Contacts", Compteurs::contacts, "Clics sur Appeler, WhatsApp et Itinéraire"));
        double taux = taux(p.contacts(), p.vuesFiches());
        double tauxAvant = taux(a.contacts(), a.vuesFiches());
        String evolution = null;
        String tendance = "stable";
        if (a.vuesFiches() > 0 && p.vuesFiches() > 0) {
            double ecart = Math.round((taux - tauxAvant) * 10) / 10.0;
            tendance = ecart > 0 ? "hausse" : ecart < 0 ? "baisse" : "stable";
            evolution = ecart == 0 ? "stable" : (ecart > 0 ? "+" : "−") + pourcent(Math.abs(ecart)) + " pt";
        }
        liste.add(new Indicateur("taux", "Taux de contact", pourcent(taux) + " %",
                "Contacts pour 100 vues de fiche : la part des visiteurs qui passent à l'action", evolution, tendance,
                courbe(sem -> sem.vuesFiches() == 0 ? 0 : Math.round(1000.0 * sem.contacts() / sem.vuesFiches()))));
        return liste;
    }

    private Indicateur tuile(String code, String libelle, ToLongFunction<Compteurs> mesure, String aide) {
        long valeur = mesure.applyAsLong(stats.periode());
        long avant = mesure.applyAsLong(stats.precedente());
        String evolution;
        String tendance;
        if (avant == 0) {
            evolution = valeur == 0 ? null : "nouveau";
            tendance = valeur == 0 ? "stable" : "hausse";
        } else {
            long ecart = Math.round(100.0 * (valeur - avant) / avant);
            tendance = ecart > 0 ? "hausse" : ecart < 0 ? "baisse" : "stable";
            evolution = ecart == 0 ? "stable" : (ecart > 0 ? "+" : "−") + Math.abs(ecart) + " %";
        }
        return new Indicateur(code, libelle, nombre(valeur), aide, evolution, tendance, courbe(mesure));
    }

    /** Tracé SVG (viewBox 0 0 100 32) de la mesure semaine après semaine. */
    private String courbe(ToLongFunction<Compteurs> mesure) {
        List<Semaine> liste = stats.parSemaine();
        if (liste == null || liste.size() < 2) return "";
        long max = liste.stream().mapToLong(s -> mesure.applyAsLong(s.compteurs())).max().orElse(0);
        StringBuilder d = new StringBuilder();
        for (int i = 0; i < liste.size(); i++) {
            double x = 100.0 * i / (liste.size() - 1);
            double y = max == 0 ? 30 : 30 - 26.0 * mesure.applyAsLong(liste.get(i).compteurs()) / max;
            d.append(i == 0 ? "M" : " L").append(String.format(Locale.ROOT, "%.1f,%.1f", x, y));
        }
        return d.toString();
    }

    /** Données du graphique hebdomadaire, lues par statistiques.js. */
    public String getDonnees() {
        StatistiquesEspace s = getStats();
        if (s == null || s.parSemaine() == null) return "[]";
        StringBuilder json = new StringBuilder("[");
        LocalDate lundiCourant = s.parSemaine().isEmpty() ? null : s.parSemaine().get(s.parSemaine().size() - 1).debut();
        for (Semaine sem : s.parSemaine()) {
            Compteurs c = sem.compteurs();
            if (json.length() > 1) json.append(',');
            json.append("{\"semaine\":\"").append(JOUR.format(sem.debut()))
                    .append("\",\"titre\":\"").append(sem.debut().equals(lundiCourant)
                            ? "Semaine en cours (depuis le " + JOUR.format(sem.debut()) + ")"
                            : "Semaine du " + JOUR.format(sem.debut()) + " au " + JOUR.format(sem.debut().plusDays(6)))
                    .append("\",\"fiches\":").append(c.vuesFiches())
                    .append(",\"decouvrir\":").append(c.vuesDecouvrir())
                    .append(",\"jaime\":").append(c.jaime())
                    .append(",\"favoris\":").append(c.favoris())
                    .append(",\"contacts\":").append(c.contacts())
                    .append(",\"appels\":").append(c.appels())
                    .append(",\"whatsapp\":").append(c.whatsapp())
                    .append(",\"itineraires\":").append(c.itineraires())
                    .append('}');
        }
        return json.append(']').toString();
    }

    public List<Canal> getCanaux() {
        StatistiquesEspace s = getStats();
        if (s == null) return List.of();
        Compteurs p = s.periode();
        long[] valeurs = {p.whatsapp(), p.appels(), p.itineraires(), p.partages()};
        String[] libelles = {"WhatsApp", "Appels", "Itinéraire", "Partages"};
        long max = 0;
        long total = 0;
        for (long v : valeurs) {
            max = Math.max(max, v);
            total += v;
        }
        List<Canal> liste = new ArrayList<>();
        for (int i = 0; i < valeurs.length; i++) {
            liste.add(new Canal(libelles[i], valeurs[i], max == 0 ? 0 : (int) Math.round(100.0 * valeurs[i] / max),
                    total == 0 ? "" : Math.round(100.0 * valeurs[i] / total) + " %"));
        }
        return liste;
    }

    public long getTotalCanaux() {
        StatistiquesEspace s = getStats();
        return s == null ? 0 : s.periode().contacts() + s.periode().partages();
    }

    public List<LigneOffre> getOffres() {
        StatistiquesEspace s = getStats();
        if (s == null || s.offres() == null) return List.of();
        return s.offres().stream().map(o -> {
            double t = taux(o.compteurs().contacts(), o.compteurs().vuesFiches());
            return new LigneOffre(o, o.compteurs().vuesFiches() == 0 ? "—" : pourcent(t) + " %", t);
        }).toList();
    }

    /** « du 20 juillet au 9 octobre 2026 ». */
    public String getLibellePeriode() {
        StatistiquesEspace s = getStats();
        if (s == null) return "";
        return "du " + JOUR.format(s.debut()) + " à aujourd'hui (" + JOUR_ANNEE.format(LocalDate.now()) + ")";
    }

    public String getLibelleComparaison() {
        return switch (semaines) {
            case 4 -> "aux 4 semaines précédentes";
            case 26 -> "aux 6 mois précédents";
            default -> "aux 12 semaines précédentes";
        };
    }

    private static double taux(long contacts, long vues) {
        return vues == 0 ? 0 : 100.0 * contacts / vues;
    }

    private static String pourcent(double valeur) {
        DecimalFormat f = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.FRANCE));
        return f.format(valeur);
    }

    private static String nombre(long valeur) {
        DecimalFormatSymbols symboles = DecimalFormatSymbols.getInstance(Locale.FRANCE);
        symboles.setGroupingSeparator(' ');
        return new DecimalFormat("#,##0", symboles).format(valeur);
    }

    public int getSemaines() { return semaines; }
    public String getErreur() { return erreur; }
}

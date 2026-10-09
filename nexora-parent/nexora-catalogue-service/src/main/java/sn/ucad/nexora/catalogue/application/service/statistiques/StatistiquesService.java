package sn.ucad.nexora.catalogue.application.service.statistiques;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import sn.ucad.nexora.catalogue.application.dto.response.StatistiquesDtos.*;
import sn.ucad.nexora.catalogue.infrastructure.persistence.StatistiquesRepository;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;

/**
 * Statistiques détaillées d'un espace pour son propriétaire (docs/architecture-acteurs.md §26) : vues des
 * fiches et dans Découvrir, j'aime, favoris, clics (Appeler, WhatsApp, Itinéraire) et partages, semaine par
 * semaine, comparés à la période précédente, et détaillés par offre.
 */
@Service
public class StatistiquesService {

    public static final Set<Integer> DUREES = Set.of(4, 12, 26);
    private static final Set<String> CLICS = Set.of("APPEL", "WHATSAPP", "ITINERAIRE", "PARTAGE");
    private static final ZoneId DAKAR = ZoneId.of("Africa/Dakar");

    private final StatistiquesRepository repository;

    public StatistiquesService(StatistiquesRepository repository) {
        this.repository = repository;
    }

    public StatistiquesEspace espace(UUID compte, Long espace, int semaines) {
        if (!DUREES.contains(semaines)) throw new BusinessException("Période : 4, 12 ou 26 semaines");
        String nom = repository.espaceDuCompte(espace, compte)
                .orElseThrow(() -> new ResourceNotFoundException("Espace introuvable"));
        LocalDate lundi = LocalDate.now(DAKAR).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate debut = lundi.minusWeeks(semaines - 1L);
        LocalDate debutPrecedente = debut.minusWeeks(semaines);

        Map<LocalDate, Map<String, Long>> parSemaine = new HashMap<>();
        for (Object[] l : repository.parSemaine(espace, debutPrecedente)) {
            LocalDate semaine = l[0] instanceof java.sql.Date d ? d.toLocalDate() : (LocalDate) l[0];
            parSemaine.computeIfAbsent(semaine, k -> new HashMap<>()).merge((String) l[1], ((Number) l[2]).longValue(), Long::sum);
        }
        List<Semaine> semainesListe = new ArrayList<>();
        Map<String, Long> periode = new HashMap<>();
        Map<String, Long> precedente = new HashMap<>();
        for (LocalDate s = debutPrecedente; !s.isAfter(lundi); s = s.plusWeeks(1)) {
            Map<String, Long> valeurs = parSemaine.getOrDefault(s, Map.of());
            if (!s.isBefore(debut)) {
                valeurs.forEach((t, n) -> periode.merge(t, n, Long::sum));
                semainesListe.add(new Semaine(s, compteurs(valeurs)));
            }
        }
        // Période précédente de même durée, arrêtée au même moment de la semaine : la semaine en cours
        // est incomplète, la comparer à une semaine entière ferait croire à une baisse.
        LocalDateTime maintenant = LocalDateTime.now(DAKAR);
        for (Object[] l : repository.totaux(espace, debutPrecedente.atStartOfDay(), maintenant.minusWeeks(semaines))) {
            precedente.merge((String) l[0], ((Number) l[1]).longValue(), Long::sum);
        }

        Map<Long, Map<String, Long>> parOffre = new HashMap<>();
        for (Object[] l : repository.parOffre(espace, debut)) {
            parOffre.computeIfAbsent(((Number) l[0]).longValue(), k -> new HashMap<>())
                    .merge((String) l[1], ((Number) l[2]).longValue(), Long::sum);
        }
        List<StatOffre> offres = new ArrayList<>();
        for (Object[] l : repository.offres(espace)) {
            Long id = ((Number) l[0]).longValue();
            offres.add(new StatOffre(id, (String) l[1], (String) l[2], Boolean.TRUE.equals(l[3]),
                    compteurs(parOffre.getOrDefault(id, Map.of()))));
        }
        offres.sort(Comparator.comparingLong((StatOffre o) -> o.compteurs().vues()).reversed().thenComparing(StatOffre::id));
        return new StatistiquesEspace(espace, nom, semaines, debut, semainesListe, compteurs(periode), compteurs(precedente), offres);
    }

    /** Clic relayé par le web ; faux s'il est ignoré (type inconnu, cible introuvable, répétition). */
    public boolean enregistrer(Evenement e) {
        if (e == null || e.type() == null || !CLICS.contains(e.type()) || (e.idOffre() == null && e.idEspace() == null)
                || e.visiteur() == null || !e.visiteur().matches("[A-Za-z0-9-]{8,64}")) {
            return false;
        }
        return repository.espaceDe(e.idOffre(), e.idEspace())
                .map(espace -> repository.enregistrer(espace, e.idOffre(), e.type(), e.visiteur()))
                .orElse(false);
    }

    private static Compteurs compteurs(Map<String, Long> v) {
        return new Compteurs(n(v, "VUE"), n(v, "VUE_DECOUVRIR"), n(v, "JAIME"), n(v, "FAVORI"), n(v, "APPEL"),
                n(v, "WHATSAPP"), n(v, "ITINERAIRE"), n(v, "PARTAGE"));
    }

    private static long n(Map<String, Long> v, String type) {
        return v.getOrDefault(type, 0L);
    }
}

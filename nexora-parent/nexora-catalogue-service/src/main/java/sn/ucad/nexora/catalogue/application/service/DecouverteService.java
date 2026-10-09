package sn.ucad.nexora.catalogue.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import sn.ucad.nexora.catalogue.application.dto.response.DecouverteDtos.Carte;
import sn.ucad.nexora.catalogue.application.dto.response.DecouverteDtos.Caracteristique;
import sn.ucad.nexora.catalogue.application.dto.response.DecouverteDtos.Flux;
import sn.ucad.nexora.catalogue.application.dto.response.DecouverteDtos.Interet;
import sn.ucad.nexora.catalogue.application.dto.response.DecouverteDtos.Profil;
import sn.ucad.nexora.catalogue.application.dto.response.DecouverteDtos.Signal;
import sn.ucad.nexora.catalogue.infrastructure.persistence.DecouverteRepository;
import sn.ucad.nexora.catalogue.infrastructure.persistence.DecouverteRepository.Candidat;
import sn.ucad.nexora.catalogue.infrastructure.persistence.DecouverteRepository.EspaceCandidat;
import sn.ucad.nexora.catalogue.infrastructure.persistence.DecouverteRepository.ProfilBrut;

/**
 * Nexora Découvrir (§18) : le flux « une offre à la fois, autour de moi, adaptée à mes goûts ».
 *
 * <p>Chaque page mélange environ 70 % d'offres correspondant aux goûts appris (signaux du
 * visiteur), 20 % de tendances locales (nouveautés, succès du coin) et 10 % de découverte hors des
 * habitudes, plus une fiche de professionnel. Le score d'une offre combine l'intérêt du visiteur,
 * la proximité, la qualité des photos, la disponibilité, la popularité, la qualité du
 * professionnel, la nouveauté et, si le flux vient d'une recherche, son rang dans la recherche.
 */
@Service
public class DecouverteService {

    public static final Set<String> TYPES_SIGNAL = Set.of(
            "VUE", "VUE_LONGUE", "PASSE", "J_AIME", "ENREGISTRE", "PARTAGE", "DETAIL", "TAILLES", "CONTACT", "ACHAT");
    private static final Pattern VISITEUR = Pattern.compile("[A-Za-z0-9-]{8,64}");
    private static final Pattern PRESTATION = Pattern.compile(
            "pack|forfait|formule|d[ée]coration|r[ée]ception|mariage|bapt[êe]me|s[ée]ance|shooting|organisation|traiteur",
            Pattern.CASE_INSENSITIVE);
    private static final ZoneId DAKAR = ZoneId.of("Africa/Dakar");

    private final DecouverteRepository repository;
    private final Random hasard = new Random();

    public DecouverteService(DecouverteRepository repository) {
        this.repository = repository;
    }

    public static boolean visiteurValide(String visiteur) {
        return visiteur != null && VISITEUR.matcher(visiteur).matches();
    }

    /** Une offre candidate et ses notes. */
    private record Note(Candidat c, double interet, double score, double tendance, String section) {}

    /**
     * Page suivante du flux. {@code rechercheClassee} : offres d'une recherche, dans l'ordre de la
     * recherche (null hors recherche). {@code vus} / {@code vusEspaces} : déjà montrés au visiteur.
     */
    public Flux flux(String visiteur, String zone, BigDecimal lat, BigDecimal lng, double rayonKm,
                     List<Long> rechercheClassee, Collection<Long> vus, Collection<Long> vusEspaces, int taille) {
        return flux(visiteur, zone, lat, lng, rayonKm, rechercheClassee, vus, vusEspaces, taille, null);
    }

    /** {@code nature} : PRODUIT (onglet Boutique), SERVICE (onglet Services) ou null (tout). */
    public Flux flux(String visiteur, String zone, BigDecimal lat, BigDecimal lng, double rayonKm,
                     List<Long> rechercheClassee, Collection<Long> vus, Collection<Long> vusEspaces, int taille, String nature) {
        ProfilBrut profil = visiteurValide(visiteur) ? repository.profil(visiteur) : ProfilBrut.vide();
        Set<Long> dejaVus = new HashSet<>(vus);
        if (rechercheClassee == null) dejaVus.addAll(repository.passeesRecemment(visiteur));

        List<Candidat> candidats = repository.candidats(rechercheClassee, zone, lat, lng, rayonKm).stream()
                .filter(c -> !dejaVus.contains(c.idOffre()))
                .filter(c -> nature == null || ("SERVICE".equals(nature) ? c.service() : !c.service()))
                .toList();
        if (candidats.isEmpty()) return new Flux(List.of(), true);

        Map<Long, Integer> rangs = new HashMap<>();
        if (rechercheClassee != null) {
            for (int i = 0; i < rechercheClassee.size(); i++) rangs.putIfAbsent(rechercheClassee.get(i), i);
        }
        double popMax = candidats.stream().mapToDouble(DecouverteService::popularite).max().orElse(1);
        List<Note> notes = new ArrayList<>();
        for (Candidat c : candidats) {
            double interet = interet(profil, c);
            double proximite = c.distanceKm() == null ? 0.5 : Math.max(0, 1 - c.distanceKm() / Math.max(rayonKm, 0.1));
            double visuel = c.nombreImages() == 0 ? 0 : 0.6 + Math.min(c.nombreImages(), 4) * 0.1;
            double dispo = c.ouvertMaintenant() == null ? 0.6 : c.ouvertMaintenant() ? 1 : 0.3;
            double populaire = popMax <= 0 ? 0 : Math.log1p(popularite(c)) / Math.log1p(popMax);
            double qualite = (c.verifie() ? 0.4 : 0) + (c.certifie() ? 0.2 : 0)
                    + (c.note() != null && c.nombreAvis() != null && c.nombreAvis() > 0 ? c.note().doubleValue() / 5 * 0.4 : 0.2);
            double nouveaute = nouveaute(c.publication());
            double recherche = rangs.isEmpty() ? 0 : 1 - (double) rangs.getOrDefault(c.idOffre(), rangs.size()) / Math.max(1, rangs.size());
            double score = 3 * interet + 1.2 * proximite + visuel + 0.5 * dispo + populaire + qualite + nouveaute
                    + 1.5 * recherche + 0.6 * hasard.nextDouble();
            double tendance = 1.5 * nouveaute + 1.5 * populaire + 0.5 * visuel + 0.5 * qualite + 0.4 * hasard.nextDouble();
            notes.add(new Note(c, interet, score, tendance, null));
        }

        int nTendance = Math.max(1, Math.round(taille * 0.2f));
        int nDecouverte = Math.max(1, Math.round(taille * 0.1f));
        int nPertinent = Math.max(1, taille - nTendance - nDecouverte);

        Set<Long> pris = new HashSet<>();
        Deque<Note> pertinents = choisir(notes, Comparator.comparingDouble(Note::score).reversed(), nPertinent, pris, "PERTINENT");
        Deque<Note> tendances = choisir(notes, Comparator.comparingDouble(Note::tendance).reversed(), nTendance, pris, "TENDANCE");
        // Découverte : de préférence un rayon que le visiteur n'a pas encore exploré, au hasard
        List<Note> ailleurs = new ArrayList<>(notes.stream()
                .filter(n -> !pris.contains(n.c().idOffre()) && profil.racines().getOrDefault(n.c().idRacine(), 0.0) <= 0).toList());
        if (ailleurs.isEmpty()) ailleurs = new ArrayList<>(notes.stream().filter(n -> !pris.contains(n.c().idOffre())).toList());
        java.util.Collections.shuffle(ailleurs, hasard);
        Deque<Note> decouvertes = choisir(ailleurs, (a, b) -> 0, nDecouverte, pris, "DECOUVERTE");

        varier(pertinents, notes, pris);

        // Ordre : P P T P P D P T … ; jamais deux fois de suite le même professionnel si on peut l'éviter
        List<Note> ordre = new ArrayList<>();
        String[] motif = {"P", "P", "T", "P", "P", "D", "P", "T"};
        for (int i = 0; !(pertinents.isEmpty() && tendances.isEmpty() && decouvertes.isEmpty()); i++) {
            Deque<Note> file = switch (motif[i % motif.length]) {
                case "T" -> premiere(tendances, pertinents, decouvertes);
                case "D" -> premiere(decouvertes, pertinents, tendances);
                default -> premiere(pertinents, tendances, decouvertes);
            };
            ordre.add(file.poll());
        }
        espacer(ordre);

        List<Long> ids = ordre.stream().map(n -> n.c().idOffre()).toList();
        Map<Long, List<String>> medias = repository.medias(ids);
        Map<Long, List<Caracteristique>> caracteristiques = repository.caracteristiques(ids);
        boolean profilConnu = profil.nombreSignaux() >= 3;
        List<Carte> cartes = new ArrayList<>();
        for (Note n : ordre) {
            cartes.add(carte(n, medias.getOrDefault(n.c().idOffre(), List.of()),
                    caracteristiques.getOrDefault(n.c().idOffre(), List.of()), profilConnu, rechercheClassee != null));
        }

        // Une fiche de professionnel après la 4e carte (hors flux tiré d'une recherche)
        if (rechercheClassee == null && nature == null && cartes.size() >= 4) {
            espace(profil, zone, lat, lng, rayonKm, vusEspaces, cartes).ifPresent(e -> cartes.add(4, e));
        }
        return new Flux(cartes, ordre.size() < taille);
    }

    /** La première file non vide, dans l'ordre de préférence. */
    @SafeVarargs
    private static Deque<Note> premiere(Deque<Note>... files) {
        for (Deque<Note> f : files) if (!f.isEmpty()) return f;
        return files[0];
    }

    private static double popularite(Candidat c) {
        // Même pondération que offre.score_popularite (§23), plus les ventes
        return c.vues() + 0.5 * c.vuesDecouvrir() + 3.0 * c.jaime() + 5.0 * c.favoris() + 10.0 * c.ventes();
    }

    private static double nouveaute(LocalDateTime publication) {
        if (publication == null) return 0.2;
        double jours = Math.max(0, Duration.between(publication, LocalDateTime.now()).toHours() / 24.0);
        return Math.exp(-jours / 14);
    }

    /** Intérêt du visiteur pour une offre, entre −1 et 1 : rayon, catégorie parente, catégorie, espace. */
    private static double interet(ProfilBrut p, Candidat c) {
        return 0.35 * normal(p.racines(), c.idRacine()) + 0.25 * normal(p.parents(), c.idParent())
                + 0.30 * normal(p.categories(), c.idCategorie()) + 0.10 * normal(p.espaces(), c.idEspace());
    }

    private static double normal(Map<Long, Double> poids, Long id) {
        if (id == null || poids.isEmpty()) return 0;
        double max = poids.values().stream().mapToDouble(Math::abs).max().orElse(1);
        return max == 0 ? 0 : poids.getOrDefault(id, 0.0) / max;
    }

    /** Les meilleures selon l'ordre donné, sans plus de deux offres d'une même catégorie (assoupli si besoin). */
    private static Deque<Note> choisir(List<Note> notes, Comparator<Note> ordre, int combien, Set<Long> pris, String section) {
        List<Note> tries = new ArrayList<>(notes);
        tries.sort(ordre);
        Deque<Note> choix = new ArrayDeque<>();
        Map<Long, Integer> parCategorie = new HashMap<>();
        for (int passe = 0; passe < 2 && choix.size() < combien; passe++) {
            for (Note n : tries) {
                if (choix.size() >= combien) break;
                if (pris.contains(n.c().idOffre())) continue;
                if (passe == 0 && parCategorie.getOrDefault(n.c().idCategorie(), 0) >= 2) continue;
                choix.add(new Note(n.c(), n.interet(), n.score(), n.tendance(), section));
                pris.add(n.c().idOffre());
                parCategorie.merge(n.c().idCategorie(), 1, Integer::sum);
            }
        }
        return choix;
    }

    /**
     * Le flux mélange produits, services et prestations : une page sans service (ou sans produit)
     * remplace sa dernière offre « pertinente » par le meilleur service (ou produit) disponible.
     */
    private static void varier(Deque<Note> pertinents, List<Note> notes, Set<Long> pris) {
        for (boolean service : new boolean[] {true, false}) {
            boolean present = pertinents.stream().anyMatch(n -> n.c().service() == service);
            if (present || pertinents.size() < 3) continue;
            notes.stream().filter(n -> !pris.contains(n.c().idOffre()) && n.c().service() == service)
                    .max(Comparator.comparingDouble(Note::score))
                    .ifPresent(n -> {
                        Note retiree = pertinents.pollLast();
                        pris.remove(retiree.c().idOffre());
                        pertinents.addLast(new Note(n.c(), n.interet(), n.score(), n.tendance(), "PERTINENT"));
                        pris.add(n.c().idOffre());
                    });
        }
    }

    /** Échange deux voisines quand deux offres du même espace se suivent. */
    private static void espacer(List<Note> ordre) {
        for (int i = 1; i < ordre.size(); i++) {
            if (!ordre.get(i).c().idEspace().equals(ordre.get(i - 1).c().idEspace())) continue;
            for (int j = i + 1; j < ordre.size(); j++) {
                if (!ordre.get(j).c().idEspace().equals(ordre.get(i - 1).c().idEspace())) {
                    java.util.Collections.swap(ordre, i, j);
                    break;
                }
            }
        }
    }

    private Carte carte(Note n, List<String> medias, List<Caracteristique> caracteristiques, boolean profilConnu, boolean depuisRecherche) {
        Candidat c = n.c();
        String type = c.service() ? (PRESTATION.matcher(c.titre() + " " + c.categorie() + " " + c.rayon()).find()
                || "Événementiel".equals(c.rayon()) ? "PRESTATION" : "SERVICE") : "PRODUIT";
        return new Carte(type, n.section(), c.idOffre(), c.idEspace(), c.titre(), c.description(), c.prix(), c.ancienPrix(),
                medias, c.categorie(), c.rayon(), c.espaceNom(), c.espaceLogo(), c.verifie(), c.certifie(), c.note(),
                c.nombreAvis(), c.typeEspace(), c.commune(), c.quartier(), c.distanceKm(), c.ouvertMaintenant(),
                c.telephone(), accroche(c), raison(n, profilConnu, depuisRecherche), caracteristiques,
                c.reservation() || restaurant(c.typeEspace()), c.domicile(), 0, c.jaime(), c.populaire());
    }

    private static boolean restaurant(String typeEspace) {
        return typeEspace != null && typeEspace.toLowerCase(Locale.ROOT).startsWith("restaura");
    }

    /** L'accroche en haut de la carte, à la sénégalaise : « 🍽️ Ce soir à Plateau », « ✨ Nouveauté »… */
    private static String accroche(Candidat c) {
        String lieu = c.quartier() != null && !c.quartier().isBlank() ? c.quartier() : c.commune() != null ? c.commune() : "Dakar";
        if (c.ancienPrix() != null && c.prix() != null && c.ancienPrix().compareTo(c.prix()) > 0) {
            int pct = c.ancienPrix().subtract(c.prix()).multiply(BigDecimal.valueOf(100))
                    .divide(c.ancienPrix(), 0, RoundingMode.HALF_UP).intValue();
            return "🏷️ −" + pct + " % en ce moment";
        }
        if (c.publication() != null && Duration.between(c.publication(), LocalDateTime.now()).toDays() < 7) {
            return "✨ Nouveauté à " + lieu;
        }
        if (restaurant(c.typeEspace())) {
            int heure = ZonedDateTime.now(DAKAR).getHour();
            return heure >= 17 ? "🍽️ Ce soir à " + lieu : heure >= 11 && heure < 15 ? "🍽️ Ce midi à " + lieu : "🍽️ À goûter à " + lieu;
        }
        String texte = (c.titre() + " " + c.categorie()).toLowerCase(Locale.ROOT);
        if (texte.contains("mariage")) return "💍 Mariage";
        if ("Événementiel".equals(c.rayon())) return "🎉 Événement";
        if (c.populaire()) return "🔥 Populaire à " + lieu;
        if (c.favoris() + c.jaime() + c.ventes() > 0 || c.vues() > 50) return "🔥 Ça bouge à " + lieu;
        return "📍 " + lieu;
    }

    /** « Pourquoi cette carte ? », affiché au visiteur. */
    private static String raison(Note n, boolean profilConnu, boolean depuisRecherche) {
        Candidat c = n.c();
        if (depuisRecherche) return "D'après votre recherche";
        return switch (n.section()) {
            case "TENDANCE" -> "Tendance à " + (c.commune() != null ? c.commune() : "Dakar");
            case "DECOUVERTE" -> "Pour changer : à découvrir";
            default -> {
                if (profilConnu && n.interet() > 0.25) yield "Parce que vous aimez « " + c.rayon() + " »";
                if (c.distanceKm() != null) yield "Près de vous";
                yield profilConnu ? "Sélectionné pour vous" : "Les incontournables du coin";
            }
        };
    }

    /** Une fiche de professionnel que le visiteur n'a pas encore vue, choisie selon ses goûts. */
    private java.util.Optional<Carte> espace(ProfilBrut profil, String zone, BigDecimal lat, BigDecimal lng, double rayonKm,
                                             Collection<Long> vusEspaces, List<Carte> cartes) {
        Set<Long> deja = new HashSet<>(vusEspaces);
        cartes.forEach(c -> deja.add(c.idEspace()));
        return repository.espaces(zone, lat, lng, rayonKm).stream()
                .filter(e -> !deja.contains(e.idEspace()))
                .max(Comparator.comparingDouble(e -> 2 * (0.6 * normal(profil.racines(), e.idRacine()) + 0.4 * normal(profil.espaces(), e.idEspace()))
                        + (e.verifie() ? 0.5 : 0) + (e.note() != null ? e.note().doubleValue() / 10 : 0)
                        + (e.distanceKm() == null ? 0 : Math.max(0, 1 - e.distanceKm() / Math.max(rayonKm, 0.1)))
                        + 0.8 * hasard.nextDouble()))
                .map(e -> carteEspace(e, profil, repository.mediasEspace(e.idEspace())));
    }

    private static Carte carteEspace(EspaceCandidat e, ProfilBrut profil, List<String> photosOffres) {
        List<String> medias = new ArrayList<>();
        if (e.couverture() != null && !e.couverture().isBlank()) medias.add(e.couverture());
        medias.addAll(photosOffres); // la vitrine : photos de ses articles
        String lieu = e.quartier() != null && !e.quartier().isBlank() ? e.quartier() : e.commune();
        String raison = normal(profil.racines(), e.idRacine()) > 0.25 ? "Parce que vous aimez « " + e.rayon() + " »" : "Un professionnel du coin";
        return new Carte("ESPACE", "PERTINENT", null, e.idEspace(), e.nom(), e.slogan(), null, null, medias, e.typeEspace(), e.rayon(),
                e.nom(), e.logo(), e.verifie(), e.certifie(), e.note(), e.nombreAvis(), e.typeEspace(), e.commune(), e.quartier(),
                e.distanceKm(), e.ouvertMaintenant(), e.telephone(), "🏪 À découvrir" + (lieu != null ? " à " + lieu : ""), raison,
                List.of(), false, false, e.nombreOffres(), e.jaime(), false);
    }

    public void signal(Signal s) {
        if (!visiteurValide(s.visiteur()) || !TYPES_SIGNAL.contains(s.type()) || (s.idOffre() == null && s.idEspace() == null)) {
            throw new IllegalArgumentException("Signal invalide");
        }
        Integer duree = s.dureeMs() == null ? null : Math.max(0, Math.min(s.dureeMs(), 600_000));
        repository.enregistrer(new Signal(s.visiteur(), s.idOffre(), s.idEspace(), s.type(), duree));
    }

    public void retirerJAime(String visiteur, Long idOffre, Long idEspace) {
        if (visiteurValide(visiteur)) repository.retirerJAime(visiteur, idOffre, idEspace);
    }

    /** Ce que Nexora a compris : les rayons et catégories les plus appréciés, sur 1. */
    public Profil profil(String visiteur) {
        if (!visiteurValide(visiteur)) return new Profil(List.of(), 0);
        ProfilBrut p = repository.profil(visiteur);
        Map<String, Double> parNom = new HashMap<>();
        p.racines().forEach((id, poids) -> parNom.merge(p.noms().getOrDefault(id, "?"), poids, Math::max));
        p.categories().forEach((id, poids) -> parNom.merge(p.noms().getOrDefault(id, "?"), poids * 0.8, Math::max));
        double max = parNom.values().stream().mapToDouble(d -> d).max().orElse(1);
        List<Interet> interets = parNom.entrySet().stream().filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed()).limit(8)
                .map(e -> new Interet(e.getKey(), Math.round(e.getValue() / max * 100) / 100.0)).toList();
        return new Profil(interets, p.nombreSignaux());
    }

    public int oublier(String visiteur) {
        return visiteurValide(visiteur) ? repository.oublier(visiteur) : 0;
    }

    public int fusionner(String ancien, String nouveau) {
        if (!visiteurValide(ancien) || !visiteurValide(nouveau) || ancien.equals(nouveau)) return 0;
        return repository.fusionner(ancien, nouveau);
    }
}

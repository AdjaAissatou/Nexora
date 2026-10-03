package sn.ucad.nexora.catalogue.application.service.vision;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import sn.ucad.nexora.catalogue.application.dto.response.OffreSummaryResponse;
import sn.ucad.nexora.catalogue.application.service.RechercherOffresService;
import sn.ucad.nexora.catalogue.infrastructure.persistence.vision.ImageVecteurRepository;
import sn.ucad.nexora.catalogue.infrastructure.persistence.vision.ImageVecteurRepository.Candidat;
import sn.ucad.nexora.catalogue.infrastructure.vision.ModeleVision;
import sn.ucad.nexora.catalogue.infrastructure.vision.SourceImages;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;

/**
 * Recherche par photo et articles similaires (docs/architecture-acteurs.md §11).
 *
 * <p>Chaque image d'offre reçoit une empreinte visuelle (vecteur du modèle {@link ModeleVision}),
 * calculée au démarrage en arrière-plan puis, au fil de l'eau, avant chaque recherche pour les
 * nouvelles images. Une recherche compare l'empreinte de la photo reçue à celles des offres
 * visibles (cosinus) ; une offre à plusieurs images garde sa meilleure ressemblance.
 */
@Service
public class RechercheVisuelleService {

    /**
     * Mesurés sur les photos de démonstration : un même article photographié autrement dépasse 0,37
     * (recherche : on montre large) ; sur une fiche, 0,55 écarte les voisins seulement « du même rayon ».
     */
    static final double SEUIL_RECHERCHE = 0.35;
    static final double SEUIL_SIMILAIRES = 0.55;
    static final int LIMITE_MAX = 48;
    /** « Vous pourriez aussi aimer » : assez proche pour être du même rayon, sans être le même article. */
    static final double SEUIL_SUGGESTIONS = 0.30;
    /** Au plus deux suggestions par espace, pour varier les boutiques. */
    private static final int SUGGESTIONS_PAR_ESPACE = 2;
    /** Nouvelles images indexées avant une recherche (le gros du travail se fait au démarrage). */
    private static final int INDEXATION_PAR_RECHERCHE = 40;
    private static final Logger LOG = LoggerFactory.getLogger(RechercheVisuelleService.class);

    public record Resultat(OffreSummaryResponse offre, double ressemblance) {}

    public record Reponse(List<Resultat> resultats, long imagesIndexees, long imagesEnAttente) {}

    private final ModeleVision modele;
    private final SourceImages sources;
    private final ImageVecteurRepository vecteurs;
    private final RechercherOffresService offres;
    private final ReentrantLock indexation = new ReentrantLock();
    /** Message donné quand la base n'a pas encore la table des empreintes. */
    static final String BASE_A_METTRE_A_JOUR = "La recherche par photo n'est pas encore installée sur cette base : "
            + "lancez database/mise_a_jour.sql, puis relancez catalogue-service.";
    private volatile boolean tableVerifiee;

    public RechercheVisuelleService(ModeleVision modele, SourceImages sources, ImageVecteurRepository vecteurs,
                                    RechercherOffresService offres) {
        this.modele = modele;
        this.sources = sources;
        this.vecteurs = vecteurs;
        this.offres = offres;
    }

    /** Indexe toutes les images au démarrage, sans retarder le service. */
    @EventListener(ApplicationReadyEvent.class)
    public void indexerAuDemarrage() {
        Thread t = new Thread(() -> {
            if (!basePrete()) {
                LOG.warn("Recherche par photo désactivée : la table image_vecteur manque. {}", BASE_A_METTRE_A_JOUR);
                return;
            }
            if (!modele.pret()) return;
            indexation.lock();
            try {
                int n = indexer(Integer.MAX_VALUE);
                LOG.info("Recherche par photo : {} image(s) indexée(s) au démarrage", n);
            } finally {
                indexation.unlock();
            }
        }, "indexation-images");
        t.setDaemon(true);
        t.start();
    }

    /** Calcule les empreintes manquantes ; renvoie le nombre d'images traitées. */
    int indexer(int max) {
        int traitees = 0;
        while (traitees < max) {
            List<String> urls = vecteurs.aIndexer(ModeleVision.NOM, Math.min(100, max - traitees));
            if (urls.isEmpty()) break;
            for (String url : urls) {
                indexerUne(url);
                traitees++;
            }
        }
        return traitees;
    }

    private void indexerUne(String url) {
        try {
            vecteurs.enregistrer(url, ModeleVision.NOM, modele.vecteur(sources.lire(url)), null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOG.debug("Image non indexée {} : {}", url, e.getMessage());
            vecteurs.enregistrer(url, ModeleVision.NOM, null, e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        }
    }

    /** Vrai si la table des empreintes existe (vérifié jusqu'à ce qu'elle apparaisse, puis mémorisé). */
    private boolean basePrete() {
        if (tableVerifiee) return true;
        try {
            tableVerifiee = vecteurs.tablePresente();
        } catch (Exception e) {
            LOG.warn("Vérification de la table image_vecteur impossible : {}", e.getMessage());
        }
        return tableVerifiee;
    }

    private void exigerModele() {
        if (!basePrete()) throw new BusinessException(BASE_A_METTRE_A_JOUR);
        if (!modele.pret()) {
            throw new BusinessException("La recherche par photo est momentanément indisponible"
                    + (modele.getIndisponibilite() == null ? "" : " (" + modele.getIndisponibilite() + ")"));
        }
    }

    public Reponse rechercher(byte[] photo, Integer limite) {
        if (photo == null || photo.length == 0) throw new BusinessException("Choisissez une photo");
        if (photo.length > SourceImages.TAILLE_MAX) throw new BusinessException("Photo trop lourde (10 Mo au plus)");
        BufferedImage image;
        try {
            image = SourceImages.decoder(photo);
        } catch (IOException e) {
            throw new BusinessException("Format de photo non pris en charge : utilisez une photo JPG, PNG ou WebP");
        }
        exigerModele();
        // Les images ajoutées depuis le démarrage, si l'indexation de démarrage est terminée.
        if (indexation.tryLock()) {
            try {
                indexer(INDEXATION_PAR_RECHERCHE);
            } finally {
                indexation.unlock();
            }
        }
        float[] q;
        try {
            q = modele.vecteur(image);
        } catch (Exception e) {
            throw new BusinessException("Cette photo n'a pas pu être analysée");
        }
        List<Resultat> resultats = classer(q, null, SEUIL_RECHERCHE, borne(limite, 24));
        long[] c = vecteurs.compteurs(ModeleVision.NOM);
        return new Reponse(resultats, c[0], c[1]);
    }

    /** Offres visuellement proches de celle-ci, dans d'autres espaces. */
    public List<Resultat> similaires(long offreId, Integer limite) {
        Object[] image = vecteurs.imageOffre(offreId).orElseThrow(() -> new ResourceNotFoundException("Offre introuvable ou sans image"));
        if (!basePrete() || !modele.pret()) return List.of();
        String url = (String) image[0];
        long espace = ((Number) image[1]).longValue();
        float[] q = vecteurs.vecteur(url, ModeleVision.NOM).orElseGet(() -> {
            indexerUne(url);
            return vecteurs.vecteur(url, ModeleVision.NOM).orElse(null);
        });
        if (q == null) return List.of();
        return classer(q, espace, SEUIL_SIMILAIRES, borne(limite, 8));
    }

    /**
     * « Vous pourriez aussi aimer » : des offres proches de celles-ci (l'offre consultée, ou l'historique
     * d'un visiteur), mais pas le même article vendu ailleurs (déjà dans « Le même genre d'article »).
     * Au plus deux par espace ; complété par des offres récentes des mêmes catégories si besoin.
     */
    public List<Resultat> suggestions(List<Long> depart, Integer limite) {
        int n = borne(limite, 8);
        java.util.Set<Long> graines = new java.util.LinkedHashSet<>(depart == null ? List.of()
                : depart.stream().filter(java.util.Objects::nonNull).limit(10).toList());
        if (graines.isEmpty()) return List.of();
        // Exclus : les offres de départ et « le même article vendu ailleurs » (déjà dans l'autre rubrique).
        java.util.Set<Long> exclus = new java.util.HashSet<>(graines);
        Map<Long, Double> meilleure = new HashMap<>();
        Map<Long, float[]> image = new HashMap<>();
        if (basePrete() && modele.pret()) {
            List<Candidat> sources = vecteurs.candidatsDe(graines, ModeleVision.NOM);
            for (Candidat c : vecteurs.candidatsVisibles(ModeleVision.NOM)) {
                if (graines.contains(c.offreId())) continue;
                double s = -1;
                for (Candidat g : sources) {
                    double x = cosinus(g.vecteur(), c.vecteur());
                    if (x >= SEUIL_SIMILAIRES && g.espaceId() != c.espaceId()) exclus.add(c.offreId());
                    s = Math.max(s, x);
                }
                if (s >= SEUIL_SUGGESTIONS && s > meilleure.getOrDefault(c.offreId(), -1.0)) {
                    meilleure.put(c.offreId(), s);
                    image.put(c.offreId(), c.vecteur());
                }
            }
        }
        meilleure.keySet().removeAll(exclus);
        List<Map.Entry<Long, Double>> tries = new ArrayList<>(meilleure.entrySet());
        tries.sort(Map.Entry.<Long, Double>comparingByValue(Comparator.reverseOrder()));
        Map<Long, OffreSummaryResponse> resumes = new HashMap<>();
        offres.resumesVisibles(tries.stream().limit(LIMITE_MAX).map(Map.Entry::getKey).toList()).forEach(o -> resumes.put(o.getId(), o));

        List<Resultat> r = new ArrayList<>();
        List<float[]> retenues = new ArrayList<>();
        Map<Long, Integer> parEspace = new HashMap<>();
        // Premier passage : deux par espace au plus ; second : on complète sans cette limite.
        for (int passage = 0; passage < 2 && r.size() < n; passage++) {
            for (Map.Entry<Long, Double> e : tries) {
                if (r.size() == n) break;
                OffreSummaryResponse o = resumes.get(e.getKey());
                if (o == null || r.stream().anyMatch(x -> x.offre().getId().equals(o.getId()))) continue;
                if (passage == 0 && parEspace.getOrDefault(o.getEspaceId(), 0) >= SUGGESTIONS_PAR_ESPACE) continue;
                float[] v = image.get(e.getKey());
                // Le même produit dans deux boutiques (photo quasi identique) n'est proposé qu'une fois.
                if (retenues.stream().anyMatch(w -> cosinus(v, w) >= 0.95)) continue;
                parEspace.merge(o.getEspaceId(), 1, Integer::sum);
                retenues.add(v);
                r.add(new Resultat(o, Math.round(e.getValue() * 1000) / 1000.0));
            }
        }
        // Pas assez de photos proches : les offres récentes des mêmes catégories, puis du même espace.
        java.util.Set<Long> deja = new java.util.HashSet<>(exclus);
        r.forEach(x -> deja.add(x.offre().getId()));
        List<OffreSummaryResponse> complements = new ArrayList<>();
        for (Long categorie : vecteurs.categories(graines)) complements.addAll(offres.recentesDeCategorie(categorie, n + deja.size()));
        for (Long espace : vecteurs.espaces(graines)) complements.addAll(offres.recentesDeEspace(espace, n + deja.size()));
        for (OffreSummaryResponse o : complements) {
            if (r.size() == n) break;
            if (deja.add(o.getId())) r.add(new Resultat(o, 0));
        }
        return r;
    }

    private List<Resultat> classer(float[] q, Long espaceExclu, double seuil, int limite) {
        Map<Long, Double> meilleure = new HashMap<>();
        for (Candidat c : vecteurs.candidatsVisibles(ModeleVision.NOM)) {
            if (espaceExclu != null && c.espaceId() == espaceExclu) continue;
            double s = cosinus(q, c.vecteur());
            if (s >= seuil) meilleure.merge(c.offreId(), s, Math::max);
        }
        List<Map.Entry<Long, Double>> tries = new ArrayList<>(meilleure.entrySet());
        tries.sort(Map.Entry.<Long, Double>comparingByValue(Comparator.reverseOrder()));
        if (tries.size() > limite) tries = tries.subList(0, limite);
        Map<Long, OffreSummaryResponse> resumes = new HashMap<>();
        offres.resumesVisibles(tries.stream().map(Map.Entry::getKey).toList()).forEach(o -> resumes.put(o.getId(), o));
        List<Resultat> r = new ArrayList<>();
        for (Map.Entry<Long, Double> e : tries) {
            OffreSummaryResponse o = resumes.get(e.getKey());
            if (o != null) r.add(new Resultat(o, Math.round(e.getValue() * 1000) / 1000.0));
        }
        return r;
    }

    static double cosinus(float[] a, float[] b) {
        double s = 0;
        for (int i = 0; i < Math.min(a.length, b.length); i++) s += a[i] * b[i];
        return s;
    }

    private static int borne(Integer limite, int defaut) {
        return limite == null || limite < 1 ? defaut : Math.min(limite, LIMITE_MAX);
    }
}

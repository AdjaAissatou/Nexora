package sn.ucad.nexora.catalogue.application.service.admin;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.catalogue.application.dto.response.admin.CatalogueAdminDtos.*;
import sn.ucad.nexora.catalogue.infrastructure.persistence.admin.CatalogueAdminRepository;
import sn.ucad.nexora.common.audit.JournalActions;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;

/**
 * Gestion du catalogue (docs/architecture-acteurs.md §9.11) : catégories, types d'offre, attributs et
 * leurs valeurs, liens des catégories racines aux types d'espace. Les permissions (GERER_CATEGORIES,
 * GERER_TYPES_OFFRES, GERER_ATTRIBUTS) sont vérifiées par SecurityConfig.
 *
 * Règles : on crée, on modifie, on désactive (motif) ; on ne supprime (motif) que ce qui n'a jamais
 * servi. Désactiver retire l'élément du parcours de création d'offre ; les offres existantes restent
 * intactes et visibles. Chaque action est journalisée (module CATALOGUE).
 */
@Service
public class CatalogueAdminService {

    private static final String MODULE = "CATALOGUE";
    private static final Set<String> TYPES_CHAMP = Set.of("TEXTE", "NOMBRE", "DATE", "BOOLEAN", "LISTE", "MULTI_LISTE");
    private static final Set<String> PRINCIPALES = Set.of("PRODUIT", "SERVICE");
    private static final int RESULTATS_MAX = 40;

    private final CatalogueAdminRepository repo;
    private final JournalActions journal;

    public CatalogueAdminService(CatalogueAdminRepository repo, JournalActions journal) {
        this.repo = repo;
        this.journal = journal;
    }

    // ------------------------------------------------------------------ lecture

    @Transactional(readOnly = true)
    public List<Noeud> racines() {
        return repo.enfants(null);
    }

    @Transactional(readOnly = true)
    public FicheCategorie fiche(Long id) {
        Noeud c = categorie(id);
        List<Etape> chemin = repo.chemin(id);
        boolean parentsActifs = chemin.stream().limit(Math.max(0, chemin.size() - 1)).allMatch(Etape::actif);
        return new FicheCategorie(c, chemin, parentsActifs, repo.enfants(id), repo.types(id), repo.attributs(id),
                c.parentId() == null ? repo.typesEspace(id) : List.of());
    }

    @Transactional(readOnly = true)
    public List<Resultat> rechercher(String texte) {
        String t = texte == null ? "" : texte.trim();
        if (t.length() < 2) throw new BusinessException("Tapez au moins deux caractères");
        return repo.rechercher(t, RESULTATS_MAX);
    }

    // ------------------------------------------------------------------ catégories

    @Transactional
    public FicheCategorie creerCategorie(UUID auteur, CategorieRequest r) {
        String nom = obligatoire(r.nom(), "Le nom", 120);
        if (r.parentId() != null) categorie(r.parentId());
        exigerNomLibreParmiSoeurs(r.parentId(), nom, null);
        Long id = repo.inserer("""
                INSERT INTO categorie (id_categorie_parent, nom, description, icone, couleur, ordre_affichage, actif, date_creation)
                VALUES (CAST(CAST(:parent AS TEXT) AS BIGINT), :nom, :description, :icone, :couleur, :ordre, TRUE, NOW())
                RETURNING id_categorie
                """, p("parent", texte(r.parentId()), "nom", nom, "description", facultatif(r.description(), 2000),
                "icone", facultatif(r.icone(), 150), "couleur", couleur(r.couleur()), "ordre", r.ordre() == null ? 0 : r.ordre()));
        journal.enregistrer(auteur, MODULE, "CREER_CATEGORIE", "categorie", id, "Catégorie « " + chemin(id) + " » créée");
        return fiche(id);
    }

    @Transactional
    public FicheCategorie modifierCategorie(UUID auteur, Long id, CategorieRequest r) {
        Noeud avant = categorie(id);
        String nom = obligatoire(r.nom(), "Le nom", 120);
        exigerNomLibreParmiSoeurs(avant.parentId(), nom, id);
        repo.executer("""
                UPDATE categorie SET nom = :nom, description = :description, icone = :icone, couleur = :couleur,
                    ordre_affichage = :ordre WHERE id_categorie = :id
                """, p("nom", nom, "description", facultatif(r.description(), 2000), "icone", facultatif(r.icone(), 150),
                "couleur", couleur(r.couleur()), "ordre", r.ordre() == null ? avant.ordre() : r.ordre(), "id", id));
        journal.enregistrer(auteur, MODULE, "MODIFIER_CATEGORIE", "categorie", id,
                "Catégorie « " + avant.nom() + " » modifiée" + (avant.nom().equals(nom) ? "" : " : renommée « " + nom + " »"));
        return fiche(id);
    }

    @Transactional
    public FicheCategorie activerCategorie(UUID auteur, Long id, boolean actif, String motif) {
        exigerMotif(motif);
        Noeud c = categorie(id);
        if (c.actif() == actif) throw new BusinessException(actif ? "Cette catégorie est déjà active" : "Cette catégorie est déjà désactivée");
        repo.executer("UPDATE categorie SET actif = :a WHERE id_categorie = :id", p("a", actif, "id", id));
        journal.enregistrer(auteur, MODULE, actif ? "ACTIVER_CATEGORIE" : "DESACTIVER_CATEGORIE", "categorie", id,
                "Catégorie « " + chemin(id) + " » " + (actif ? "réactivée" : "désactivée") + " : " + motif.trim());
        return fiche(id);
    }

    /** Supprime une catégorie qui n'a jamais servi. Renvoie la fiche du parent (null pour une racine). */
    @Transactional
    public FicheCategorie supprimerCategorie(UUID auteur, Long id, String motif) {
        exigerMotif(motif);
        Noeud c = categorie(id);
        if (c.sousCategories() > 0 || c.typesOffre() > 0 || c.attributs() > 0 || c.offres() > 0
                || repo.compter("SELECT COUNT(*) FROM offre WHERE id_categorie = :id", id) > 0) {
            throw new BusinessException("Cette catégorie contient des sous-catégories, des types d'offre, des attributs ou des offres : "
                    + "désactivez-la plutôt");
        }
        String libelle = chemin(id);
        repo.executer("DELETE FROM categorie WHERE id_categorie = :id", p("id", id));
        journal.enregistrer(auteur, MODULE, "SUPPRIMER_CATEGORIE", "categorie", id, "Catégorie « " + libelle + " » supprimée : " + motif.trim());
        return c.parentId() == null ? null : fiche(c.parentId());
    }

    /** Lie ou délie une catégorie racine et un type d'espace : ce qu'un espace de ce type peut proposer. */
    @Transactional
    public FicheCategorie lierTypeEspace(UUID auteur, Long id, Long typeEspaceId, boolean lier) {
        Noeud c = categorie(id);
        if (c.parentId() != null) throw new BusinessException("Seules les catégories racines se lient aux types d'espace");
        String typeEspace = repo.ligne("SELECT nom, id_type_espace FROM type_espace WHERE id_type_espace = :id", typeEspaceId)
                .map(l -> (String) l[0]).orElseThrow(() -> new ResourceNotFoundException("Type d'espace introuvable"));
        int n = lier
                ? repo.executer("""
                        INSERT INTO categorie_type_espace (id_categorie, id_type_espace, principal, date_creation)
                        VALUES (:c, :t, FALSE, NOW()) ON CONFLICT DO NOTHING""", p("c", id, "t", typeEspaceId))
                : repo.executer("DELETE FROM categorie_type_espace WHERE id_categorie = :c AND id_type_espace = :t", p("c", id, "t", typeEspaceId));
        if (n == 0) throw new BusinessException(lier ? "Ce lien existe déjà" : "Ce lien n'existe pas");
        journal.enregistrer(auteur, MODULE, lier ? "LIER_TYPE_ESPACE" : "DELIER_TYPE_ESPACE", "categorie", id,
                "« " + c.nom() + " » " + (lier ? "proposée aux" : "retirée des") + " espaces de type " + typeEspace);
        return fiche(id);
    }

    // ------------------------------------------------------------------ types d'offre

    @Transactional
    public FicheCategorie creerType(UUID auteur, Long categorieId, TypeOffreRequest r) {
        categorie(categorieId);
        String libelle = obligatoire(r.libelle(), "Le libellé", 120);
        String principale = principale(r.principale());
        exigerLibre("SELECT COUNT(*) FROM type_offre WHERE id_categorie = :c AND LOWER(libelle) = LOWER(:l)",
                p("c", categorieId, "l", libelle), "Un type d'offre porte déjà ce libellé dans cette catégorie");
        Long id = repo.inserer("""
                INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
                VALUES (:c, :l, :d, CAST(:p AS type_offre_principale), TRUE) RETURNING id_type_offre
                """, p("c", categorieId, "l", libelle, "d", facultatif(r.description(), 2000), "p", principale));
        journal.enregistrer(auteur, MODULE, "CREER_TYPE_OFFRE", "type_offre", id,
                "Type d'offre « " + libelle + " » créé dans « " + chemin(categorieId) + " »");
        return fiche(categorieId);
    }

    @Transactional
    public FicheCategorie modifierType(UUID auteur, Long id, TypeOffreRequest r) {
        Object[] t = type(id);
        Long categorieId = ((Number) t[0]).longValue();
        String libelle = obligatoire(r.libelle(), "Le libellé", 120);
        exigerLibre("SELECT COUNT(*) FROM type_offre WHERE id_categorie = :c AND LOWER(libelle) = LOWER(:l) AND id_type_offre <> :id",
                p("c", categorieId, "l", libelle, "id", id), "Un type d'offre porte déjà ce libellé dans cette catégorie");
        repo.executer("""
                UPDATE type_offre SET libelle = :l, description = :d, principale = CAST(:p AS type_offre_principale)
                WHERE id_type_offre = :id
                """, p("l", libelle, "d", facultatif(r.description(), 2000), "p", principale(r.principale()), "id", id));
        journal.enregistrer(auteur, MODULE, "MODIFIER_TYPE_OFFRE", "type_offre", id,
                "Type d'offre « " + t[1] + " » modifié" + (t[1].equals(libelle) ? "" : " : renommé « " + libelle + " »"));
        return fiche(categorieId);
    }

    @Transactional
    public FicheCategorie activerType(UUID auteur, Long id, boolean actif, String motif) {
        exigerMotif(motif);
        Object[] t = type(id);
        if (Boolean.TRUE.equals(t[2]) == actif) throw new BusinessException(actif ? "Ce type d'offre est déjà actif" : "Ce type d'offre est déjà désactivé");
        repo.executer("UPDATE type_offre SET actif = :a WHERE id_type_offre = :id", p("a", actif, "id", id));
        journal.enregistrer(auteur, MODULE, actif ? "ACTIVER_TYPE_OFFRE" : "DESACTIVER_TYPE_OFFRE", "type_offre", id,
                "Type d'offre « " + t[1] + " » " + (actif ? "réactivé" : "désactivé") + " : " + motif.trim());
        return fiche(((Number) t[0]).longValue());
    }

    @Transactional
    public FicheCategorie supprimerType(UUID auteur, Long id, String motif) {
        exigerMotif(motif);
        Object[] t = type(id);
        if (repo.compter("SELECT COUNT(*) FROM offre WHERE id_type_offre = :id", id) > 0) {
            throw new BusinessException("Des offres utilisent ce type : désactivez-le plutôt");
        }
        repo.executer("DELETE FROM type_offre WHERE id_type_offre = :id", p("id", id));
        journal.enregistrer(auteur, MODULE, "SUPPRIMER_TYPE_OFFRE", "type_offre", id, "Type d'offre « " + t[1] + " » supprimé : " + motif.trim());
        return fiche(((Number) t[0]).longValue());
    }

    // ------------------------------------------------------------------ attributs

    @Transactional
    public FicheCategorie creerAttribut(UUID auteur, Long categorieId, AttributRequest r) {
        categorie(categorieId);
        String nom = obligatoire(r.nom(), "Le nom", 150);
        String typeChamp = typeChamp(r.typeChamp());
        exigerLibre("SELECT COUNT(*) FROM attribut WHERE id_categorie = :c AND LOWER(nom) = LOWER(:n)",
                p("c", categorieId, "n", nom), "Un attribut porte déjà ce nom dans cette catégorie");
        Long id = repo.inserer("""
                INSERT INTO attribut (id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, ordre_affichage, unite, aide, actif, date_creation)
                VALUES (:c, :n, :code, CAST(:t AS type_champ), :o, :f, TRUE, :ordre, :u, :aide, TRUE, NOW()) RETURNING id_attribut
                """, p("c", categorieId, "n", nom, "code", codeLibre(nom), "t", typeChamp, "o", Boolean.TRUE.equals(r.obligatoire()), "f", !Boolean.FALSE.equals(r.filtrable()),
                "ordre", r.ordre() == null ? 0 : r.ordre(), "u", facultatif(r.unite(), 50), "aide", facultatif(r.aide(), 1000)));
        journal.enregistrer(auteur, MODULE, "CREER_ATTRIBUT", "attribut", id,
                "Attribut « " + nom + " » (" + typeChamp + ") créé dans « " + chemin(categorieId) + " »");
        return fiche(categorieId);
    }

    @Transactional
    public FicheCategorie modifierAttribut(UUID auteur, Long id, AttributRequest r) {
        Object[] a = attribut(id);
        Long categorieId = ((Number) a[0]).longValue();
        String nom = obligatoire(r.nom(), "Le nom", 150);
        String typeChamp = typeChamp(r.typeChamp());
        exigerLibre("SELECT COUNT(*) FROM attribut WHERE id_categorie = :c AND LOWER(nom) = LOWER(:n) AND id_attribut <> :id",
                p("c", categorieId, "n", nom, "id", id), "Un attribut porte déjà ce nom dans cette catégorie");
        if (!typeChamp.equals(a[3]) && repo.compter("SELECT COUNT(*) FROM offre_attribut WHERE id_attribut = :id", id) > 0) {
            throw new BusinessException("Des offres ont déjà renseigné cet attribut : son type de champ ne peut plus changer");
        }
        repo.executer("""
                UPDATE attribut SET nom = :n, type_champ = CAST(:t AS type_champ), obligatoire = :o, filtrable = :f,
                    ordre_affichage = :ordre, unite = :u, aide = :aide WHERE id_attribut = :id
                """, p("n", nom, "t", typeChamp, "o", Boolean.TRUE.equals(r.obligatoire()), "f", !Boolean.FALSE.equals(r.filtrable()), "ordre", r.ordre() == null ? 0 : r.ordre(),
                "u", facultatif(r.unite(), 50), "aide", facultatif(r.aide(), 1000), "id", id));
        journal.enregistrer(auteur, MODULE, "MODIFIER_ATTRIBUT", "attribut", id, "Attribut « " + a[1] + " » modifié");
        return fiche(categorieId);
    }

    @Transactional
    public FicheCategorie activerAttribut(UUID auteur, Long id, boolean actif, String motif) {
        exigerMotif(motif);
        Object[] a = attribut(id);
        if (Boolean.TRUE.equals(a[2]) == actif) throw new BusinessException(actif ? "Cet attribut est déjà actif" : "Cet attribut est déjà désactivé");
        repo.executer("UPDATE attribut SET actif = :a WHERE id_attribut = :id", p("a", actif, "id", id));
        journal.enregistrer(auteur, MODULE, actif ? "ACTIVER_ATTRIBUT" : "DESACTIVER_ATTRIBUT", "attribut", id,
                "Attribut « " + a[1] + " » " + (actif ? "réactivé" : "désactivé") + " : " + motif.trim());
        return fiche(((Number) a[0]).longValue());
    }

    @Transactional
    public FicheCategorie supprimerAttribut(UUID auteur, Long id, String motif) {
        exigerMotif(motif);
        Object[] a = attribut(id);
        if (repo.compter("SELECT COUNT(*) FROM offre_attribut WHERE id_attribut = :id", id) > 0) {
            throw new BusinessException("Des offres ont renseigné cet attribut : désactivez-le plutôt");
        }
        repo.executer("DELETE FROM attribut WHERE id_attribut = :id", p("id", id));
        journal.enregistrer(auteur, MODULE, "SUPPRIMER_ATTRIBUT", "attribut", id, "Attribut « " + a[1] + " » supprimé : " + motif.trim());
        return fiche(((Number) a[0]).longValue());
    }

    // ------------------------------------------------------------------ valeurs d'un attribut « liste »

    @Transactional
    public FicheCategorie creerValeur(UUID auteur, Long attributId, ValeurRequest r) {
        Object[] a = attribut(attributId);
        if (!"LISTE".equals(a[3]) && !"MULTI_LISTE".equals(a[3])) {
            throw new BusinessException("Seul un attribut de type liste a des valeurs proposées");
        }
        String valeur = obligatoire(r.valeur(), "La valeur", 255);
        exigerLibre("SELECT COUNT(*) FROM valeur_attribut_possible WHERE id_attribut = :a AND LOWER(valeur) = LOWER(:v)",
                p("a", attributId, "v", valeur), "Cette valeur existe déjà pour cet attribut");
        Long id = repo.inserer("""
                INSERT INTO valeur_attribut_possible (id_attribut, valeur, ordre_affichage, actif)
                VALUES (:a, :v, :o, TRUE) RETURNING id_valeur
                """, p("a", attributId, "v", valeur, "o", r.ordre() == null ? 0 : r.ordre()));
        journal.enregistrer(auteur, MODULE, "CREER_VALEUR", "valeur_attribut", id, "Valeur « " + valeur + " » ajoutée à l'attribut « " + a[1] + " »");
        return fiche(((Number) a[0]).longValue());
    }

    @Transactional
    public FicheCategorie modifierValeur(UUID auteur, Long id, ValeurRequest r) {
        Object[] v = valeur(id);
        String valeur = obligatoire(r.valeur(), "La valeur", 255);
        exigerLibre("SELECT COUNT(*) FROM valeur_attribut_possible WHERE id_attribut = :a AND LOWER(valeur) = LOWER(:v) AND id_valeur <> :id",
                p("a", ((Number) v[0]).longValue(), "v", valeur, "id", id), "Cette valeur existe déjà pour cet attribut");
        repo.executer("UPDATE valeur_attribut_possible SET valeur = :v, ordre_affichage = :o WHERE id_valeur = :id",
                p("v", valeur, "o", r.ordre() == null ? 0 : r.ordre(), "id", id));
        journal.enregistrer(auteur, MODULE, "MODIFIER_VALEUR", "valeur_attribut", id, "Valeur « " + v[1] + " » renommée « " + valeur + " »");
        return fiche(((Number) v[2]).longValue());
    }

    @Transactional
    public FicheCategorie activerValeur(UUID auteur, Long id, boolean actif, String motif) {
        exigerMotif(motif);
        Object[] v = valeur(id);
        if (Boolean.TRUE.equals(v[3]) == actif) throw new BusinessException(actif ? "Cette valeur est déjà active" : "Cette valeur est déjà désactivée");
        repo.executer("UPDATE valeur_attribut_possible SET actif = :a WHERE id_valeur = :id", p("a", actif, "id", id));
        journal.enregistrer(auteur, MODULE, actif ? "ACTIVER_VALEUR" : "DESACTIVER_VALEUR", "valeur_attribut", id,
                "Valeur « " + v[1] + " » " + (actif ? "réactivée" : "désactivée") + " : " + motif.trim());
        return fiche(((Number) v[2]).longValue());
    }

    @Transactional
    public FicheCategorie supprimerValeur(UUID auteur, Long id, String motif) {
        exigerMotif(motif);
        Object[] v = valeur(id);
        if (repo.compter("SELECT COUNT(*) FROM offre_attribut WHERE id_valeur = :id", id) > 0) {
            throw new BusinessException("Des offres utilisent cette valeur : désactivez-la plutôt");
        }
        repo.executer("DELETE FROM valeur_attribut_possible WHERE id_valeur = :id", p("id", id));
        journal.enregistrer(auteur, MODULE, "SUPPRIMER_VALEUR", "valeur_attribut", id, "Valeur « " + v[1] + " » supprimée : " + motif.trim());
        return fiche(((Number) v[2]).longValue());
    }

    // ------------------------------------------------------------------ outils

    private Noeud categorie(Long id) {
        return repo.categorie(id).orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));
    }

    /** [id_categorie, libelle, actif] */
    private Object[] type(Long id) {
        return repo.ligne("SELECT id_categorie, libelle, COALESCE(actif, TRUE) FROM type_offre WHERE id_type_offre = :id", id)
                .orElseThrow(() -> new ResourceNotFoundException("Type d'offre introuvable"));
    }

    /** [id_categorie, nom, actif, type_champ] */
    private Object[] attribut(Long id) {
        return repo.ligne("SELECT id_categorie, nom, COALESCE(actif, TRUE), CAST(type_champ AS TEXT) FROM attribut WHERE id_attribut = :id", id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribut introuvable"));
    }

    /** [id_attribut, valeur, id_categorie, actif] */
    private Object[] valeur(Long id) {
        return repo.ligne("""
                SELECT v.id_attribut, v.valeur, a.id_categorie, COALESCE(v.actif, TRUE)
                FROM valeur_attribut_possible v JOIN attribut a ON a.id_attribut = v.id_attribut WHERE v.id_valeur = :id""", id)
                .orElseThrow(() -> new ResourceNotFoundException("Valeur introuvable"));
    }

    private String chemin(Long id) {
        return String.join(" › ", repo.chemin(id).stream().map(Etape::nom).toList());
    }

    private void exigerNomLibreParmiSoeurs(Long parentId, String nom, Long saufId) {
        exigerLibre("""
                SELECT COUNT(*) FROM categorie WHERE COALESCE(id_categorie_parent, 0) = COALESCE(CAST(CAST(:p AS TEXT) AS BIGINT), 0)
                  AND LOWER(nom) = LOWER(:n) AND id_categorie <> COALESCE(CAST(CAST(:s AS TEXT) AS BIGINT), 0)""",
                p("p", texte(parentId), "n", nom, "s", texte(saufId)), "Une catégorie porte déjà ce nom à cet endroit");
    }

    private void exigerLibre(String sql, Map<String, Object> parametres, String message) {
        if (repo.existe(sql, parametres)) throw new BusinessException(message);
    }

    /** Code technique unique dérivé du nom : « Taille d'écran » → taille_d_ecran (puis _2, _3…). */
    private String codeLibre(String nom) {
        String base = Normalizer.normalize(nom, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase()
                .replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        if (base.isEmpty()) base = "attribut";
        if (base.length() > 90) base = base.substring(0, 90);
        String code = base;
        for (int i = 2; repo.existe("SELECT COUNT(*) FROM attribut WHERE code = :c", p("c", code)); i++) code = base + "_" + i;
        return code;
    }

    static String obligatoire(String s, String quoi, int max) {
        if (s == null || s.isBlank()) throw new BusinessException(quoi + " est obligatoire");
        String t = s.trim();
        if (t.length() > max) throw new BusinessException(quoi + " est trop long (" + max + " caractères au plus)");
        return t;
    }

    static String facultatif(String s, int max) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim();
        if (t.length() > max) throw new BusinessException("Texte trop long (" + max + " caractères au plus)");
        return t;
    }

    static String couleur(String s) {
        if (s == null || s.isBlank()) return null;
        if (!s.trim().matches("#[0-9a-fA-F]{6}")) throw new BusinessException("Couleur attendue au format #RRGGBB");
        return s.trim().toUpperCase();
    }

    static String principale(String s) {
        String p = s == null || s.isBlank() ? "PRODUIT" : s.trim();
        if (!PRINCIPALES.contains(p)) throw new BusinessException("Nature inconnue : PRODUIT ou SERVICE");
        return p;
    }

    static String typeChamp(String s) {
        if (s == null || !TYPES_CHAMP.contains(s)) throw new BusinessException("Type de champ inconnu : " + String.join(", ", TYPES_CHAMP.stream().sorted().toList()));
        return s;
    }

    static void exigerMotif(String motif) {
        if (motif == null || motif.isBlank()) throw new BusinessException("Le motif est obligatoire");
    }

    private static String texte(Long v) {
        return v == null ? null : v.toString();
    }

    /** Paramètres nommés, valeurs nulles permises. */
    private static Map<String, Object> p(Object... cleValeur) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < cleValeur.length; i += 2) m.put((String) cleValeur[i], cleValeur[i + 1]);
        return m;
    }
}

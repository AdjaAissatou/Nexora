package sn.ucad.nexora.catalogue.infrastructure.persistence.vision;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Empreintes visuelles des images d'offres (table image_vecteur), §11. */
@Repository
public class ImageVecteurRepository {

    /** Une image dont l'adresse et le vecteur sont connus, rattachée à son offre et à son espace. */
    public record Candidat(long offreId, long espaceId, float[] vecteur) {}

    @PersistenceContext
    private EntityManager em;

    /** Images des offres non supprimées sans vecteur pour ce modèle ; les erreurs sont retentées après un jour. */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<String> aIndexer(String modele, int max) {
        return em.createNativeQuery("""
                SELECT DISTINCT i.url FROM image i JOIN offre o ON o.id_offre = i.id_offre
                WHERE CAST(o.statut AS TEXT) <> 'SUPPRIME' AND i.url IS NOT NULL AND i.url <> ''
                  AND NOT EXISTS (SELECT 1 FROM image_vecteur v WHERE v.url = i.url AND v.modele = :m
                                  AND (v.vecteur IS NOT NULL OR v.date_calcul > NOW() - INTERVAL '1 day'))
                LIMIT :max""").setParameter("m", modele).setParameter("max", max).getResultList();
    }

    @Transactional
    public void enregistrer(String url, String modele, float[] vecteur, String erreur) {
        em.createNativeQuery("""
                INSERT INTO image_vecteur (url, modele, vecteur, erreur, date_calcul) VALUES (:u, :m, :v, :e, NOW())
                ON CONFLICT (url) DO UPDATE SET modele = EXCLUDED.modele, vecteur = EXCLUDED.vecteur,
                    erreur = EXCLUDED.erreur, date_calcul = EXCLUDED.date_calcul""")
                .setParameter("u", url).setParameter("m", modele)
                .setParameter("v", vecteur == null ? null : enOctets(vecteur))
                .setParameter("e", erreur == null ? null : (erreur.length() > 500 ? erreur.substring(0, 500) : erreur))
                .executeUpdate();
    }

    /** Toutes les images des offres visibles du public (publiées, espace actif et ouvert) déjà indexées. */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Candidat> candidatsVisibles(String modele) {
        List<Object[]> lignes = em.createNativeQuery("""
                SELECT o.id_offre, o.id_espace, v.vecteur
                FROM offre o
                JOIN espace_professionnel ep ON ep.id_espace = o.id_espace
                JOIN image i ON i.id_offre = o.id_offre
                JOIN image_vecteur v ON v.url = i.url AND v.modele = :m AND v.vecteur IS NOT NULL
                WHERE CAST(o.statut AS TEXT) = 'PUBLIE' AND ep.ouvert = TRUE AND CAST(ep.statut AS TEXT) = 'ACTIF'""")
                .setParameter("m", modele).getResultList();
        return lignes.stream().map(l -> new Candidat(((Number) l[0]).longValue(), ((Number) l[1]).longValue(),
                enVecteur((byte[]) l[2]))).toList();
    }

    /** Images indexées de ces offres (publiées ou non), pour partir de ce qu'un visiteur a regardé. */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Candidat> candidatsDe(java.util.Collection<Long> offres, String modele) {
        if (offres.isEmpty()) return List.of();
        List<Object[]> lignes = em.createNativeQuery("""
                SELECT o.id_offre, o.id_espace, v.vecteur FROM offre o
                JOIN image i ON i.id_offre = o.id_offre
                JOIN image_vecteur v ON v.url = i.url AND v.modele = :m AND v.vecteur IS NOT NULL
                WHERE o.id_offre IN (:ids)""").setParameter("m", modele).setParameter("ids", offres).getResultList();
        return lignes.stream().map(l -> new Candidat(((Number) l[0]).longValue(), ((Number) l[1]).longValue(),
                enVecteur((byte[]) l[2]))).toList();
    }

    /** Espaces de ces offres. */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Long> espaces(java.util.Collection<Long> offres) {
        if (offres.isEmpty()) return List.of();
        List<Number> r = em.createNativeQuery("SELECT DISTINCT id_espace FROM offre WHERE id_offre IN (:ids)")
                .setParameter("ids", offres).getResultList();
        return r.stream().map(Number::longValue).toList();
    }

    /** Catégories de ces offres (pour compléter les suggestions quand les photos ne suffisent pas). */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Long> categories(java.util.Collection<Long> offres) {
        if (offres.isEmpty()) return List.of();
        List<Number> r = em.createNativeQuery("SELECT DISTINCT id_categorie FROM offre WHERE id_offre IN (:ids) AND id_categorie IS NOT NULL")
                .setParameter("ids", offres).getResultList();
        return r.stream().map(Number::longValue).toList();
    }

    /** [url de l'image principale (ou première image), id de l'espace] d'une offre visible. */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Optional<Object[]> imageOffre(long offreId) {
        List<Object[]> r = em.createNativeQuery("""
                SELECT i.url, o.id_espace FROM offre o JOIN image i ON i.id_offre = o.id_offre
                WHERE o.id_offre = :id AND CAST(o.statut AS TEXT) = 'PUBLIE'
                ORDER BY i.principale DESC, i.ordre_affichage LIMIT 1""").setParameter("id", offreId).getResultList();
        return r.stream().findFirst();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Optional<float[]> vecteur(String url, String modele) {
        List<byte[]> r = em.createNativeQuery("SELECT vecteur FROM image_vecteur WHERE url = :u AND modele = :m AND vecteur IS NOT NULL")
                .setParameter("u", url).setParameter("m", modele).getResultList();
        return r.stream().findFirst().map(ImageVecteurRepository::enVecteur);
    }

    /** [images indexées, images en attente] pour les offres non supprimées. */
    @Transactional(readOnly = true)
    public long[] compteurs(String modele) {
        Object[] r = (Object[]) em.createNativeQuery("""
                SELECT COUNT(*) FILTER (WHERE v.vecteur IS NOT NULL), COUNT(*) FILTER (WHERE v.url IS NULL)
                FROM (SELECT DISTINCT i.url FROM image i JOIN offre o ON o.id_offre = i.id_offre
                      WHERE CAST(o.statut AS TEXT) <> 'SUPPRIME') im
                LEFT JOIN image_vecteur v ON v.url = im.url AND v.modele = :m""").setParameter("m", modele).getSingleResult();
        return new long[]{((Number) r[0]).longValue(), ((Number) r[1]).longValue()};
    }

    static byte[] enOctets(float[] v) {
        ByteBuffer b = ByteBuffer.allocate(v.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (float x : v) b.putFloat(x);
        return b.array();
    }

    static float[] enVecteur(byte[] octets) {
        ByteBuffer b = ByteBuffer.wrap(octets).order(ByteOrder.LITTLE_ENDIAN);
        float[] v = new float[octets.length / 4];
        for (int i = 0; i < v.length; i++) v[i] = b.getFloat();
        return v;
    }
}

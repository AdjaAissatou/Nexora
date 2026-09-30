package sn.ucad.nexora.espace.infrastructure.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sn.ucad.nexora.common.exception.BusinessException;

/**
 * Stockage PRIVÉ des justificatifs de vérification (pièces d'identité, NINEA, etc.).
 *
 * Contrairement aux images des offres et des espaces (écrites par le web dans
 * {@code nexora-uploads/} et servies publiquement sous {@code /uploads/**}), ces fichiers ne sont
 * jamais exposés en statique : ils ne sortent que par l'endpoint authentifié de téléchargement,
 * après contrôle de l'accès (propriétaire, agent de la demande ou administrateur).
 * Voir docs/architecture-acteurs.md §8.4.
 */
@Component
public class StockageJustificatifs {

    /** Même limite que les envois du web (spring.servlet.multipart.max-file-size). */
    public static final long TAILLE_MAX = 8L * 1024 * 1024;

    private final Path racine;

    public StockageJustificatifs(
            @Value("${nexora.justificatifs.dir:${NEXORA_JUSTIFICATIFS_DIR:${user.home}/nexora-justificatifs}}") String dossier) {
        this.racine = Paths.get(dossier).toAbsolutePath().normalize();
    }

    /** Fichier écrit sur disque : chemin relatif à la racine privée, type réel, taille, empreinte. */
    public record FichierStocke(String chemin, String typeMime, long taille, String empreinteSha256) {}

    public FichierStocke enregistrer(Long verificationId, byte[] contenu) {
        if (contenu == null || contenu.length == 0) {
            throw new BusinessException("Le fichier est vide");
        }
        if (contenu.length > TAILLE_MAX) {
            throw new BusinessException("Fichier trop volumineux : 8 Mo au maximum");
        }
        // Le type est déduit du contenu, jamais du nom ni de l'en-tête envoyé par le client.
        String[] type = typeReel(contenu);
        if (type == null) {
            throw new BusinessException("Format non accepté : PDF, JPG ou PNG uniquement");
        }
        String chemin = verificationId + "/" + UUID.randomUUID() + "." + type[1];
        try {
            Path cible = resoudre(chemin);
            Files.createDirectories(cible.getParent());
            Files.write(cible, contenu);
        } catch (IOException e) {
            throw new IllegalStateException("Enregistrement du justificatif impossible", e);
        }
        return new FichierStocke(chemin, type[0], contenu.length, sha256(contenu));
    }

    public InputStream lire(String chemin) throws IOException {
        return Files.newInputStream(resoudre(chemin));
    }

    private Path resoudre(String chemin) {
        Path cible = racine.resolve(chemin).normalize();
        if (!cible.startsWith(racine)) {
            throw new BusinessException("Chemin de justificatif invalide");
        }
        return cible;
    }

    /** {type MIME, extension} d'après la signature du fichier, ou null si le format n'est pas accepté. */
    static String[] typeReel(byte[] c) {
        if (c.length >= 4 && c[0] == '%' && c[1] == 'P' && c[2] == 'D' && c[3] == 'F') {
            return new String[] {"application/pdf", "pdf"};
        }
        if (c.length >= 8 && (c[0] & 0xFF) == 0x89 && c[1] == 'P' && c[2] == 'N' && c[3] == 'G') {
            return new String[] {"image/png", "png"};
        }
        if (c.length >= 3 && (c[0] & 0xFF) == 0xFF && (c[1] & 0xFF) == 0xD8 && (c[2] & 0xFF) == 0xFF) {
            return new String[] {"image/jpeg", "jpg"};
        }
        return null;
    }

    private static String sha256(byte[] contenu) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(contenu));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

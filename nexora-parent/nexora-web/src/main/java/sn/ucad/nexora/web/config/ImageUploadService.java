package sn.ucad.nexora.web.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Enregistrement des images envoyées depuis les formulaires (logo, couverture, photos du lieu)
 * sur le disque local, servies ensuite en statique via {@link StaticUploadsConfig} sous
 * {@code /uploads/**}. Classe utilitaire pure (pas de bean Spring) : les beans JSF sont gérés
 * par Weld/CDI, qui n'a pas accès aux {@code @Bean} Spring — voir {@link GatewayConfig}.
 */
public final class ImageUploadService {

    private static final Set<String> EXTENSIONS_AUTORISEES = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private ImageUploadService() {}

    public static Path repertoire() {
        String valeur = System.getenv("NEXORA_UPLOADS_DIR");
        if (valeur == null || valeur.isBlank()) valeur = System.getProperty("nexora.uploads.dir");
        if (valeur == null || valeur.isBlank()) {
            valeur = Paths.get(System.getProperty("user.home"), "nexora-uploads").toString();
        }
        return Paths.get(valeur);
    }

    /** Enregistre le fichier reçu et retourne son URL publique (chemin relatif servi par {@code /uploads/}). */
    public static String enregistrer(InputStream contenu, String nomOriginal) throws IOException {
        String extension = extension(nomOriginal);
        if (!EXTENSIONS_AUTORISEES.contains(extension)) {
            throw new IllegalArgumentException("Format d'image non supporté : ." + extension);
        }
        Path dossier = repertoire();
        Files.createDirectories(dossier);
        String nomFichier = UUID.randomUUID() + "." + extension;
        Path cible = dossier.resolve(nomFichier);
        Files.copy(contenu, cible, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + nomFichier;
    }

    private static String extension(String nomFichier) {
        int point = nomFichier == null ? -1 : nomFichier.lastIndexOf('.');
        return point < 0 ? "" : nomFichier.substring(point + 1).toLowerCase();
    }
}

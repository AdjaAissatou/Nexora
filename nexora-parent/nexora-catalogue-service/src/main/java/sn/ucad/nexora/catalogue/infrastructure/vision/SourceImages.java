package sn.ucad.nexora.catalogue.infrastructure.vision;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Lit les images des offres pour les indexer : les photos déposées sur Nexora ({@code /uploads/...},
 * dans le même dossier que celui du web : {@code NEXORA_UPLOADS_DIR}, par défaut
 * {@code ~/nexora-uploads}) et les images en ligne ({@code http(s)://}).
 */
@Component
public class SourceImages {

    /** Taille maximale d'une image lue ou reçue. */
    public static final int TAILLE_MAX = 10 * 1024 * 1024;
    private static final String PREFIXE_UPLOADS = "/uploads/";

    private final Path uploads;
    private final HttpClient http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
            .proxy(ProxySelector.getDefault()).connectTimeout(Duration.ofSeconds(10)).build();

    public SourceImages(@Value("${nexora.uploads.dir:}") String dossier) {
        String valeur = dossier;
        if (valeur == null || valeur.isBlank()) valeur = System.getenv("NEXORA_UPLOADS_DIR");
        if (valeur == null || valeur.isBlank()) valeur = Paths.get(System.getProperty("user.home"), "nexora-uploads").toString();
        this.uploads = Paths.get(valeur).toAbsolutePath().normalize();
    }

    public BufferedImage lire(String url) throws IOException, InterruptedException {
        if (url == null || url.isBlank()) throw new IOException("image sans adresse");
        if (url.startsWith(PREFIXE_UPLOADS)) {
            Path chemin = uploads.resolve(url.substring(PREFIXE_UPLOADS.length())).normalize();
            if (!chemin.startsWith(uploads)) throw new IOException("chemin refusé");
            if (!Files.isRegularFile(chemin)) throw new IOException("fichier introuvable : " + chemin);
            if (Files.size(chemin) > TAILLE_MAX) throw new IOException("image trop lourde");
            return decoder(Files.readAllBytes(chemin));
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            HttpResponse<InputStream> r = http.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20))
                    .header("User-Agent", "Nexora/1.0").GET().build(), HttpResponse.BodyHandlers.ofInputStream());
            if (r.statusCode() != 200) throw new IOException("HTTP " + r.statusCode());
            try (InputStream in = r.body()) {
                byte[] octets = in.readNBytes(TAILLE_MAX + 1);
                if (octets.length > TAILLE_MAX) throw new IOException("image trop lourde");
                return decoder(octets);
            }
        }
        throw new IOException("source d'image non prise en charge");
    }

    /** JPEG, PNG, WebP, GIF ou BMP ; null est remplacé par une erreur explicite. */
    public static BufferedImage decoder(byte[] octets) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(octets));
        if (image == null || image.getWidth() < 8 || image.getHeight() < 8) {
            throw new IOException("format d'image non pris en charge");
        }
        return image;
    }
}

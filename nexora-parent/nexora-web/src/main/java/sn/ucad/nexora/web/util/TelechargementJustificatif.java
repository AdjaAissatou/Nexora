package sn.ucad.nexora.web.util;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import java.io.IOException;
import java.io.OutputStream;
import org.springframework.http.ContentDisposition;
import sn.ucad.nexora.web.dto.verification.FichierJustificatif;

/**
 * Renvoie un justificatif au navigateur depuis une action JSF, sans jamais l'écrire sur le disque du
 * web ni lui donner d'URL publique : le fichier transite d'espace-service (qui a vérifié l'accès)
 * directement vers la réponse HTTP.
 */
public final class TelechargementJustificatif {

    private TelechargementJustificatif() {}

    public static void envoyer(FichierJustificatif fichier) throws IOException {
        FacesContext contexte = FacesContext.getCurrentInstance();
        ExternalContext externe = contexte.getExternalContext();
        externe.responseReset();
        externe.setResponseContentType(fichier.typeMime());
        externe.setResponseContentLength(fichier.contenu().length);
        externe.setResponseHeader("Content-Disposition",
                ContentDisposition.inline().filename(fichier.nom(), java.nio.charset.StandardCharsets.UTF_8).build().toString());
        externe.setResponseHeader("Cache-Control", "private, no-store");
        externe.setResponseHeader("X-Content-Type-Options", "nosniff");
        try (OutputStream sortie = externe.getResponseOutputStream()) {
            sortie.write(fichier.contenu());
        }
        contexte.responseComplete();
    }
}

package sn.ucad.nexora.web.config;

import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.application.ResourceHandlerWrapper;
import jakarta.faces.application.ResourceWrapper;

/**
 * Ajoute « &amp;v=… » à l'adresse des feuilles de style et des scripts de Nexora (bibliothèques
 * css et js). En production, JSF demande au navigateur de les garder en cache plusieurs jours sous
 * la même adresse : sans cette version, une nouvelle feuille de style n'était pas chargée et les
 * pages modifiées s'affichaient avec l'ancien style. La version change à chaque démarrage du web.
 */
public class RessourcesVersionnees extends ResourceHandlerWrapper {

    static final String VERSION = Long.toString(System.currentTimeMillis() / 1000, 36);

    public RessourcesVersionnees(ResourceHandler wrapped) {
        super(wrapped);
    }

    @Override
    public Resource createResource(String nom, String bibliotheque) {
        Resource r = super.createResource(nom, bibliotheque);
        if (r == null || !("css".equals(bibliotheque) || "js".equals(bibliotheque))) return r;
        return new ResourceWrapper(r) {
            @Override
            public String getRequestPath() {
                String chemin = super.getRequestPath();
                return chemin + (chemin.contains("?") ? "&" : "?") + "v=" + VERSION;
            }
        };
    }
}

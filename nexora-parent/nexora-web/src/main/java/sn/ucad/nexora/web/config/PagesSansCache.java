package sn.ucad.nexora.web.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Les pages ne sont jamais gardées en cache par le navigateur : après une déconnexion, le bouton
 * « Retour » ne doit pas réafficher une page connectée (nom, espace, back-office) à la personne
 * suivante sur un ordinateur partagé. Les feuilles de style, scripts et images restent en cache
 * (ils ne contiennent rien de personnel).
 */
@Component
public class PagesSansCache extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest requete) {
        String chemin = requete.getRequestURI().substring(requete.getContextPath().length());
        return chemin.startsWith("/jakarta.faces.resource/") || chemin.startsWith("/uploads/")
                || chemin.startsWith("/resources/") || chemin.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requete, HttpServletResponse reponse, FilterChain suite)
            throws ServletException, IOException {
        reponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        reponse.setHeader("Pragma", "no-cache");
        reponse.setDateHeader("Expires", 0);
        suite.doFilter(requete, reponse);
    }
}

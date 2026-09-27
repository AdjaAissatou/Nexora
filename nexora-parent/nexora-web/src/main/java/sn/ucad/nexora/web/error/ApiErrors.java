package sn.ucad.nexora.web.error;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestClientResponseException;

/**
 * Traduit une erreur HTTP de l'API Gateway en {@link ApiException} avec un message en français.
 *
 * Tolère les deux formes rencontrées dans l'écosystème Nexora : le {@code GlobalExceptionHandler}
 * maison de catalogue-service ({@code {timestamp, status, message}}) et le {@code ProblemDetail}
 * par défaut de Spring ({@code {title, detail, status, errors}}) des autres services.
 */
public final class ApiErrors {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ApiErrors() {}

    public static ApiException depuis(RestClientResponseException e) {
        int statut = e.getStatusCode().value();
        String message = extraireMessage(e.getResponseBodyAsString(), statut);
        return new ApiException(message, statut, e);
    }

    public static ApiException reseau(Exception e) {
        return new ApiException("Impossible de joindre Nexora. Réessayez dans un instant.", 0, e);
    }

    private static String extraireMessage(String corps, int statut) {
        if (corps != null && !corps.isBlank()) {
            try {
                JsonNode noeud = MAPPER.readTree(corps);
                for (String champ : new String[] {"message", "detail", "error_description"}) {
                    JsonNode valeur = noeud.get(champ);
                    if (valeur != null && valeur.isTextual() && !valeur.asText().isBlank()) {
                        return valeur.asText();
                    }
                }
            } catch (Exception ignore) {
                // Corps non-JSON : on retombe sur le message générique ci-dessous.
            }
        }
        return messageParDefaut(statut);
    }

    private static String messageParDefaut(int statut) {
        return switch (statut) {
            case 400 -> "Certaines informations sont invalides.";
            case 401 -> "Email ou mot de passe incorrect.";
            case 403 -> "Vous n'avez pas accès à cette ressource.";
            case 404 -> "Élément introuvable.";
            case 409 -> "Cet élément existe déjà.";
            default -> statut >= 500
                    ? "Nexora rencontre un souci technique. Réessayez plus tard."
                    : "Une erreur est survenue.";
        };
    }
}

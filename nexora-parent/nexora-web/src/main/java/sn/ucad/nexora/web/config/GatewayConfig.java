package sn.ucad.nexora.web.config;

/**
 * URL publique de l'écosystème Nexora (l'API Gateway), lue directement de l'environnement.
 *
 * Les clients REST ({@link sn.ucad.nexora.web.client.AuthApiClient},
 * {@link sn.ucad.nexora.web.client.CatalogueApiClient}) sont des beans CDI, gérés par Weld —
 * le pont entre le conteneur Spring et le conteneur CDI n'expose pas les {@code @Bean} Spring
 * à {@code @Inject} côté CDI, donc on évite volontairement de faire dépendre ce chemin de Spring.
 */
public final class GatewayConfig {

    private static final String DEFAUT = "http://localhost:8080";

    private GatewayConfig() {}

    public static String gatewayUrl() {
        String valeur = System.getenv("GATEWAY_URL");
        if (valeur == null || valeur.isBlank()) valeur = System.getProperty("nexora.api.gateway-url");
        return (valeur == null || valeur.isBlank()) ? DEFAUT : valeur.replaceAll("/+$", "");
    }
}

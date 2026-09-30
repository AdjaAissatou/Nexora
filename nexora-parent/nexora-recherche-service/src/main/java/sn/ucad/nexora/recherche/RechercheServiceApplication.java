package sn.ucad.nexora.recherche;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

// Le gestionnaire d'erreurs de nexora-common est exclu : ce service a le sien
// (presentation/controller/GlobalExceptionHandler), deux @RestControllerAdvice seraient en concurrence.
@SpringBootApplication(excludeName = "sn.ucad.nexora.common.web.GlobalExceptionHandler")
@EnableDiscoveryClient
public class RechercheServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RechercheServiceApplication.class, args);
    }
}

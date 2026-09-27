package sn.ucad.nexora.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Application web Nexora — JSF (Jakarta Faces) / PrimeFaces, embarquée dans Spring Boot.
 *
 * Elle ne contient aucune logique métier ni accès direct aux bases de données : chaque page
 * est adossée à un managed bean CDI qui appelle l'API Gateway (voir {@code nexora.api.gateway-url}),
 * exactement comme n'importe quel autre client de l'écosystème Nexora.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class NexoraWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexoraWebApplication.class, args);
    }
}

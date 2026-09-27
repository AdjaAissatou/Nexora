package sn.ucad.nexora.recherche;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RechercheServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RechercheServiceApplication.class, args);
    }
}

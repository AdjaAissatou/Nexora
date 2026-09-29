package sn.ucad.nexora.web.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Sert les images uploadées (logo, couverture, photos du lieu) depuis le disque sous {@code /uploads/**}. */
@Configuration
public class StaticUploadsConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            Files.createDirectories(ImageUploadService.repertoire());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        String emplacement = ImageUploadService.repertoire().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(emplacement);
    }
}

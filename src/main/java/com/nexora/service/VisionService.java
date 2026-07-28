package com.nexora.service;

/** Reconnaissance d'image embarquee : d'une photo -> un mot-cle de recherche. */
public interface VisionService {

    /**
     * Analyse une image (JPEG/PNG) avec le modele MobileNetV2 et renvoie un
     * mot-cle de recherche en francais (ex. « sac », « chemise », « montre »),
     * ou {@code null} si l'image est illisible ou aucune classe pertinente.
     */
    String motCleDepuisImage(byte[] image);
}

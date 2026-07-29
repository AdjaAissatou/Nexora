package com.nexora.service.impl;

import com.nexora.service.VisionService;
import jakarta.ejb.Stateless;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client du microservice de reconnaissance d'image (nexora-vision).
 *
 * La classification lourde (ONNX + MobileNetV2) est deportee dans un process
 * separe : ici on ne fait qu'un appel HTTP. Cela allege le serveur applicatif
 * et permet de scaler la vision independamment. En cas d'indisponibilite du
 * service, on renvoie {@code null} sans casser la recherche.
 *
 * URL configurable via la variable d'environnement {@code NEXORA_VISION_URL}
 * (defaut {@code http://localhost:8090/recognize}).
 */
@Stateless
public class VisionServiceImpl implements VisionService {

    private static final Logger LOG = Logger.getLogger(VisionServiceImpl.class.getName());

    private static final String URL = resoudreUrl();
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    @Override
    public String motCleDepuisImage(byte[] image) {
        if (image == null || image.length == 0) return null;
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(URL))
                    .timeout(Duration.ofSeconds(6))
                    .header("Content-Type", "application/octet-stream")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(image))
                    .build();
            HttpResponse<String> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) return null;
            return extraireKeyword(res.body());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Service de vision injoignable (" + URL + ")", e);
            return null;
        }
    }

    /** Extrait la valeur du champ "keyword" d'une reponse JSON plate. */
    private static String extraireKeyword(String json) {
        if (json == null) return null;
        int i = json.indexOf("\"keyword\"");
        if (i < 0) return null;
        int deuxPoints = json.indexOf(':', i);
        if (deuxPoints < 0) return null;
        int p = deuxPoints + 1;
        while (p < json.length() && Character.isWhitespace(json.charAt(p))) p++;
        if (p >= json.length()) return null;
        if (json.startsWith("null", p)) return null;
        if (json.charAt(p) != '"') return null;
        StringBuilder sb = new StringBuilder();
        for (int k = p + 1; k < json.length(); k++) {
            char c = json.charAt(k);
            if (c == '\\' && k + 1 < json.length()) { sb.append(json.charAt(++k)); continue; }
            if (c == '"') break;
            sb.append(c);
        }
        String s = sb.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private static String resoudreUrl() {
        String v = System.getenv("NEXORA_VISION_URL");
        return (v != null && !v.isBlank()) ? v.trim() : "http://localhost:8090/recognize";
    }
}

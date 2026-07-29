package com.nexora.common.security;

import com.nexora.domain.user.Utilisateur;
import jakarta.enterprise.context.ApplicationScoped;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service JWT minimaliste (HS256) sans dependance externe : signe et verifie
 * des jetons d'authentification pour l'API REST (web et mobile Flutter).
 *
 * <p>En production, le secret doit provenir d'une source securisee
 * (variable d'environnement, coffre-fort), et non d'une constante.</p>
 */
@ApplicationScoped
public class JwtService {

    /** A remplacer par une configuration securisee (ENV/secret manager). */
    private static final String SECRET = System.getenv().getOrDefault(
            "NEXORA_JWT_SECRET", "change-me-nexora-dev-secret-key-0123456789");
    private static final long VALIDITE_MS = 1000L * 60 * 60 * 24; // 24 h
    private static final String HEADER_B64 =
            base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
    private static final Pattern SUB = Pattern.compile("\"sub\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern EXP = Pattern.compile("\"exp\"\\s*:\\s*(\\d+)");

    /** Genere un jeton signe pour l'utilisateur donne. */
    public String generateToken(Utilisateur u) {
        long now = System.currentTimeMillis() / 1000;
        long exp = now + VALIDITE_MS / 1000;
        String profile = u.getProfile() != null ? u.getProfile().getLibelle() : "CLIENT";
        String payload = "{"
                + "\"sub\":\"" + u.getIdUtilisateur() + "\","
                + "\"email\":\"" + escape(u.getEmail()) + "\","
                + "\"name\":\"" + escape(u.getNom()) + "\","
                + "\"profile\":\"" + escape(profile) + "\","
                + "\"iat\":" + now + ",\"exp\":" + exp + "}";
        String head = HEADER_B64 + "." + base64Url(payload.getBytes(StandardCharsets.UTF_8));
        return head + "." + sign(head);
    }

    public long getExpiresInSeconds() {
        return VALIDITE_MS / 1000;
    }

    /** Verifie signature + expiration et retourne le "sub" (id utilisateur). */
    public Optional<String> validateAndGetSubject(String token) {
        if (token == null) return Optional.empty();
        String[] parts = token.split("\\.");
        if (parts.length != 3) return Optional.empty();
        String signingInput = parts[0] + "." + parts[1];
        if (!constantTimeEquals(sign(signingInput), parts[2])) return Optional.empty();
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        Matcher expM = EXP.matcher(payload);
        if (expM.find() && Long.parseLong(expM.group(1)) < System.currentTimeMillis() / 1000) {
            return Optional.empty(); // expire
        }
        Matcher subM = SUB.matcher(payload);
        return subM.find() ? Optional.of(subM.group(1)) : Optional.empty();
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return base64Url(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Erreur de signature JWT", e);
        }
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int r = 0;
        for (int i = 0; i < a.length(); i++) r |= a.charAt(i) ^ b.charAt(i);
        return r == 0;
    }
}

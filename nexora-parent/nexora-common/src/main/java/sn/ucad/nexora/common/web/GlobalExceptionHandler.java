package sn.ucad.nexora.common.web;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;
import sn.ucad.nexora.common.exception.UnauthorizedException;
import sn.ucad.nexora.common.exception.ValidationException;

/**
 * Gestionnaire d'erreurs partagé par tous les microservices Nexora (auto-configuré via
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}).
 *
 * Avant cette classe, chaque service dupliquait le même {@code @RestControllerAdvice}, et certains
 * (auth-service, user-service, espace-service) n'en avaient tout simplement aucun : une
 * {@link BusinessException}/{@link ResourceNotFoundException}/{@link IllegalArgumentException} levée
 * dans le code métier finissait alors en exception non gérée, et l'appelant ne recevait qu'un statut
 * générique à corps vide au lieu du vrai message.
 *
 * Couvre aussi {@link IllegalArgumentException} (avec l'heuristique déjà utilisée par
 * catalogue-service/recherche-service : "introuvable" dans le message → 404, sinon 400), car
 * plusieurs services l'utilisent directement plutôt que la hiérarchie d'exceptions de ce module.
 */
@AutoConfiguration
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return corps(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex) {
        return corps(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler({BusinessException.class, ValidationException.class})
    public ResponseEntity<Map<String, Object>> handleBusiness(RuntimeException ex) {
        return corps(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        HttpStatus status = ex.getMessage() != null && ex.getMessage().contains("introuvable")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return corps(status, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " : " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return corps(HttpStatus.BAD_REQUEST, "Validation échouée : " + details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return corps(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne : " + ex.getMessage());
    }

    private static ResponseEntity<Map<String, Object>> corps(HttpStatus statut, String message) {
        return ResponseEntity.status(statut).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", statut.value(),
                "message", message));
    }
}

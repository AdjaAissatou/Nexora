package sn.ucad.nexora.recherche.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Règle métier refusée (motif manquant, doublon, contenu non visible…). */
    @ExceptionHandler(sn.ucad.nexora.common.exception.BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleMetier(sn.ucad.nexora.common.exception.BusinessException ex) {
        return corps(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(sn.ucad.nexora.common.exception.ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleIntrouvable(sn.ucad.nexora.common.exception.ResourceNotFoundException ex) {
        return corps(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    private static ResponseEntity<Map<String, Object>> corps(HttpStatus statut, String message) {
        return ResponseEntity.status(statut).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", statut.value(),
                "message", message == null ? statut.getReasonPhrase() : message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        HttpStatus status = ex.getMessage().contains("introuvable") || ex.getMessage().contains("Avis introuvable")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " : " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 400,
                "message", "Validation échouée : " + details
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 500,
                "message", "Erreur interne : " + ex.getMessage()
        ));
    }
}

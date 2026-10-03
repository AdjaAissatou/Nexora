package sn.ucad.nexora.catalogue.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;
import sn.ucad.nexora.common.exception.UnauthorizedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Règle métier refusée (ex. modération : motif manquant, offre déjà suspendue). */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleMetier(BusinessException ex) {
        return corps(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** Corps JSON illisible ou incomplet : erreur de l'appelant. */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleIllisible(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        return corps(HttpStatus.BAD_REQUEST, "Requête invalide : corps JSON illisible ou incomplet");
    }

    /** Photo trop lourde pour la recherche par photo (§11). */
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleTropLourd(org.springframework.web.multipart.MaxUploadSizeExceededException ex) {
        return corps(HttpStatus.BAD_REQUEST, "Photo trop lourde (10 Mo au plus)");
    }

    @ExceptionHandler({org.springframework.web.multipart.support.MissingServletRequestPartException.class,
            org.springframework.web.multipart.MultipartException.class,
            org.springframework.web.HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<Map<String, Object>> handleSansPhoto(Exception ex) {
        return corps(HttpStatus.BAD_REQUEST, "Choisissez une photo");
    }

    /** Paramètre obligatoire absent ou mal formé : erreur de l'appelant. */
    @ExceptionHandler({org.springframework.web.bind.MissingServletRequestParameterException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> handleParametre(Exception ex) {
        return corps(HttpStatus.BAD_REQUEST, "Paramètre manquant ou invalide");
    }

    /** Chemin inconnu : 404 et non 500. */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCheminInconnu(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        return corps(HttpStatus.NOT_FOUND, "Ressource introuvable");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleIntrouvable(ResourceNotFoundException ex) {
        return corps(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleInterdit(UnauthorizedException ex) {
        return corps(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    private static ResponseEntity<Map<String, Object>> corps(HttpStatus statut, String message) {
        return ResponseEntity.status(statut).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", statut.value(),
                "message", message == null ? statut.getReasonPhrase() : message
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 404,
                "message", ex.getMessage()
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

package sn.ucad.nexora.auth.presentation.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
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
 * Sans cette classe, chaque {@link BusinessException}/{@link ResourceNotFoundException} levée par
 * register/login/verify-otp/forgot-password/reset-password (compte non vérifié, OTP expiré, email
 * déjà utilisé...) n'était interceptée par personne : la requête finissait en exception non gérée
 * remontant jusqu'au conteneur, et le client ne recevait qu'un statut générique à corps vide — le
 * vrai message métier ("Votre compte n'est pas encore vérifié.", etc.) n'atteignait jamais l'appelant.
 */
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

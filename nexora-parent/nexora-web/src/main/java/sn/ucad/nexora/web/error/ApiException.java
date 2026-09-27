package sn.ucad.nexora.web.error;

/** Erreur d'appel à l'API Gateway, avec un message déjà présentable à l'utilisateur. */
public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(String message, int status) {
        super(message);
        this.status = status;
    }

    public ApiException(String message, int status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}

package cm.kfokam48.suivi.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier portant directement le statut HTTP et le code d'erreur
 * stable attendus par api/contrat.yaml (ex. CODE_EXPIRE, DEJA_PRESENT).
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}

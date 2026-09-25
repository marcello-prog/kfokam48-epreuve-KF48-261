package cm.kfokam48.suivi.web;

import cm.kfokam48.suivi.dto.ErreurResponse;
import cm.kfokam48.suivi.exception.ApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Gestion centralisée des erreurs (contrainte B4) : toute erreur renvoyée au
 * client respecte le format { code, message } imposé par api/contrat.yaml.
 * Aucune stack trace, aucun corps vide, aucune page d'erreur Spring par défaut.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErreurResponse> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ErreurResponse(ex.getCode(), ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + " : " + e.getDefaultMessage())
                .orElse("Requête invalide.");
        return ResponseEntity.badRequest().body(new ErreurResponse("REQUETE_INVALIDE", message));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest()
                .body(new ErreurResponse("REQUETE_ILLISIBLE", "Le corps de la requête est invalide."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurResponse> handleAutre(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErreurResponse("ERREUR_INTERNE", "Une erreur inattendue est survenue."));
    }
}

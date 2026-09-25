package cm.kfokam48.suivi.dto;

/**
 * Format d'erreur imposé par api/contrat.yaml pour toutes les erreurs, sans exception.
 */
public record ErreurResponse(String code, String message) {
}

package cm.kfokam48.suivi.dto;

public record TableauLigne(Long etudiantId, String nom, long presences, long exercicesDeposes,
                            Double moyenne, long relecturesEnAttente) {
}

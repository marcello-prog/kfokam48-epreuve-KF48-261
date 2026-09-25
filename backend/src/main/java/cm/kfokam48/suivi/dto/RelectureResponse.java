package cm.kfokam48.suivi.dto;

import cm.kfokam48.suivi.entity.Relecture;
import cm.kfokam48.suivi.entity.StatutRelecture;

public record RelectureResponse(Long id, Long exerciceId, String lien, StatutRelecture statut,
                                 Integer note, String commentaire) {

    public static RelectureResponse from(Relecture relecture) {
        return new RelectureResponse(
                relecture.getId(),
                relecture.getExercice().getId(),
                relecture.getExercice().getLien(),
                relecture.getStatut(),
                relecture.getNote(),
                relecture.getCommentaire()
        );
    }
}

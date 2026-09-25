package cm.kfokam48.suivi.dto;

import cm.kfokam48.suivi.entity.Exercice;
import cm.kfokam48.suivi.entity.StatutExercice;

public record ExerciceResponse(Long id, StatutExercice statut) {

    public static ExerciceResponse from(Exercice exercice) {
        return new ExerciceResponse(exercice.getId(), exercice.getStatut());
    }
}

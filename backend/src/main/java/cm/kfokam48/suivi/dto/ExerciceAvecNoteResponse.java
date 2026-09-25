package cm.kfokam48.suivi.dto;

import cm.kfokam48.suivi.entity.StatutExercice;

import java.util.List;

/**
 * EF17, EF19 · RG17 (étape 3) — note finale d'un exercice, sans identité
 * des relecteurs (RG8).
 */
public record ExerciceAvecNoteResponse(Long id, Long sessionId, String lien, StatutExercice statut,
                                        Double note, boolean provisoire, List<String> commentaires) {
}

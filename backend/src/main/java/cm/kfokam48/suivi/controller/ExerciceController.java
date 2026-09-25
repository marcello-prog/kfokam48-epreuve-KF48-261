package cm.kfokam48.suivi.controller;

import cm.kfokam48.suivi.dto.DeposerExerciceRequest;
import cm.kfokam48.suivi.dto.ExerciceResponse;
import cm.kfokam48.suivi.entity.Exercice;
import cm.kfokam48.suivi.service.ExerciceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exercices")
public class ExerciceController {

    private final ExerciceService exerciceService;

    public ExerciceController(ExerciceService exerciceService) {
        this.exerciceService = exerciceService;
    }

    @PostMapping
    public ResponseEntity<ExerciceResponse> deposerExercice(@Valid @RequestBody DeposerExerciceRequest requete) {
        Exercice exercice = exerciceService.deposerExercice(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(ExerciceResponse.from(exercice));
    }
}

package cm.kfokam48.suivi.controller;

import cm.kfokam48.suivi.dto.ExerciceAvecNoteResponse;
import cm.kfokam48.suivi.dto.RelectureResponse;
import cm.kfokam48.suivi.service.ExerciceService;
import cm.kfokam48.suivi.service.RelectureService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Support des issues #8 et #28 : relectures assignées à un étudiant en
 * tant que relecteur (EF18), et ses propres exercices avec leur note finale
 * (EF17, EF19), pour alimenter les écrans étudiant et relecteur (F2).
 */
@RestController
@RequestMapping("/api/etudiants")
public class EtudiantController {

    private final RelectureService relectureService;
    private final ExerciceService exerciceService;

    public EtudiantController(RelectureService relectureService, ExerciceService exerciceService) {
        this.relectureService = relectureService;
        this.exerciceService = exerciceService;
    }

    @GetMapping("/{id}/relectures")
    public List<RelectureResponse> listerRelectures(@PathVariable Long id) {
        return relectureService.listerPourRelecteur(id).stream()
                .map(RelectureResponse::from)
                .toList();
    }

    @GetMapping("/{id}/exercices")
    public List<ExerciceAvecNoteResponse> listerExercices(@PathVariable Long id) {
        return exerciceService.listerExercicesEtudiant(id);
    }
}

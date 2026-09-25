package cm.kfokam48.suivi.controller;

import cm.kfokam48.suivi.dto.RelectureResponse;
import cm.kfokam48.suivi.service.RelectureService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Support de l'issue #8 : liste des relectures assignées à un étudiant
 * en tant que relecteur (EF18), pour alimenter l'écran relecteur (F2).
 */
@RestController
@RequestMapping("/api/etudiants")
public class EtudiantController {

    private final RelectureService relectureService;

    public EtudiantController(RelectureService relectureService) {
        this.relectureService = relectureService;
    }

    @GetMapping("/{id}/relectures")
    public List<RelectureResponse> listerRelectures(@PathVariable Long id) {
        return relectureService.listerPourRelecteur(id).stream()
                .map(RelectureResponse::from)
                .toList();
    }
}

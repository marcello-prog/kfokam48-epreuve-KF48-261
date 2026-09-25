package cm.kfokam48.suivi.controller;

import cm.kfokam48.suivi.dto.RendreRelectureRequest;
import cm.kfokam48.suivi.service.RelectureService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relectures")
public class RelectureController {

    private final RelectureService relectureService;

    public RelectureController(RelectureService relectureService) {
        this.relectureService = relectureService;
    }

    @PostMapping("/{id}")
    public void rendreRelecture(@PathVariable Long id, @Valid @RequestBody RendreRelectureRequest requete) {
        relectureService.rendreRelecture(id, requete);
    }
}

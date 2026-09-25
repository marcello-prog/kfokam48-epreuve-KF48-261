package cm.kfokam48.suivi.controller;

import cm.kfokam48.suivi.dto.MarquerPresenceRequest;
import cm.kfokam48.suivi.dto.PresenceResponse;
import cm.kfokam48.suivi.entity.Presence;
import cm.kfokam48.suivi.service.PresenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/presences")
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @PostMapping
    public ResponseEntity<PresenceResponse> marquerPresence(@Valid @RequestBody MarquerPresenceRequest requete) {
        Presence presence = presenceService.marquerPresence(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(PresenceResponse.from(presence));
    }
}

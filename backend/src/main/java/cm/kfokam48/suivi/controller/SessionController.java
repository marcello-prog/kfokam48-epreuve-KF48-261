package cm.kfokam48.suivi.controller;

import cm.kfokam48.suivi.dto.OuvrirSessionRequest;
import cm.kfokam48.suivi.dto.SessionResponse;
import cm.kfokam48.suivi.entity.Session;
import cm.kfokam48.suivi.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<SessionResponse> ouvrirSession(@Valid @RequestBody OuvrirSessionRequest requete) {
        Session session = sessionService.ouvrirSession(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(SessionResponse.from(session));
    }
}

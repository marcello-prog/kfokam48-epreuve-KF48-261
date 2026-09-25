package cm.kfokam48.suivi.dto;

import cm.kfokam48.suivi.entity.Presence;
import cm.kfokam48.suivi.entity.SourcePresence;

public record PresenceResponse(Long id, Long sessionId, Long etudiantId, SourcePresence source) {

    public static PresenceResponse from(Presence presence) {
        return new PresenceResponse(
                presence.getId(),
                presence.getSession().getId(),
                presence.getEtudiant().getId(),
                presence.getSource()
        );
    }
}

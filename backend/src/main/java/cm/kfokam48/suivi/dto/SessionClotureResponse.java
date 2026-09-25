package cm.kfokam48.suivi.dto;

import cm.kfokam48.suivi.entity.Session;
import cm.kfokam48.suivi.entity.StatutSession;

import java.time.Instant;

public record SessionClotureResponse(Long id, StatutSession statut, Instant clotureAt) {

    public static SessionClotureResponse from(Session session) {
        return new SessionClotureResponse(session.getId(), session.getStatut(), session.getClotureAt());
    }
}

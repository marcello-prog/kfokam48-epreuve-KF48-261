package cm.kfokam48.suivi.dto;

import cm.kfokam48.suivi.entity.Session;

import java.time.Instant;

public record SessionResponse(Long id, String code, Instant ouvertureAt, Instant expirationAt) {

    public static SessionResponse from(Session session) {
        return new SessionResponse(session.getId(), session.getCode(), session.getOuvertureAt(), session.getExpirationAt());
    }
}

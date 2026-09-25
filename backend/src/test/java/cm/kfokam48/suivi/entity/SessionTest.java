package cm.kfokam48.suivi.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test unitaire sur une règle métier réelle : RG1 — le code de présence
 * expire 15 minutes après l'ouverture de la session (contrainte B6).
 */
class SessionTest {

    @Test
    void uneSessionNestPasExpireeAvantSaDateDexpiration() {
        Instant ouvertureAt = Instant.now();
        Instant expirationAt = ouvertureAt.plus(15, ChronoUnit.MINUTES);
        Session session = new Session("Cours Java", null, null, "ABC123", ouvertureAt, expirationAt);

        Instant quatorzeMinutesApres = ouvertureAt.plus(14, ChronoUnit.MINUTES);

        assertThat(session.estExpiree(quatorzeMinutesApres)).isFalse();
    }

    @Test
    void uneSessionEstExpireeApresQuinzeMinutes() {
        Instant ouvertureAt = Instant.now();
        Instant expirationAt = ouvertureAt.plus(15, ChronoUnit.MINUTES);
        Session session = new Session("Cours Java", null, null, "ABC123", ouvertureAt, expirationAt);

        Instant seizeMinutesApres = ouvertureAt.plus(16, ChronoUnit.MINUTES);

        assertThat(session.estExpiree(seizeMinutesApres)).isTrue();
    }
}

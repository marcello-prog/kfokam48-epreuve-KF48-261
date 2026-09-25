package cm.kfokam48.suivi.service;

import cm.kfokam48.suivi.dto.OuvrirSessionRequest;
import cm.kfokam48.suivi.entity.Formateur;
import cm.kfokam48.suivi.entity.Promotion;
import cm.kfokam48.suivi.entity.Session;
import cm.kfokam48.suivi.exception.ApiException;
import cm.kfokam48.suivi.repository.FormateurRepository;
import cm.kfokam48.suivi.repository.PromotionRepository;
import cm.kfokam48.suivi.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

/**
 * EF1 · RG1 — ouverture d'une session et génération d'un code de présence
 * qui expire {@code expirationMinutes} après l'ouverture.
 */
@Service
public class SessionService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LONGUEUR_CODE = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SessionRepository sessionRepository;
    private final PromotionRepository promotionRepository;
    private final FormateurRepository formateurRepository;
    private final int expirationMinutes;

    public SessionService(SessionRepository sessionRepository,
                           PromotionRepository promotionRepository,
                           FormateurRepository formateurRepository,
                           @Value("${kfokam48.presence.expiration-minutes}") int expirationMinutes) {
        this.sessionRepository = sessionRepository;
        this.promotionRepository = promotionRepository;
        this.formateurRepository = formateurRepository;
        this.expirationMinutes = expirationMinutes;
    }

    public Session ouvrirSession(OuvrirSessionRequest requete) {
        Promotion promotion = promotionRepository.findById(requete.promotionId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "PROMOTION_INCONNUE",
                        "Aucune promotion ne correspond à cet identifiant."));

        Formateur formateur = formateurRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "AUCUN_FORMATEUR",
                        "Aucun formateur n'est enregistré."));

        Instant ouvertureAt = Instant.now();
        Instant expirationAt = ouvertureAt.plus(Duration.ofMinutes(expirationMinutes));

        Session session = new Session(requete.titre(), promotion, formateur, genererCodeUnique(),
                ouvertureAt, expirationAt);

        return sessionRepository.save(session);
    }

    private String genererCodeUnique() {
        String code;
        do {
            code = genererCode();
        } while (sessionRepository.existsByCode(code));
        return code;
    }

    private String genererCode() {
        StringBuilder sb = new StringBuilder(LONGUEUR_CODE);
        for (int i = 0; i < LONGUEUR_CODE; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}

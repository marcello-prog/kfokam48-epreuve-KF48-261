package cm.kfokam48.suivi.service;

import cm.kfokam48.suivi.dto.MarquerPresenceRequest;
import cm.kfokam48.suivi.entity.Etudiant;
import cm.kfokam48.suivi.entity.Presence;
import cm.kfokam48.suivi.entity.Session;
import cm.kfokam48.suivi.entity.SourcePresence;
import cm.kfokam48.suivi.exception.ApiException;
import cm.kfokam48.suivi.repository.EtudiantRepository;
import cm.kfokam48.suivi.repository.PresenceRepository;
import cm.kfokam48.suivi.repository.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * EF2-4 · RG1-3, RG15 — marquage de présence par code, avec ses cas
 * d'erreur (code inconnu, session clôturée, code expiré, déjà présent).
 */
@Service
public class PresenceService {

    private final SessionRepository sessionRepository;
    private final EtudiantRepository etudiantRepository;
    private final PresenceRepository presenceRepository;

    public PresenceService(SessionRepository sessionRepository,
                            EtudiantRepository etudiantRepository,
                            PresenceRepository presenceRepository) {
        this.sessionRepository = sessionRepository;
        this.etudiantRepository = etudiantRepository;
        this.presenceRepository = presenceRepository;
    }

    public Presence marquerPresence(MarquerPresenceRequest requete) {
        Session session = sessionRepository.findByCode(requete.code())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "CODE_INCONNU",
                        "Ce code de présence n'existe pas."));

        if (session.estCloturee()) {
            throw new ApiException(HttpStatus.CONFLICT, "SESSION_CLOTUREE",
                    "Cette session est clôturée, plus aucune présence n'est possible.");
        }

        if (session.estExpiree(Instant.now())) {
            throw new ApiException(HttpStatus.GONE, "CODE_EXPIRE", "Le code de présence a expiré.");
        }

        Etudiant etudiant = etudiantRepository.findById(requete.etudiantId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "ETUDIANT_INCONNU",
                        "Aucun étudiant ne correspond à cet identifiant."));

        if (presenceRepository.existsBySession_IdAndEtudiant_Id(session.getId(), etudiant.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "DEJA_PRESENT",
                    "Vous avez déjà marqué votre présence.");
        }

        Presence presence = new Presence(session, etudiant, SourcePresence.ETUDIANT, Instant.now());
        return presenceRepository.save(presence);
    }
}

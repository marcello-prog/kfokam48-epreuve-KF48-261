package cm.kfokam48.suivi.service;

import cm.kfokam48.suivi.dto.DeposerExerciceRequest;
import cm.kfokam48.suivi.entity.Etudiant;
import cm.kfokam48.suivi.entity.Exercice;
import cm.kfokam48.suivi.entity.Session;
import cm.kfokam48.suivi.exception.ApiException;
import cm.kfokam48.suivi.repository.EtudiantRepository;
import cm.kfokam48.suivi.repository.ExerciceRepository;
import cm.kfokam48.suivi.repository.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;

/**
 * EF7-8 · RG12 — dépôt du lien d'un exercice, jusqu'à la clôture de la
 * session (la vérification de clôture arrive avec l'issue #10).
 */
@Service
public class ExerciceService {

    private final SessionRepository sessionRepository;
    private final EtudiantRepository etudiantRepository;
    private final ExerciceRepository exerciceRepository;

    public ExerciceService(SessionRepository sessionRepository,
                            EtudiantRepository etudiantRepository,
                            ExerciceRepository exerciceRepository) {
        this.sessionRepository = sessionRepository;
        this.etudiantRepository = etudiantRepository;
        this.exerciceRepository = exerciceRepository;
    }

    public Exercice deposerExercice(DeposerExerciceRequest requete) {
        Session session = sessionRepository.findById(requete.sessionId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "SESSION_INCONNUE",
                        "Aucune session ne correspond à cet identifiant."));

        Etudiant etudiant = etudiantRepository.findById(requete.etudiantId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "ETUDIANT_INCONNU",
                        "Aucun étudiant ne correspond à cet identifiant."));

        validerLien(requete.lien());

        if (exerciceRepository.findBySession_IdAndEtudiant_Id(session.getId(), etudiant.getId()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "EXERCICE_DEJA_DEPOSE",
                    "Un exercice a déjà été déposé pour cette session.");
        }

        Exercice exercice = new Exercice(session, etudiant, requete.lien(), Instant.now());
        return exerciceRepository.save(exercice);
    }

    private void validerLien(String lien) {
        try {
            URI uri = new URI(lien);
            boolean schemeValide = "http".equals(uri.getScheme()) || "https".equals(uri.getScheme());
            if (!schemeValide || uri.getHost() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "LIEN_INVALIDE",
                        "Le lien doit être une URL http(s) valide.");
            }
        } catch (URISyntaxException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LIEN_INVALIDE", "Le lien doit être une URL http(s) valide.");
        }
    }
}

package cm.kfokam48.suivi.service;

import cm.kfokam48.suivi.dto.DeposerExerciceRequest;
import cm.kfokam48.suivi.dto.ExerciceAvecNoteResponse;
import cm.kfokam48.suivi.entity.Etudiant;
import cm.kfokam48.suivi.entity.Exercice;
import cm.kfokam48.suivi.entity.Relecture;
import cm.kfokam48.suivi.entity.Session;
import cm.kfokam48.suivi.exception.ApiException;
import cm.kfokam48.suivi.repository.EtudiantRepository;
import cm.kfokam48.suivi.repository.ExerciceRepository;
import cm.kfokam48.suivi.repository.RelectureRepository;
import cm.kfokam48.suivi.repository.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;

/**
 * EF7-8 · RG12, RG15 — dépôt du lien d'un exercice, jusqu'à la clôture de
 * la session.
 */
@Service
public class ExerciceService {

    private final SessionRepository sessionRepository;
    private final EtudiantRepository etudiantRepository;
    private final ExerciceRepository exerciceRepository;
    private final RelectureService relectureService;
    private final RelectureRepository relectureRepository;

    public ExerciceService(SessionRepository sessionRepository,
                            EtudiantRepository etudiantRepository,
                            ExerciceRepository exerciceRepository,
                            RelectureService relectureService,
                            RelectureRepository relectureRepository) {
        this.sessionRepository = sessionRepository;
        this.etudiantRepository = etudiantRepository;
        this.exerciceRepository = exerciceRepository;
        this.relectureService = relectureService;
        this.relectureRepository = relectureRepository;
    }

    public Exercice deposerExercice(DeposerExerciceRequest requete) {
        Session session = sessionRepository.findById(requete.sessionId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "SESSION_INCONNUE",
                        "Aucune session ne correspond à cet identifiant."));

        Etudiant etudiant = etudiantRepository.findById(requete.etudiantId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "ETUDIANT_INCONNU",
                        "Aucun étudiant ne correspond à cet identifiant."));

        if (session.estCloturee()) {
            throw new ApiException(HttpStatus.CONFLICT, "SESSION_CLOTUREE",
                    "Cette session est clôturée, aucun dépôt n'est plus possible.");
        }

        validerLien(requete.lien());

        if (exerciceRepository.findBySession_IdAndEtudiant_Id(session.getId(), etudiant.getId()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "EXERCICE_DEJA_DEPOSE",
                    "Un exercice a déjà été déposé pour cette session.");
        }

        Exercice exercice = new Exercice(session, etudiant, requete.lien(), Instant.now());
        exercice = exerciceRepository.save(exercice);
        relectureService.assignerRelecteurs(exercice);
        return exerciceRepository.save(exercice);
    }

    /**
     * EF17, EF19 · RG17 (étape 3) — exercices d'un étudiant avec leur note
     * finale (moyenne des relectures rendues, provisoire si toutes ne sont
     * pas encore rendues), sans identité des relecteurs (RG8).
     */
    public List<ExerciceAvecNoteResponse> listerExercicesEtudiant(Long etudiantId) {
        return exerciceRepository.findByEtudiant_Id(etudiantId).stream()
                .map(this::avecNote)
                .toList();
    }

    private ExerciceAvecNoteResponse avecNote(Exercice exercice) {
        List<Relecture> toutes = relectureRepository.findByExercice_Id(exercice.getId());
        List<Relecture> rendues = toutes.stream().filter(Relecture::dejaRendue).toList();

        Double note = rendues.isEmpty() ? null
                : rendues.stream().mapToInt(Relecture::getNote).average().orElseThrow();
        boolean provisoire = !rendues.isEmpty() && rendues.size() < toutes.size();
        List<String> commentaires = rendues.stream().map(Relecture::getCommentaire).toList();

        return new ExerciceAvecNoteResponse(exercice.getId(), exercice.getSession().getId(), exercice.getLien(),
                exercice.getStatut(), note, provisoire, commentaires);
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

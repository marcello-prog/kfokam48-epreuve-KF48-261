package cm.kfokam48.suivi.service;

import cm.kfokam48.suivi.dto.TableauLigne;
import cm.kfokam48.suivi.entity.Etudiant;
import cm.kfokam48.suivi.entity.Relecture;
import cm.kfokam48.suivi.entity.StatutRelecture;
import cm.kfokam48.suivi.exception.ApiException;
import cm.kfokam48.suivi.repository.EtudiantRepository;
import cm.kfokam48.suivi.repository.ExerciceRepository;
import cm.kfokam48.suivi.repository.PresenceRepository;
import cm.kfokam48.suivi.repository.PromotionRepository;
import cm.kfokam48.suivi.repository.RelectureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * EF16 · RG16 — tableau récapitulatif du formateur : présences, exercices
 * déposés, moyenne des notes reçues (relectures rendues uniquement — cf.
 * cahier des charges section 7), relectures encore en attente à faire.
 */
@Service
public class TableauService {

    private final PromotionRepository promotionRepository;
    private final EtudiantRepository etudiantRepository;
    private final PresenceRepository presenceRepository;
    private final ExerciceRepository exerciceRepository;
    private final RelectureRepository relectureRepository;

    public TableauService(PromotionRepository promotionRepository,
                           EtudiantRepository etudiantRepository,
                           PresenceRepository presenceRepository,
                           ExerciceRepository exerciceRepository,
                           RelectureRepository relectureRepository) {
        this.promotionRepository = promotionRepository;
        this.etudiantRepository = etudiantRepository;
        this.presenceRepository = presenceRepository;
        this.exerciceRepository = exerciceRepository;
        this.relectureRepository = relectureRepository;
    }

    public List<TableauLigne> genererTableau(Long promotionId) {
        if (!promotionRepository.existsById(promotionId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "PROMOTION_INCONNUE",
                    "Aucune promotion ne correspond à cet identifiant.");
        }

        return etudiantRepository.findByPromotionId(promotionId).stream()
                .map(this::ligne)
                .toList();
    }

    private TableauLigne ligne(Etudiant etudiant) {
        long presences = presenceRepository.countByEtudiant_Id(etudiant.getId());
        long exercicesDeposes = exerciceRepository.countByEtudiant_Id(etudiant.getId());

        // RG17 (étape 3) : la moyenne agrège une note finale PAR EXERCICE (moyenne
        // de ses relectures rendues), pas une par relecture individuelle — sinon un
        // exercice à deux relectures pèserait deux fois plus qu'un exercice à une.
        List<Relecture> relecturesRecues = relectureRepository
                .findByExercice_Etudiant_IdAndStatut(etudiant.getId(), StatutRelecture.RENDUE);
        Map<Long, List<Relecture>> parExercice = relecturesRecues.stream()
                .collect(Collectors.groupingBy(r -> r.getExercice().getId()));
        Collection<Double> notesFinalesParExercice = parExercice.values().stream()
                .map(rs -> rs.stream().mapToInt(Relecture::getNote).average().orElseThrow())
                .toList();
        Double moyenne = notesFinalesParExercice.isEmpty() ? null
                : notesFinalesParExercice.stream().mapToDouble(Double::doubleValue).average().orElseThrow();

        long relecturesEnAttente = relectureRepository
                .countByRelecteur_IdAndStatut(etudiant.getId(), StatutRelecture.EN_ATTENTE);

        return new TableauLigne(etudiant.getId(), etudiant.getNom(), presences, exercicesDeposes,
                moyenne, relecturesEnAttente);
    }
}

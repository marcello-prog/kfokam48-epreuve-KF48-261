package cm.kfokam48.suivi.service;

import cm.kfokam48.suivi.entity.Etudiant;
import cm.kfokam48.suivi.entity.Exercice;
import cm.kfokam48.suivi.entity.Presence;
import cm.kfokam48.suivi.entity.Relecture;
import cm.kfokam48.suivi.repository.PresenceRepository;
import cm.kfokam48.suivi.repository.RelectureRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

/**
 * EF10 · RG5-7 — assignation d'un relecteur choisi au hasard parmi les
 * étudiants présents à la session, à l'exclusion de l'auteur.
 */
@Service
public class RelectureService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final PresenceRepository presenceRepository;
    private final RelectureRepository relectureRepository;

    public RelectureService(PresenceRepository presenceRepository, RelectureRepository relectureRepository) {
        this.presenceRepository = presenceRepository;
        this.relectureRepository = relectureRepository;
    }

    /**
     * Tente d'assigner un relecteur à l'exercice. Si aucun candidat n'est
     * présent (hors auteur), l'exercice reste au statut DEPOSE — cf.
     * docs/CAHIER_DES_CHARGES.md section 7.
     */
    public Optional<Relecture> assignerRelecteur(Exercice exercice) {
        List<Etudiant> candidats = presenceRepository.findBySession_Id(exercice.getSession().getId()).stream()
                .map(Presence::getEtudiant)
                .filter(etudiant -> !etudiant.getId().equals(exercice.getEtudiant().getId()))
                .toList();

        if (candidats.isEmpty()) {
            return Optional.empty();
        }

        Etudiant relecteur = candidats.get(RANDOM.nextInt(candidats.size()));
        exercice.assignerRelecteur();
        Relecture relecture = new Relecture(exercice, relecteur);
        return Optional.of(relectureRepository.save(relecture));
    }
}

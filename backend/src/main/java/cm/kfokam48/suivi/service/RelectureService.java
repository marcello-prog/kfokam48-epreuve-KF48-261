package cm.kfokam48.suivi.service;

import cm.kfokam48.suivi.dto.RendreRelectureRequest;
import cm.kfokam48.suivi.entity.Etudiant;
import cm.kfokam48.suivi.entity.Exercice;
import cm.kfokam48.suivi.entity.Presence;
import cm.kfokam48.suivi.entity.Relecture;
import cm.kfokam48.suivi.entity.StatutRelecture;
import cm.kfokam48.suivi.exception.ApiException;
import cm.kfokam48.suivi.repository.ExerciceRepository;
import cm.kfokam48.suivi.repository.PresenceRepository;
import cm.kfokam48.suivi.repository.RelectureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * EF10 · RG5-7, RG7bis — assignation de deux relecteurs (un seul si un seul
 * candidat est disponible) choisis au hasard parmi les étudiants présents à
 * la session, à l'exclusion de l'auteur. RG6 révisée à l'étape 3 : un
 * exercice n'a plus un relecteur unique.
 */
@Service
public class RelectureService {

    private static final int NOMBRE_RELECTEURS = 2;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PresenceRepository presenceRepository;
    private final RelectureRepository relectureRepository;
    private final ExerciceRepository exerciceRepository;

    public RelectureService(PresenceRepository presenceRepository, RelectureRepository relectureRepository,
                             ExerciceRepository exerciceRepository) {
        this.presenceRepository = presenceRepository;
        this.relectureRepository = relectureRepository;
        this.exerciceRepository = exerciceRepository;
    }

    /**
     * Assigne jusqu'à deux relecteurs différents à l'exercice. Si aucun
     * candidat n'est présent (hors auteur), l'exercice reste au statut
     * DEPOSE — cf. docs/CAHIER_DES_CHARGES.md section 7. Si un seul candidat
     * est disponible, un seul relecteur est assigné (RG7bis).
     */
    public List<Relecture> assignerRelecteurs(Exercice exercice) {
        List<Etudiant> candidats = new ArrayList<>(
                presenceRepository.findBySession_Id(exercice.getSession().getId()).stream()
                        .map(Presence::getEtudiant)
                        .filter(etudiant -> !etudiant.getId().equals(exercice.getEtudiant().getId()))
                        .toList());

        if (candidats.isEmpty()) {
            return List.of();
        }

        int nombreAAssigner = Math.min(NOMBRE_RELECTEURS, candidats.size());
        List<Relecture> relectures = new ArrayList<>();
        for (int i = 0; i < nombreAAssigner; i++) {
            Etudiant relecteur = candidats.remove(RANDOM.nextInt(candidats.size()));
            relectures.add(relectureRepository.save(new Relecture(exercice, relecteur)));
        }

        exercice.assignerRelecteur();
        return relectures;
    }

    /**
     * EF11-13 · RG5, RG6, RG9 — un relecteur rend sa note et son commentaire.
     * Le contrat impose { note, commentaire } sans identité du relecteur
     * (pas d'authentification, Q1) : le contrôle "auto-relecture" (403) est
     * conservé pour la conformité au contrat mais ne peut structurellement
     * pas se déclencher, RG5 étant déjà appliquée à l'assignation (issue #7).
     * L'exercice ne passe RELU que lorsque toutes ses relectures assignées
     * sont rendues (RG17, étape 3).
     */
    public Relecture rendreRelecture(Long relectureId, RendreRelectureRequest requete) {
        Relecture relecture = relectureRepository.findById(relectureId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "RELECTURE_INCONNUE",
                        "Aucune relecture ne correspond à cet identifiant."));

        if (relecture.estAuteurDeExercice(relecture.getRelecteur())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "AUTO_RELECTURE",
                    "Un étudiant ne peut pas relire son propre exercice.");
        }

        if (relecture.dejaRendue()) {
            throw new ApiException(HttpStatus.CONFLICT, "RELECTURE_DEJA_RENDUE",
                    "Cette relecture a déjà été rendue.");
        }

        if (requete.note() < 0 || requete.note() > 20) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NOTE_INVALIDE",
                    "La note doit être un entier compris entre 0 et 20.");
        }

        relecture.rendre(requete.note(), requete.commentaire(), Instant.now());
        Relecture sauvegardee = relectureRepository.save(relecture);

        long enAttente = relectureRepository.countByExercice_IdAndStatut(
                relecture.getExercice().getId(), StatutRelecture.EN_ATTENTE);
        if (enAttente == 0) {
            Exercice exercice = relecture.getExercice();
            exercice.marquerRelu();
            exerciceRepository.save(exercice);
        }

        return sauvegardee;
    }

    public List<Relecture> listerPourRelecteur(Long etudiantId) {
        return relectureRepository.findByRelecteur_Id(etudiantId);
    }
}

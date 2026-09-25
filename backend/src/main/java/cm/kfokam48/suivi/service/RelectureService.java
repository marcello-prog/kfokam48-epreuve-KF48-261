package cm.kfokam48.suivi.service;

import cm.kfokam48.suivi.dto.RendreRelectureRequest;
import cm.kfokam48.suivi.entity.Etudiant;
import cm.kfokam48.suivi.entity.Exercice;
import cm.kfokam48.suivi.entity.Presence;
import cm.kfokam48.suivi.entity.Relecture;
import cm.kfokam48.suivi.exception.ApiException;
import cm.kfokam48.suivi.repository.PresenceRepository;
import cm.kfokam48.suivi.repository.RelectureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
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

    /**
     * EF11-13 · RG5, RG6, RG9 — un relecteur rend sa note et son commentaire.
     * Le contrat impose { note, commentaire } sans identité du relecteur
     * (pas d'authentification, Q1) : le contrôle "auto-relecture" (403) est
     * conservé pour la conformité au contrat mais ne peut structurellement
     * pas se déclencher, RG5 étant déjà appliquée à l'assignation (issue #7).
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
        relecture.getExercice().marquerRelu();
        return relectureRepository.save(relecture);
    }

    public List<Relecture> listerPourRelecteur(Long etudiantId) {
        return relectureRepository.findByRelecteur_Id(etudiantId);
    }
}

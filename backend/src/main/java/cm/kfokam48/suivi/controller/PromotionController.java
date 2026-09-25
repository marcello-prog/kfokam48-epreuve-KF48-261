package cm.kfokam48.suivi.controller;

import cm.kfokam48.suivi.dto.EtudiantResponse;
import cm.kfokam48.suivi.exception.ApiException;
import cm.kfokam48.suivi.repository.EtudiantRepository;
import cm.kfokam48.suivi.repository.PromotionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Support de l'issue #2 : liste des étudiants d'une promotion, pour que
 * l'étudiant choisisse son nom dans une liste (Q1 — pas de mot de passe).
 */
@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionRepository promotionRepository;
    private final EtudiantRepository etudiantRepository;

    public PromotionController(PromotionRepository promotionRepository, EtudiantRepository etudiantRepository) {
        this.promotionRepository = promotionRepository;
        this.etudiantRepository = etudiantRepository;
    }

    @GetMapping("/{id}/etudiants")
    public List<EtudiantResponse> listerEtudiants(@PathVariable Long id) {
        if (!promotionRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "PROMOTION_INCONNUE",
                    "Aucune promotion ne correspond à cet identifiant.");
        }
        return etudiantRepository.findByPromotionId(id).stream()
                .map(EtudiantResponse::from)
                .toList();
    }
}

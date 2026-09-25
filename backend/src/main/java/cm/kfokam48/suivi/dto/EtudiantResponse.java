package cm.kfokam48.suivi.dto;

import cm.kfokam48.suivi.entity.Etudiant;

public record EtudiantResponse(Long id, String nom) {

    public static EtudiantResponse from(Etudiant etudiant) {
        return new EtudiantResponse(etudiant.getId(), etudiant.getNom());
    }
}

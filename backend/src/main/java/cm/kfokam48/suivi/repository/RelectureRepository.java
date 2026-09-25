package cm.kfokam48.suivi.repository;

import cm.kfokam48.suivi.entity.Relecture;
import cm.kfokam48.suivi.entity.StatutRelecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    Optional<Relecture> findByExercice_Id(Long exerciceId);

    List<Relecture> findByRelecteur_Id(Long relecteurId);

    List<Relecture> findByExercice_Etudiant_IdAndStatut(Long etudiantId, StatutRelecture statut);

    long countByRelecteur_IdAndStatut(Long relecteurId, StatutRelecture statut);
}

package cm.kfokam48.suivi.repository;

import cm.kfokam48.suivi.entity.Exercice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    Optional<Exercice> findBySession_IdAndEtudiant_Id(Long sessionId, Long etudiantId);

    long countByEtudiant_Id(Long etudiantId);
}

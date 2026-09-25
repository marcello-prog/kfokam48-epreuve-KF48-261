package cm.kfokam48.suivi.repository;

import cm.kfokam48.suivi.entity.Relecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    Optional<Relecture> findByExercice_Id(Long exerciceId);
}

package cm.kfokam48.suivi.repository;

import cm.kfokam48.suivi.entity.Formateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FormateurRepository extends JpaRepository<Formateur, Long> {

    Optional<Formateur> findFirstByOrderByIdAsc();
}

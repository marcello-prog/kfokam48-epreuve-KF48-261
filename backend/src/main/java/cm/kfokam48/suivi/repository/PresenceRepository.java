package cm.kfokam48.suivi.repository;

import cm.kfokam48.suivi.entity.Presence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

    boolean existsBySession_IdAndEtudiant_Id(Long sessionId, Long etudiantId);
}

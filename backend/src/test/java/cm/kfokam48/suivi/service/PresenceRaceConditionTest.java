package cm.kfokam48.suivi.service;

import cm.kfokam48.suivi.dto.MarquerPresenceRequest;
import cm.kfokam48.suivi.entity.Etudiant;
import cm.kfokam48.suivi.entity.Formateur;
import cm.kfokam48.suivi.entity.Promotion;
import cm.kfokam48.suivi.entity.Session;
import cm.kfokam48.suivi.exception.ApiException;
import cm.kfokam48.suivi.repository.EtudiantRepository;
import cm.kfokam48.suivi.repository.FormateurRepository;
import cm.kfokam48.suivi.repository.PresenceRepository;
import cm.kfokam48.suivi.repository.PromotionRepository;
import cm.kfokam48.suivi.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Issue #25 — reproduit le bug rapporté par le client (étape 3) : deux
 * étudiants qui marquent leur présence "presque en même temps" doivent
 * chacun recevoir une réponse propre (une 201, une 409 DEJA_PRESENT),
 * jamais une exception brute non gérée (contrainte B4).
 */
@SpringBootTest
class PresenceRaceConditionTest {

    @Autowired
    private PresenceService presenceService;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private FormateurRepository formateurRepository;

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private PresenceRepository presenceRepository;

    @Test
    void deuxMarquagesConcurrentsDuMemeEtudiantNeDoiventJamaisLeverUneExceptionNonGeree() throws Exception {
        Promotion promotion = promotionRepository.findById(1L).orElseThrow();
        Formateur formateur = formateurRepository.findFirstByOrderByIdAsc().orElseThrow();
        Etudiant etudiant = etudiantRepository.save(new Etudiant("Etudiant Race Condition", promotion));
        Session session = sessionRepository.save(new Session("Session concurrence", promotion, formateur, "RACE01",
                Instant.now(), Instant.now().plus(Duration.ofMinutes(15))));

        MarquerPresenceRequest requete = new MarquerPresenceRequest("RACE01", etudiant.getId());

        int nbThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
        CountDownLatch pret = new CountDownLatch(nbThreads);
        CountDownLatch depart = new CountDownLatch(1);

        AtomicReference<Object> resultat1 = new AtomicReference<>();
        AtomicReference<Object> resultat2 = new AtomicReference<>();

        Runnable tache1 = () -> {
            pret.countDown();
            attendre(depart);
            resultat1.set(appelerEtCapturer(requete));
        };
        Runnable tache2 = () -> {
            pret.countDown();
            attendre(depart);
            resultat2.set(appelerEtCapturer(requete));
        };

        executor.submit(tache1);
        executor.submit(tache2);
        pret.await(5, TimeUnit.SECONDS);
        depart.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        List<Object> resultats = List.of(resultat1.get(), resultat2.get());

        // Chaque résultat doit être soit une présence créée, soit une ApiException
        // 409 DEJA_PRESENT — jamais une autre exception (ex. violation de
        // contrainte SQL brute non traduite).
        long succes = resultats.stream().filter(r -> !(r instanceof Throwable)).count();
        long conflitsPropres = resultats.stream()
                .filter(r -> r instanceof ApiException e && "DEJA_PRESENT".equals(e.getCode()))
                .count();
        long exceptionsNonGerees = resultats.stream()
                .filter(r -> r instanceof Throwable && !(r instanceof ApiException e && "DEJA_PRESENT".equals(e.getCode())))
                .count();

        assertThat(exceptionsNonGerees)
                .as("aucune exception non traduite en ApiException 409 DEJA_PRESENT ne doit fuiter")
                .isEqualTo(0);
        assertThat(succes).isEqualTo(1);
        assertThat(conflitsPropres).isEqualTo(1);
        assertThat(presenceRepository.existsBySession_IdAndEtudiant_Id(session.getId(), etudiant.getId())).isTrue();
    }

    private Object appelerEtCapturer(MarquerPresenceRequest requete) {
        try {
            return presenceService.marquerPresence(requete);
        } catch (Throwable t) {
            return t;
        }
    }

    private void attendre(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

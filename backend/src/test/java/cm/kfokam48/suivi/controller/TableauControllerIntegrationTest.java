package cm.kfokam48.suivi.controller;

import cm.kfokam48.suivi.entity.Etudiant;
import cm.kfokam48.suivi.entity.Exercice;
import cm.kfokam48.suivi.entity.Formateur;
import cm.kfokam48.suivi.entity.Presence;
import cm.kfokam48.suivi.entity.Promotion;
import cm.kfokam48.suivi.entity.Relecture;
import cm.kfokam48.suivi.entity.Session;
import cm.kfokam48.suivi.entity.SourcePresence;
import cm.kfokam48.suivi.repository.EtudiantRepository;
import cm.kfokam48.suivi.repository.ExerciceRepository;
import cm.kfokam48.suivi.repository.FormateurRepository;
import cm.kfokam48.suivi.repository.PresenceRepository;
import cm.kfokam48.suivi.repository.PromotionRepository;
import cm.kfokam48.suivi.repository.RelectureRepository;
import cm.kfokam48.suivi.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration sur GET /api/tableau (contrainte B6) : agrégation
 * présences/dépôts/moyenne/relectures en attente (EF16, RG16).
 */
@SpringBootTest
@AutoConfigureMockMvc
class TableauControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private FormateurRepository formateurRepository;

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PresenceRepository presenceRepository;

    @Autowired
    private ExerciceRepository exerciceRepository;

    @Autowired
    private RelectureRepository relectureRepository;

    @Test
    void leTableauAgregeLesDonneesDunEtudiant() throws Exception {
        // Étudiants dédiés à ce test (et non réutilisés depuis les autres
        // classes de test) pour que les compteurs partent bien de zéro —
        // la base H2 est partagée sur toute l'exécution des tests.
        Promotion promotion = promotionRepository.findById(1L).orElseThrow();
        Formateur formateur = formateurRepository.findFirstByOrderByIdAsc().orElseThrow();
        Etudiant auteur = etudiantRepository.save(new Etudiant("Auteur Tableau", promotion));
        Etudiant relecteur = etudiantRepository.save(new Etudiant("Relecteur Tableau", promotion));

        Session session = sessionRepository.save(new Session("Session tableau", promotion, formateur, "TB0001",
                Instant.now(), Instant.now().plus(Duration.ofMinutes(15))));
        presenceRepository.save(new Presence(session, auteur, SourcePresence.ETUDIANT, Instant.now()));

        Exercice exercice = exerciceRepository.save(new Exercice(session, auteur, "https://exemple.com", Instant.now()));
        Relecture relecture = new Relecture(exercice, relecteur);
        relecture.rendre(18, "Excellent", Instant.now());
        relectureRepository.save(relecture);

        mockMvc.perform(get("/api/tableau").param("promotionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.etudiantId == " + auteur.getId() + ")].presences").value(1))
                .andExpect(jsonPath("$[?(@.etudiantId == " + auteur.getId() + ")].exercicesDeposes").value(1))
                .andExpect(jsonPath("$[?(@.etudiantId == " + auteur.getId() + ")].moyenne").value(18.0));
    }

    @Test
    void unePromotionInconnueRetourne404() throws Exception {
        mockMvc.perform(get("/api/tableau").param("promotionId", "999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }
}

package cm.kfokam48.suivi.controller;

import cm.kfokam48.suivi.entity.Formateur;
import cm.kfokam48.suivi.entity.Promotion;
import cm.kfokam48.suivi.entity.Session;
import cm.kfokam48.suivi.repository.EtudiantRepository;
import cm.kfokam48.suivi.repository.FormateurRepository;
import cm.kfokam48.suivi.repository.PromotionRepository;
import cm.kfokam48.suivi.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration sur POST /api/exercices (contrainte B6) : cas
 * nominal, lien invalide (EF8) et dépôt en double (EF7).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ExerciceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private FormateurRepository formateurRepository;

    @Autowired
    private EtudiantRepository etudiantRepository;

    private Long creerSessionEtRenvoyerId(String code) {
        Promotion promotion = promotionRepository.findById(1L).orElseThrow();
        Formateur formateur = formateurRepository.findFirstByOrderByIdAsc().orElseThrow();
        Session session = sessionRepository.save(new Session("Session de test", promotion, formateur, code,
                Instant.now(), Instant.now().plus(Duration.ofMinutes(15))));
        return session.getId();
    }

    @Test
    void deposerUnExerciceAvecUnLienValideRetourne201() throws Exception {
        Long sessionId = creerSessionEtRenvoyerId("EX0001");
        Long etudiantId = etudiantRepository.findByPromotionId(1L).get(0).getId();

        mockMvc.perform(post("/api/exercices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"sessionId\": " + sessionId + ", \"etudiantId\": " + etudiantId
                                + ", \"lien\": \"https://github.com/exemple/exercice\" }"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("DEPOSE"));
    }

    @Test
    void unLienInvalideRetourne400() throws Exception {
        Long sessionId = creerSessionEtRenvoyerId("EX0002");
        Long etudiantId = etudiantRepository.findByPromotionId(1L).get(0).getId();

        mockMvc.perform(post("/api/exercices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"sessionId\": " + sessionId + ", \"etudiantId\": " + etudiantId
                                + ", \"lien\": \"pas-une-url\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LIEN_INVALIDE"));
    }

    @Test
    void unDepotEnDoubleRetourne409() throws Exception {
        Long sessionId = creerSessionEtRenvoyerId("EX0003");
        Long etudiantId = etudiantRepository.findByPromotionId(1L).get(0).getId();
        String corps = "{ \"sessionId\": " + sessionId + ", \"etudiantId\": " + etudiantId
                + ", \"lien\": \"https://github.com/exemple/exercice\" }";

        mockMvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EXERCICE_DEJA_DEPOSE"));
    }
}

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration sur PATCH /api/sessions/{id}/cloture (contrainte B6) :
 * clôture nominale, clôture en double, verrouillage du dépôt d'exercice
 * après clôture (EF15, RG15).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ClotureSessionIntegrationTest {

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
        Session session = sessionRepository.save(new Session("Session cloture", promotion, formateur, code,
                Instant.now(), Instant.now().plus(Duration.ofMinutes(15))));
        return session.getId();
    }

    @Test
    void clotureUneSessionOuverteRetourne200() throws Exception {
        Long sessionId = creerSessionEtRenvoyerId("CL0001");

        mockMvc.perform(patch("/api/sessions/" + sessionId + "/cloture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("CLOTUREE"))
                .andExpect(jsonPath("$.clotureAt").exists());
    }

    @Test
    void clotureUneSessionDejaClotureeRetourne409() throws Exception {
        Long sessionId = creerSessionEtRenvoyerId("CL0002");

        mockMvc.perform(patch("/api/sessions/" + sessionId + "/cloture")).andExpect(status().isOk());

        mockMvc.perform(patch("/api/sessions/" + sessionId + "/cloture"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_DEJA_CLOTUREE"));
    }

    @Test
    void deposerUnExerciceApresClotureRetourne409() throws Exception {
        Long sessionId = creerSessionEtRenvoyerId("CL0003");
        Long etudiantId = etudiantRepository.findByPromotionId(1L).get(0).getId();

        mockMvc.perform(patch("/api/sessions/" + sessionId + "/cloture")).andExpect(status().isOk());

        mockMvc.perform(post("/api/exercices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"sessionId\": " + sessionId + ", \"etudiantId\": " + etudiantId
                                + ", \"lien\": \"https://github.com/exemple/exercice\" }"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
    }

    @Test
    void uneSessionInconnueRetourne404() throws Exception {
        mockMvc.perform(patch("/api/sessions/999999/cloture"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SESSION_INCONNUE"));
    }
}

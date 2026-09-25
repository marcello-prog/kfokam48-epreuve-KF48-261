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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration sur l'endpoint POST /api/presences (contrainte B6) :
 * cas nominal et les deux cas d'erreur imposés par le sujet — code expiré
 * et étudiant déjà présent (D3).
 */
@SpringBootTest
@AutoConfigureMockMvc
class PresenceControllerIntegrationTest {

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

    private Session creerSession(Instant ouvertureAt, Instant expirationAt, String code) {
        Promotion promotion = promotionRepository.findById(1L).orElseThrow();
        Formateur formateur = formateurRepository.findFirstByOrderByIdAsc().orElseThrow();
        return sessionRepository.save(new Session("Session de test", promotion, formateur, code, ouvertureAt, expirationAt));
    }

    @Test
    void marquerSaPresenceAvecUnCodeValideRetourne201() throws Exception {
        creerSession(Instant.now(), Instant.now().plus(Duration.ofMinutes(15)), "OK1234");
        Long etudiantId = etudiantRepository.findByPromotionId(1L).get(0).getId();

        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"code\": \"OK1234\", \"etudiantId\": " + etudiantId + " }"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("ETUDIANT"));
    }

    @Test
    void unCodeExpireRetourne410() throws Exception {
        creerSession(Instant.now().minus(Duration.ofMinutes(20)), Instant.now().minus(Duration.ofMinutes(5)), "EXP123");
        Long etudiantId = etudiantRepository.findByPromotionId(1L).get(0).getId();

        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"code\": \"EXP123\", \"etudiantId\": " + etudiantId + " }"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("CODE_EXPIRE"));
    }

    @Test
    void unePresenceEnDoubleRetourne409() throws Exception {
        creerSession(Instant.now(), Instant.now().plus(Duration.ofMinutes(15)), "DUP123");
        Long etudiantId = etudiantRepository.findByPromotionId(1L).get(0).getId();
        String corps = "{ \"code\": \"DUP123\", \"etudiantId\": " + etudiantId + " }";

        mockMvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEJA_PRESENT"));
    }
}

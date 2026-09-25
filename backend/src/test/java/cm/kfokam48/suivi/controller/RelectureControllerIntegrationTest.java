package cm.kfokam48.suivi.controller;

import cm.kfokam48.suivi.entity.Etudiant;
import cm.kfokam48.suivi.entity.Exercice;
import cm.kfokam48.suivi.entity.Formateur;
import cm.kfokam48.suivi.entity.Presence;
import cm.kfokam48.suivi.entity.Promotion;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration sur POST /api/relectures/{id} (contrainte B6) :
 * cas nominal, note invalide (EF13), relecture déjà rendue (EF11).
 */
@SpringBootTest
@AutoConfigureMockMvc
class RelectureControllerIntegrationTest {

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

    private Long creerRelectureEtRenvoyerId(String code) {
        Promotion promotion = promotionRepository.findById(1L).orElseThrow();
        Formateur formateur = formateurRepository.findFirstByOrderByIdAsc().orElseThrow();
        Etudiant auteur = etudiantRepository.findByPromotionId(1L).get(0);
        Etudiant relecteurAttendu = etudiantRepository.findByPromotionId(1L).get(1);

        Session session = sessionRepository.save(new Session("Session relecture", promotion, formateur, code,
                Instant.now(), Instant.now().plus(Duration.ofMinutes(15))));
        presenceRepository.save(new Presence(session, auteur, SourcePresence.ETUDIANT, Instant.now()));
        presenceRepository.save(new Presence(session, relecteurAttendu, SourcePresence.ETUDIANT, Instant.now()));

        Exercice exercice = exerciceRepository.save(new Exercice(session, auteur, "https://exemple.com", Instant.now()));
        return relectureRepository.save(new cm.kfokam48.suivi.entity.Relecture(exercice, relecteurAttendu)).getId();
    }

    @Test
    void rendreUneRelectureAvecUneNoteValideRetourne200() throws Exception {
        Long relectureId = creerRelectureEtRenvoyerId("RL0001");

        mockMvc.perform(post("/api/relectures/" + relectureId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"note\": 15, \"commentaire\": \"Bon travail\" }"))
                .andExpect(status().isOk());

        assertThat(relectureRepository.findById(relectureId).orElseThrow().getStatut().name()).isEqualTo("RENDUE");
    }

    @Test
    void uneNoteHorsBornesRetourne400() throws Exception {
        Long relectureId = creerRelectureEtRenvoyerId("RL0002");

        mockMvc.perform(post("/api/relectures/" + relectureId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"note\": 25, \"commentaire\": \"Trop haut\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));
    }

    @Test
    void uneRelectureDejaRenduRetourne409() throws Exception {
        Long relectureId = creerRelectureEtRenvoyerId("RL0003");
        String corps = "{ \"note\": 12, \"commentaire\": \"ok\" }";

        mockMvc.perform(post("/api/relectures/" + relectureId).contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/relectures/" + relectureId).contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RELECTURE_DEJA_RENDUE"));
    }

    @Test
    void listerLesRelecturesDunEtudiantLesRenvoieToutes() throws Exception {
        Long relectureId = creerRelectureEtRenvoyerId("RL0004");
        Long relecteurId = relectureRepository.findById(relectureId).orElseThrow().getRelecteur().getId();

        String reponse = mockMvc.perform(get("/api/etudiants/" + relecteurId + "/relectures"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(reponse).contains("\"id\":" + relectureId).contains("\"statut\":\"EN_ATTENTE\"");
    }
}

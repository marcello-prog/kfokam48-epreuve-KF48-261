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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issues #27, #28 — étape 3 (RG6 révisée, RG17) : deux relecteurs assignés,
 * note finale = moyenne des relectures rendues, provisoire si une seule sur
 * deux, exercice RELU seulement quand toutes sont rendues.
 */
@SpringBootTest
@AutoConfigureMockMvc
class NoteFinaleExerciceIntegrationTest {

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
    void laNoteEstProvisoirePuisDefinitiveQuandLesDeuxRelecturesSontRendues() throws Exception {
        Promotion promotion = promotionRepository.findById(1L).orElseThrow();
        Formateur formateur = formateurRepository.findFirstByOrderByIdAsc().orElseThrow();
        Etudiant auteur = etudiantRepository.save(new Etudiant("Auteur NoteFinale", promotion));
        Etudiant relecteurA = etudiantRepository.save(new Etudiant("Relecteur A", promotion));
        Etudiant relecteurB = etudiantRepository.save(new Etudiant("Relecteur B", promotion));

        Session session = sessionRepository.save(new Session("Session note finale", promotion, formateur, "NF0001",
                Instant.now(), Instant.now().plus(Duration.ofMinutes(15))));
        presenceRepository.save(new Presence(session, auteur, SourcePresence.ETUDIANT, Instant.now()));
        presenceRepository.save(new Presence(session, relecteurA, SourcePresence.ETUDIANT, Instant.now()));
        presenceRepository.save(new Presence(session, relecteurB, SourcePresence.ETUDIANT, Instant.now()));

        mockMvc.perform(post("/api/exercices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"sessionId\": " + session.getId() + ", \"etudiantId\": " + auteur.getId()
                                + ", \"lien\": \"https://exemple.com/exo\" }"))
                .andExpect(status().isCreated());

        Exercice exercice = exerciceRepository.findBySession_IdAndEtudiant_Id(session.getId(), auteur.getId())
                .orElseThrow();
        var relectures = relectureRepository.findByExercice_Id(exercice.getId());
        org.assertj.core.api.Assertions.assertThat(relectures).hasSize(2);

        // Une seule des deux rend sa note -> provisoire.
        mockMvc.perform(post("/api/relectures/" + relectures.get(0).getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"note\": 10, \"commentaire\": \"correct\" }"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/etudiants/" + auteur.getId() + "/exercices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].note").value(10.0))
                .andExpect(jsonPath("$[0].provisoire").value(true))
                .andExpect(jsonPath("$[0].statut").value("EN_ATTENTE_RELECTURE"));

        // La seconde rend sa note -> moyenne définitive, exercice RELU.
        mockMvc.perform(post("/api/relectures/" + relectures.get(1).getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"note\": 16, \"commentaire\": \"bien aussi\" }"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/etudiants/" + auteur.getId() + "/exercices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].note").value(13.0))
                .andExpect(jsonPath("$[0].provisoire").value(false))
                .andExpect(jsonPath("$[0].statut").value("RELU"))
                .andExpect(jsonPath("$[0].commentaires.length()").value(2));
    }
}

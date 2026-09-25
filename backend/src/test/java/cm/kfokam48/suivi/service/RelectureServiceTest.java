package cm.kfokam48.suivi.service;

import cm.kfokam48.suivi.entity.Etudiant;
import cm.kfokam48.suivi.entity.Exercice;
import cm.kfokam48.suivi.entity.Presence;
import cm.kfokam48.suivi.entity.Promotion;
import cm.kfokam48.suivi.entity.Relecture;
import cm.kfokam48.suivi.entity.Session;
import cm.kfokam48.suivi.entity.SourcePresence;
import cm.kfokam48.suivi.entity.StatutExercice;
import cm.kfokam48.suivi.repository.ExerciceRepository;
import cm.kfokam48.suivi.repository.PresenceRepository;
import cm.kfokam48.suivi.repository.RelectureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Test unitaire sur une règle métier réelle : RG5 (interdiction
 * d'auto-relecture), RG7 (relecteurs choisis parmi les présents) et RG7bis
 * (repli à un seul relecteur si un seul candidat, étape 3), contrainte B6.
 */
@ExtendWith(MockitoExtension.class)
class RelectureServiceTest {

    @Mock
    private PresenceRepository presenceRepository;

    @Mock
    private RelectureRepository relectureRepository;

    @Mock
    private ExerciceRepository exerciceRepository;

    private Etudiant creerEtudiant(long id, String nom, Promotion promotion) {
        Etudiant etudiant = new Etudiant(nom, promotion);
        ReflectionTestUtils.setField(etudiant, "id", id);
        return etudiant;
    }

    private Presence creerPresence(Session session, Etudiant etudiant) {
        return new Presence(session, etudiant, SourcePresence.ETUDIANT, Instant.now());
    }

    @Test
    void neChoisitJamaisLauteurCommeRelecteur() {
        RelectureService service = new RelectureService(presenceRepository, relectureRepository, exerciceRepository);
        Promotion promotion = new Promotion("Promo test");
        Session session = new Session("S", promotion, null, "CODE01", Instant.now(), Instant.now().plusSeconds(900));

        Etudiant auteur = creerEtudiant(1L, "Auteur", promotion);
        Exercice exercice = new Exercice(session, auteur, "https://exemple.com", Instant.now());

        when(presenceRepository.findBySession_Id(any())).thenReturn(List.of(creerPresence(session, auteur)));

        List<Relecture> resultat = service.assignerRelecteurs(exercice);

        assertThat(resultat).isEmpty();
        assertThat(exercice.getStatut()).isEqualTo(StatutExercice.DEPOSE);
    }

    @Test
    void assigneUnSeulRelecteurSiUnSeulCandidatPresent() {
        RelectureService service = new RelectureService(presenceRepository, relectureRepository, exerciceRepository);
        Promotion promotion = new Promotion("Promo test");
        Session session = new Session("S", promotion, null, "CODE02", Instant.now(), Instant.now().plusSeconds(900));

        Etudiant auteur = creerEtudiant(1L, "Auteur", promotion);
        Etudiant autre = creerEtudiant(2L, "Autre", promotion);
        Exercice exercice = new Exercice(session, auteur, "https://exemple.com", Instant.now());

        when(presenceRepository.findBySession_Id(any()))
                .thenReturn(List.of(creerPresence(session, auteur), creerPresence(session, autre)));
        when(relectureRepository.save(any(Relecture.class))).thenAnswer(i -> i.getArgument(0));

        List<Relecture> resultat = service.assignerRelecteurs(exercice);

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getRelecteur()).isEqualTo(autre);
        assertThat(exercice.getStatut()).isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);
    }

    @Test
    void assigneDeuxRelecteursDifferentsSiAuMoinsDeuxCandidatsPresents() {
        RelectureService service = new RelectureService(presenceRepository, relectureRepository, exerciceRepository);
        Promotion promotion = new Promotion("Promo test");
        Session session = new Session("S", promotion, null, "CODE03", Instant.now(), Instant.now().plusSeconds(900));

        Etudiant auteur = creerEtudiant(1L, "Auteur", promotion);
        Etudiant candidatA = creerEtudiant(2L, "Candidat A", promotion);
        Etudiant candidatB = creerEtudiant(3L, "Candidat B", promotion);
        Exercice exercice = new Exercice(session, auteur, "https://exemple.com", Instant.now());

        when(presenceRepository.findBySession_Id(any())).thenReturn(List.of(
                creerPresence(session, auteur), creerPresence(session, candidatA), creerPresence(session, candidatB)));
        when(relectureRepository.save(any(Relecture.class))).thenAnswer(i -> i.getArgument(0));

        List<Relecture> resultat = service.assignerRelecteurs(exercice);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getRelecteur()).isNotEqualTo(resultat.get(1).getRelecteur());
        assertThat(resultat).extracting(Relecture::getRelecteur).allMatch(r -> !r.equals(auteur));
        assertThat(exercice.getStatut()).isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);
    }
}

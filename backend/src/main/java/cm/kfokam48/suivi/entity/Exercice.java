package cm.kfokam48.suivi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "exercice")
public class Exercice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    private String lien;

    @Enumerated(EnumType.STRING)
    private StatutExercice statut;

    private Instant deposeAt;

    protected Exercice() {
    }

    public Exercice(Session session, Etudiant etudiant, String lien, Instant deposeAt) {
        this.session = session;
        this.etudiant = etudiant;
        this.lien = lien;
        this.statut = StatutExercice.DEPOSE;
        this.deposeAt = deposeAt;
    }

    public Long getId() {
        return id;
    }

    public Session getSession() {
        return session;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public String getLien() {
        return lien;
    }

    public StatutExercice getStatut() {
        return statut;
    }

    public Instant getDeposeAt() {
        return deposeAt;
    }

    public boolean relecteurAssigne() {
        return statut != StatutExercice.DEPOSE;
    }

    public void remplacerLien(String nouveauLien) {
        this.lien = nouveauLien;
    }

    public void assignerRelecteur() {
        this.statut = StatutExercice.EN_ATTENTE_RELECTURE;
    }
}

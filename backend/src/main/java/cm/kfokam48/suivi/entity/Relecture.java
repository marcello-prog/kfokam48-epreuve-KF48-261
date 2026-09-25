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
@Table(name = "relecture")
public class Relecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exercice_id", nullable = false)
    private Exercice exercice;

    @ManyToOne
    @JoinColumn(name = "relecteur_id", nullable = false)
    private Etudiant relecteur;

    private Integer note;

    private String commentaire;

    @Enumerated(EnumType.STRING)
    private StatutRelecture statut;

    private Instant rendueAt;

    protected Relecture() {
    }

    public Relecture(Exercice exercice, Etudiant relecteur) {
        this.exercice = exercice;
        this.relecteur = relecteur;
        this.statut = StatutRelecture.EN_ATTENTE;
    }

    public Long getId() {
        return id;
    }

    public Exercice getExercice() {
        return exercice;
    }

    public Etudiant getRelecteur() {
        return relecteur;
    }

    public Integer getNote() {
        return note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public StatutRelecture getStatut() {
        return statut;
    }

    public Instant getRendueAt() {
        return rendueAt;
    }

    public boolean estAuteurDeExercice(Etudiant candidat) {
        return exercice.getEtudiant().getId().equals(candidat.getId());
    }

    public boolean dejaRendue() {
        return statut == StatutRelecture.RENDUE;
    }

    public void rendre(Integer note, String commentaire, Instant rendueAt) {
        this.note = note;
        this.commentaire = commentaire;
        this.statut = StatutRelecture.RENDUE;
        this.rendueAt = rendueAt;
    }
}

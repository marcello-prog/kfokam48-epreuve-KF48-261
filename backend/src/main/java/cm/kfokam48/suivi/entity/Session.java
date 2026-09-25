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
@Table(name = "session_cours")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @ManyToOne
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @ManyToOne
    @JoinColumn(name = "formateur_id", nullable = false)
    private Formateur formateur;

    private String code;

    private Instant ouvertureAt;

    private Instant expirationAt;

    private Instant clotureAt;

    @Enumerated(EnumType.STRING)
    private StatutSession statut;

    protected Session() {
    }

    public Session(String titre, Promotion promotion, Formateur formateur, String code,
                   Instant ouvertureAt, Instant expirationAt) {
        this.titre = titre;
        this.promotion = promotion;
        this.formateur = formateur;
        this.code = code;
        this.ouvertureAt = ouvertureAt;
        this.expirationAt = expirationAt;
        this.statut = StatutSession.OUVERTE;
    }

    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public Formateur getFormateur() {
        return formateur;
    }

    public String getCode() {
        return code;
    }

    public Instant getOuvertureAt() {
        return ouvertureAt;
    }

    public Instant getExpirationAt() {
        return expirationAt;
    }

    public Instant getClotureAt() {
        return clotureAt;
    }

    public StatutSession getStatut() {
        return statut;
    }

    public boolean estExpiree(Instant maintenant) {
        return maintenant.isAfter(expirationAt);
    }

    public boolean estCloturee() {
        return statut == StatutSession.CLOTUREE;
    }

    public void cloturer(Instant maintenant) {
        this.statut = StatutSession.CLOTUREE;
        this.clotureAt = maintenant;
    }
}

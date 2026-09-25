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
@Table(name = "presence")
public class Presence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @Enumerated(EnumType.STRING)
    private SourcePresence source;

    private Instant horodatage;

    protected Presence() {
    }

    public Presence(Session session, Etudiant etudiant, SourcePresence source, Instant horodatage) {
        this.session = session;
        this.etudiant = etudiant;
        this.source = source;
        this.horodatage = horodatage;
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

    public SourcePresence getSource() {
        return source;
    }

    public Instant getHorodatage() {
        return horodatage;
    }
}

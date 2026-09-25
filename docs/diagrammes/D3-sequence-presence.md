# D3 — Séquence : marquer sa présence

Cas nominal et trois cas d'erreur (dont les deux imposés : code expiré et étudiant déjà présent), cohérents avec les codes HTTP et le format d'erreur imposés par `api/contrat.yaml` (Annexe B).

```mermaid
sequenceDiagram
    participant E as Étudiant
    participant F as Front (écran étudiant)
    participant API as PresenceController
    participant S as PresenceService
    participant DB as Base de données

    E->>F: saisit le code
    F->>API: POST /api/presences { code, etudiantId }
    API->>S: enregistrer(code, etudiantId)
    S->>DB: rechercher la session par code

    alt code inconnu
        DB-->>S: aucune session trouvée
        S-->>API: CodeInconnuException
        API-->>F: 400 { "code": "CODE_INCONNU", "message": "Ce code de présence n'existe pas." }
        F-->>E: affiche l'erreur
    else code expiré (RG1, RG2)
        DB-->>S: session trouvée, expirationAt dépassé
        S-->>API: CodeExpireException
        API-->>F: 410 { "code": "CODE_EXPIRE", "message": "Le code de présence a expiré." }
        F-->>E: affiche l'erreur
    else déjà présent (RG3)
        DB-->>S: session trouvée, présence déjà enregistrée pour cet étudiant
        S-->>API: DejaPresentException
        API-->>F: 409 { "code": "DEJA_PRESENT", "message": "Vous avez déjà marqué votre présence." }
        F-->>E: affiche l'erreur
    else cas nominal
        DB-->>S: session trouvée, valide, pas de présence existante
        S->>DB: créer Presence { sessionId, etudiantId, source: ETUDIANT }
        DB-->>S: Presence créée
        S-->>API: Presence
        API-->>F: 201 { id, sessionId, etudiantId, source }
        F-->>E: confirmation "présence enregistrée"
    end
```

## Note — RG4 (blocage après 5 erreurs)

Le blocage de 2 minutes après 5 codes invalides consécutifs (RG4) est un compteur tenu côté `PresenceService` par étudiant + session, hors du diagramme ci-dessus pour ne pas surcharger le cas nominal. Il se traduit par une erreur `429 { "code": "TROP_DE_TENTATIVES", "message": "..." }` tant que le blocage est actif, avant même la recherche du code.

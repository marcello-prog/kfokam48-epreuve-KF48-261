# D4 (bonus) — États-transitions du cycle de vie d'un exercice

```mermaid
stateDiagram-v2
    [*] --> DEPOSE : POST /api/exercices (EF7)

    DEPOSE --> DEPOSE : PUT /api/exercices/{id} — remplacement du lien (RG13, EF9)
    DEPOSE --> EN_ATTENTE_RELECTURE : relecteur assigné automatiquement (RG7, EF10)
    DEPOSE --> DEPOSE : aucun étudiant présent hors auteur — reste sans relecteur (cf. section 7 du cahier des charges)

    EN_ATTENTE_RELECTURE --> RELU : POST /api/relectures/{id} — première relecture rendue (EF11)

    RELU --> RELU : PATCH /api/relectures/{id} — correction (RG10, EF14), tant que la session n'est pas clôturée

    DEPOSE --> [*] : session clôturée (RG15) — état figé, plus de relecteur assignable
    EN_ATTENTE_RELECTURE --> [*] : session clôturée (RG15) — reste "en attente" (RG11), visible comme tel dans le tableau
    RELU --> [*] : session clôturée (RG15) — relecture définitive
```

Les trois transitions vers `[*]` en bas ne suppriment rien : elles marquent que la clôture de session (RG15) fige l'état atteint, quel qu'il soit — y compris `EN_ATTENTE_RELECTURE`, qui reste alors visible au formateur comme "en attente" sans jamais être relu (RG11).

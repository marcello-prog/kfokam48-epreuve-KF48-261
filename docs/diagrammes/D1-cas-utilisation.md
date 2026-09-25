# D1 — Cas d'utilisation

Acteurs et ce que chacun peut faire. Le rôle "Relecteur" est porté par un étudiant présent à la session (cf. cahier des charges, section 2) : il n'a pas de compte séparé, c'est pourquoi il hérite des cas d'utilisation de l'Étudiant.

Mermaid ne propose pas de notation UML "cas d'utilisation" native ; on la représente ici en flowchart : les acteurs à gauche/droite, les cas d'utilisation au centre (bulles), une ligne = une association. Les cas d'utilisation du Système (assignation, génération de code, calcul de moyenne) sont automatiques, déclenchés par les actions des autres acteurs.

```mermaid
flowchart LR
    Formateur((Formateur))
    Etudiant((Étudiant))
    Relecteur((Relecteur))
    Systeme((Système))

    Etudiant -. porte le rôle .-> Relecteur

    subgraph CU["Cas d'utilisation"]
        UC1([Ouvrir une session — EF1])
        UC2([Ajouter une présence manuelle — EF6])
        UC3([Clôturer une session — EF15])
        UC4([Consulter le tableau de bord — EF16])
        UC5([Marquer sa présence — EF2])
        UC6([Déposer le lien d'un exercice — EF7])
        UC7([Remplacer le lien d'un exercice — EF9])
        UC8([Consulter sa note et son commentaire — EF17])
        UC9([Rendre une relecture — EF11])
        UC10([Corriger une relecture rendue — EF14])
        UC11([Lister ses relectures assignées — EF18])
        UC12([Assigner un relecteur au hasard — EF10])
        UC13([Générer le code de présence — EF1])
        UC14([Calculer la moyenne d'un étudiant — EF16])
    end

    Formateur --- UC1
    Formateur --- UC2
    Formateur --- UC3
    Formateur --- UC4

    Etudiant --- UC5
    Etudiant --- UC6
    Etudiant --- UC7
    Etudiant --- UC8

    Relecteur --- UC9
    Relecteur --- UC10
    Relecteur --- UC11

    Systeme --- UC12
    Systeme --- UC13
    Systeme --- UC14

    UC1 -. déclenche .-> UC13
    UC6 -. déclenche .-> UC12
    UC4 -. utilise .-> UC14
```

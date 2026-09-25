# Cahier des charges — Plateforme de suivi de sessions KFOKAM48

Auteur : KF48-261 · Version 1 · Frontend choisi : **React**, parce que l'application ne compte que trois écrans avec des appels API simples (pas de besoin de rendu serveur ni de routing avancé) — l'écosystème le plus direct pour ce périmètre.

---

## 1. Contexte et objectif

La direction de la formation KFOKAM48 organise des sessions de cours pendant lesquelles trois besoins reviennent systématiquement et sont aujourd'hui gérés de façon informelle (papier, déclaratif oral, fichiers partagés) :

1. **Constater qui est présent** à une session, sans dispositif d'authentification lourd.
2. **Faire circuler les exercices** entre pairs pour une relecture croisée, sans que l'étudiant ne relise son propre travail.
3. **Donner au formateur une vue d'ensemble** de l'assiduité et de la progression de sa promotion, sans reconstituer l'information à la main après coup.

L'application répond à ce besoin par trois écrans (formateur, étudiant, relecteur) adossés à une API unique, avec un modèle de données qui fait autorité sur la présence, le dépôt d'exercices et les relectures.

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire |
|---|---|
| **Formateur** | Ouvrir une session et obtenir un code de présence · ajouter une présence manuellement · clôturer une session · consulter le tableau de bord de sa promotion |
| **Étudiant** | Choisir son nom dans une liste (pas de mot de passe, Q1) · marquer sa présence avec un code · déposer le lien de son exercice · remplacer ce lien tant qu'aucun relecteur n'est assigné · consulter la note et le commentaire reçus sur son exercice |
| **Relecteur** | Rôle porté par un étudiant présent à la session, assigné automatiquement par le système. Rend une note (0–20, entier) et un commentaire sur l'exercice d'un pair · peut corriger sa relecture tant que la session n'est pas clôturée |
| **Système** | Génère le code de session et son expiration · assigne aléatoirement un relecteur · calcule la moyenne affichée au formateur · verrouille une session à sa clôture |

Un même étudiant porte tour à tour le rôle "étudiant" (sur son propre exercice) et "relecteur" (sur l'exercice d'un pair) : ce ne sont pas deux comptes différents, seulement deux contextes d'action.

## 3. Périmètre

**Inclus :**
- Ouverture de session et génération de code de présence, avec expiration
- Marquage de présence par code, y compris ajout manuel par le formateur
- Dépôt et remplacement du lien d'un exercice
- Assignation aléatoire d'un relecteur parmi les étudiants présents
- Saisie et correction d'une relecture (note + commentaire) jusqu'à clôture de la session
- Clôture de session par le formateur, verrouillant présences / dépôts / relectures
- Tableau de bord agrégé par promotion (présence, dépôts, moyenne, relectures en attente)
- Données de démonstration au démarrage

**Exclu (hors périmètre v1, à documenter comme tel, pas à développer) :**
- Authentification par mot de passe ou tout mécanisme d'identité (Q1)
- Gestion de contenu pédagogique (cours, supports, corrigés)
- Notifications (email, push, SMS)
- Réassignation automatique différée d'un relecteur quand aucun candidat n'est disponible au moment du dépôt (cf. section 7)
- Plusieurs relecteurs par exercice, ou relecture collaborative
- Application mobile native (le web doit rester utilisable sur mobile, cf. exigences non fonctionnelles)
- Export (CSV/PDF) du tableau de bord
- Gestion multi-formateur d'une même session

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| EF1 | Le formateur ouvre une session et obtient un code | Quand un formateur crée une session (titre + promotion), alors il reçoit un id, un code, une heure d'ouverture et une heure d'expiration (+15 min) | Must |
| EF2 | L'étudiant marque sa présence avec un code valide | Quand je saisis un code valide et non expiré et que je ne suis pas déjà présent, ma présence apparaît dans le tableau du formateur | Must |
| EF3 | Un code expiré est refusé | Quand je saisis un code plus de 15 min après l'ouverture, je reçois une erreur 410 `CODE_EXPIRE` | Must |
| EF4 | Une présence en double est refusée | Quand j'ai déjà marqué ma présence sur cette session, je reçois une erreur 409 `DEJA_PRESENT` | Must |
| EF5 | Blocage après 5 erreurs de code | Quand je soumets 5 codes invalides consécutifs sur une session, je suis bloqué 2 minutes avant de pouvoir réessayer | Should |
| EF6 | Le formateur ajoute une présence manuelle | Quand le formateur déclare une présence pour un étudiant, elle apparaît dans le tableau avec la mention "ajouté par le formateur" (source `FORMATEUR`) | Should |
| EF7 | L'étudiant dépose le lien de son exercice | Quand je dépose un lien valide et que je n'ai pas déjà déposé d'exercice pour cette session, il est enregistré (statut `DEPOSE`) | Must |
| EF8 | Un lien invalide est refusé | Quand le lien déposé n'est pas une URL valide, je reçois une erreur 400 `LIEN_INVALIDE` | Must |
| EF9 | L'étudiant remplace le lien de son exercice | Quand aucun relecteur ne m'a encore été assigné, je peux remplacer le lien déposé ; sinon je reçois une erreur 409 `RELECTURE_DEJA_COMMENCEE` | Should |
| EF10 | Le système assigne un relecteur aléatoire | Quand un exercice est déposé et qu'au moins un étudiant présent (hors auteur) existe, un relecteur lui est assigné automatiquement et l'exercice passe à `EN_ATTENTE_RELECTURE` | Must |
| EF11 | Le relecteur rend une relecture | Quand je soumets une note entière (0–20) et un commentaire pour un exercice qui m'est assigné, la relecture est enregistrée et l'exercice passe à `RELU` | Must |
| EF12 | Refus d'auto-relecture | Quand je tente de relire mon propre exercice, je reçois une erreur 403 `AUTO_RELECTURE` | Must |
| EF13 | Refus d'une note invalide | Quand je soumets une note hors de 0–20 ou non entière, je reçois une erreur 400 `NOTE_INVALIDE` | Must |
| EF14 | Le relecteur corrige une relecture déjà rendue | Quand la session n'est pas clôturée, je peux corriger une note déjà envoyée ; une fois la session clôturée, je reçois une erreur 409 `SESSION_CLOTUREE` | Should |
| EF15 | Le formateur clôture une session | Quand le formateur clôture une session, plus aucune présence, dépôt ou relecture n'est modifiable sur cette session | Must |
| EF16 | Le formateur consulte le tableau de bord | Quand je consulte le tableau d'une promotion, je vois par étudiant : présences, exercices déposés, moyenne des notes reçues, relectures encore en attente | Must |
| EF17 | L'étudiant consulte sa note et son commentaire | Quand ma relecture est rendue, je vois la note et le commentaire, jamais l'identité du relecteur | Should |
| EF18 | L'étudiant liste les relectures qui lui sont assignées | Quand je consulte mon espace relecteur, je vois les exercices qui m'ont été assignés, rendus ou non | Could |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| NF1 | Temps de réponse raisonnable en conditions de classe (≤ 60 étudiants marquant leur présence sur une fenêtre de 15 min) | Test manuel/charge légère : p95 < 300 ms sur `POST /api/presences` |
| NF2 | Les trois écrans restent utilisables sur smartphone (l'étudiant marque sa présence depuis son téléphone) | Vérification responsive manuelle sur viewport mobile (< 400px) |
| NF3 | Aucune règle métier dupliquée côté frontend (la moyenne, les statuts viennent toujours de l'API) | Revue de code : aucun calcul de moyenne ou de statut recalculé côté client |
| NF4 | Toute présence ajoutée manuellement reste traçable | Le champ `source` est visible dans le tableau, jamais recalculé a posteriori |
| NF5 | Démarrage reproductible chez un tiers | `docker compose up` ou 3 commandes max, testées depuis un clone vierge, avec données de démonstration |
| NF6 | Aucune fuite d'erreur technique au client | Toute erreur HTTP renvoie le format `{ code, message }` imposé, jamais de stack trace |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| RG1 | Le code de présence expire 15 minutes après l'ouverture de la session | Q2 |
| RG2 | Un étudiant ne peut pas marquer sa présence après expiration du code | Q2, Q3 |
| RG3 | Un étudiant ne peut avoir qu'une seule présence par session | Q4 (implicite, confirmé par le contrat : 409) |
| RG4 | Après 5 tentatives de code erroné consécutives, l'étudiant est bloqué 2 minutes | Q4 |
| RG5 | Un étudiant ne peut jamais relire son propre exercice | Q5 |
| RG6 | Un exercice a exactement un relecteur | Q6 |
| RG7 | Le relecteur est choisi aléatoirement par le système parmi les étudiants présents à la session, à l'exclusion de l'auteur | Q7 |
| RG8 | L'étudiant relu voit sa note et le commentaire reçus, jamais l'identité du relecteur | Q8 |
| RG9 | La note est un entier compris entre 0 et 20 | Q9 |
| RG10 | Le relecteur peut corriger une relecture déjà rendue tant que la session n'est pas clôturée par le formateur ; passé la clôture, la relecture est définitive | Q10, réconciliée avec Q15 — voir section 7 |
| RG11 | Un exercice dont la relecture n'a jamais été rendue reste au statut "en attente" et doit être visible comme tel dans le tableau du formateur | Q11 |
| RG12 | Un étudiant peut déposer son exercice jusqu'à la clôture de la session par le formateur, même après la fin programmée de la session | Q12 |
| RG13 | L'étudiant peut remplacer le lien de son exercice tant qu'aucun relecteur ne lui a été assigné | Q13 |
| RG14 | Une présence ajoutée par le formateur porte la source `FORMATEUR`, distincte d'une présence auto-déclarée (`ETUDIANT`) | Q14 |
| RG15 | Une fois la session clôturée par le formateur, plus aucune présence, dépôt d'exercice ou correction de relecture n'est possible sur cette session | Q10, Q12, Q13, Q15 — trou comblé, voir section 7 |
| RG16 | Le tableau de bord agrège par étudiant, pour une promotion donnée : présence à chaque session, nombre d'exercices déposés, moyenne des notes reçues, relectures encore en attente | Q16 |

## 7. Zones d'ombre, hypothèses et contradictions tranchées

| Point | Réponse client (Qx) ou hypothèse | Décision retenue | Pourquoi |
|---|---|---|---|
| Correction d'une note après envoi | Q10 dit "oui, tant que la session n'est pas clôturée" ; Q15 dit "non, définitive dès l'envoi" — contradiction directe | On **réconcilie** plutôt que d'écarter une réponse : la note est modifiable jusqu'à la clôture de la session (RG10), et devient réellement définitive à ce moment-là (RG15). Q15 est vraie *après* clôture, Q10 est vraie *avant* | Q11 décrit un usage réel où le tableau doit refléter un état "en attente" vivant, ce qui suppose que la session reste ouverte à modification un moment ; Q15 formule une intention ("plus honnête pour tout le monde") qui s'obtient naturellement une fois la session clôturée, sans nier Q10 |
| Aucune des 16 questions ne définit l'action "clôturer une session" | Q10, Q12, Q13 et Q15 la citent toutes comme déclencheur, mais aucune question ne demande qui la déclenche ni ce qu'elle fait précisément — c'est le trou du sujet | On l'ajoute comme **6e opération d'API**, `PATCH /api/sessions/{id}/cloture`, réservée au formateur, qui verrouille définitivement présences, dépôts d'exercices et relectures de cette session (RG15) | Sans cette opération, RG10/RG12/RG13/RG15 sont impossibles à implémenter : la clôture est la pièce manquante qui les rend cohérentes entre elles |
| Le contrat impose `POST /api/relectures/{id}` avec `409 relecture déjà rendue`, ce qui semble interdire toute correction | Le contrat (Annexe B) ne peut pas être modifié (B2) | On garde `POST` tel quel comme **première soumission uniquement**, et on ajoute `PATCH /api/relectures/{id}` pour la **correction** (RG10), refusée par `409 SESSION_CLOTUREE` une fois la session close | Ça respecte le contrat à la lettre (B2) tout en rendant RG10 possible : POST et PATCH ont des sémantiques différentes, l'un ne contredit pas l'autre |
| "Tant que personne n'a commencé à le relire" (Q13) n'est pas défini précisément | Le modèle ne distingue pas "relecteur assigné" de "relecture commencée en train d'être rédigée" | On interprète "commencé à relire" comme **"un relecteur a été assigné"** (RG13/EF9), même si aucune note n'a encore été saisie | Protège le travail du relecteur dès qu'il est engagé, sans avoir à suivre un état intermédiaire ("brouillon") hors périmètre |
| Que se passe-t-il si aucun étudiant (hors auteur) n'est présent au moment du dépôt d'un exercice ? | Aucune question ne couvre ce cas | L'exercice reste au statut `DEPOSE`, sans relecteur assigné, jusqu'à nouvelle tentative manuelle ; pas de réassignation automatique différée en v1 (hors périmètre, section 3) | Cas marginal (promotion très réduite) ; une réassignation différée demanderait un mécanisme d'événement (nouvelle présence après coup) non spécifié par le client |
| La moyenne du tableau (Q16) compte-t-elle les exercices en attente comme 0 ? | Non précisé | La moyenne ne porte que sur les relectures **rendues** ; un exercice en attente n'entre pas dans le calcul | Une moyenne polluée par des 0 artificiels pénaliserait injustement un étudiant dont le relecteur n'a pas rendu sa copie (cf. RG11) |

## 8. Contraintes techniques

- **Backend** : Java 17+, Spring Boot, Maven, wrapper `mvnw` commité (B1)
- Contrat `api/contrat.yaml` respecté à la lettre : chemins, verbes, codes de statut, format d'erreur (B2)
- Séparation contrôleur / service / repository, DTO en frontière API — aucune entité JPA exposée en JSON (B3)
- Validation des entrées + gestion centralisée des erreurs via `@RestControllerAdvice` (B4)
- Schéma versionné par migrations (Flyway ou Liquibase), `ddl-auto=update` interdit hors tests (B5)
- Deux tests significatifs : un test unitaire sur une règle métier réelle, un test d'intégration sur un endpoint (B6)
- **Frontend** : React, build qui passe (F1) ; trois écrans formateur / étudiant / relecteur (F2) ; couche d'appel API dédiée, états de chargement/erreur gérés, aucune règle métier dupliquée (F3)
- **Démarrage** : `docker compose up` ou 3 commandes max documentées, données de démonstration chargées au lancement

## 9. Livrables

- Dépôt GitHub public `kfokam48-epreuve-KF48-261`, structuré `/docs`, `/api`, `/backend`, `/frontend`
- `docs/CAHIER_DES_CHARGES.md` (ce document)
- `docs/diagrammes/` : D1 (cas d'utilisation), D2 (classes / modèle de données), D3 (séquence présence), D4 bonus (états-transitions d'un exercice)
- Backlog en issues GitHub, priorisées Must / Should / Could
- `api/contrat.yaml` complété (5 opérations imposées + opérations additionnelles identifiées en section 7)
- Backend Spring Boot conforme à B1–B6, frontend React conforme à F1–F3
- `JOURNAL.md`, `CHANGELOG.md`, `README.md` d'installation testé depuis un clone vierge
- Trois commits `[JALON]` : `analyse`, `v0.1`, `v1.0`

## 10. Démarche prévue

1. **Étape 1 (en cours)** — cahier des charges, diagrammes, backlog en issues, contrat d'API, puis `[JALON] analyse`
2. **Étape 2** — développement des stories `Must` uniquement, une branche par issue, une PR par branche fermant l'issue (`Closes #x`), commits atomiques référençant `EFx`/`RGx`, puis `[JALON] v0.1`
3. **Étape 3** — réception de l'enveloppe (bug + évolution de besoin), issue dédiée ouverte avant tout correctif, migration versionnée, contrat et analyse mis à jour dans des commits qui le disent explicitement, correctif et évolution traités séparément
4. **Étape 4** — stories `Should` restantes selon le temps disponible, `CHANGELOG.md`, `README` testé depuis un clone vierge, backlog restant trié, puis `[JALON] v1.0`
5. **Étape 5** — vérification des liens/hash en navigation privée, rédaction et dépôt de `SOUMISSION.md` sur la plateforme avant 18h00

**Definition of Done** d'une issue : le code est mergé sur `main` via une PR qui ferme l'issue, les critères d'acceptation sont vérifiés manuellement ou par un test, aucune régression sur le contrat d'API, et le `JOURNAL.md` reflète le travail fait à cette étape.

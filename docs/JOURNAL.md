# Journal de bord — KFOKAM48

## Étape 1 — Analyse et conception

**Fait** : cahier des charges (18 EF, 16 RG numérotées), 4 diagrammes Mermaid (D1 cas d'utilisation, D2 modèle de données, D3 séquence présence, D4 bonus états-transitions), 13 issues créées sur GitHub (7 Must / 5 Should / 1 Could) via `gh` CLI, `api/contrat.yaml` complété (5 opérations imposées + 7 ajoutées), `.gitignore` posé avant tout code.

**Bloqué** : la contradiction Q10 (relecteur corrige jusqu'à clôture) / Q15 (note définitive dès l'envoi) — tranchée par réconciliation plutôt qu'en écartant une réponse : modifiable jusqu'à clôture, définitive après (section 7 du cahier des charges). Le trou (aucune des 16 questions ne définit l'action « clôturer une session ») a été repéré en observant que Q10/Q12/Q13/Q15 la citent toutes comme déclencheur sans jamais la spécifier — comblé par l'ajout de `PATCH /api/sessions/{id}/cloture`.

**IA** : utilisée pour rédiger un premier jet du cahier des charges, des diagrammes et du contrat d'API. Vérifications effectuées :
- Les décisions structurantes (contradiction, trou) ont été proposées par l'IA puis validées explicitement avant rédaction, pas acceptées telles quelles.
- Syntaxe des 4 diagrammes Mermaid vérifiée automatiquement (chargement du moteur Mermaid, `mermaid.parse()` sur chacun) puis relue manuellement ligne par ligne contre la grammaire officielle.
- `api/contrat.yaml` validé avec `js-yaml` (syntaxe) et `@redocly/cli lint` (structure OpenAPI) — cette dernière vérification a permis de détecter et corriger une clé `/api/relectures/{id}` dupliquée qui aurait silencieusement écrasé l'opération imposée `POST`.
- Cohérence croisée relue à la main : le modèle de données (D2) utilisait initialement des `UUID`, incohérent avec les `integer/int64` imposés par le contrat — corrigé en `Long` partout.

## Étape 2 — Construction de la v0.1

**Fait** : scaffolding (Spring Boot 4.1 + React/TypeScript, structure feature-based data/presentation), migration TypeScript du frontend, puis les 7 issues Must (#1, #2, #5, #7, #8, #10, #11) chacune sur sa propre branche avec PR liée, 24 tests backend (unitaires + intégration), build frontend sans erreur. `[JALON] v0.1` posé une fois les 7 PR mergées et le démarrage vérifié depuis un clone vierge.

**Bloqué** :
- Spring Boot 4.1 a renommé plusieurs artefacts et packages par rapport à ce que je connaissais (`spring-boot-starter-webmvc` au lieu de `-web`, `AutoConfigureMockMvc` déplacé de package) — résolu en inspectant directement les jars téléchargés dans `~/.m2`.
- Un test d'intégration du tableau de bord échouait car la base H2 est partagée entre toutes les classes de test sur toute l'exécution (pas de rollback entre classes) : des étudiants réutilisés par index accumulaient des compteurs d'un test à l'autre. Corrigé en créant des étudiants dédiés par test plutôt que de réutiliser les données de démo partagées.
- Vérification du démarrage en 3 commandes (`docker compose up`) : sur cette machine, un service PostgreSQL natif Windows et le proxy réseau de Docker Desktop peuvent tous les deux se lier au port 5432 sans erreur, ce qui a fait que le backend parlait silencieusement à la mauvaise base (celle du service natif, déjà remplie) au lieu du conteneur Docker fraîchement créé — repéré parce que le conteneur, interrogé directement, n'avait aucune des tables Flyway. Le service natif a été arrêté le temps du test pour confirmer que `docker compose up` fonctionne bien seul, puis relancé.

**IA** : utilisée pour écrire le code backend (entités, services, contrôleurs, migrations) et frontend (composants React/TypeScript) de chaque issue. Vérifications effectuées à chaque fois, pas seulement à la fin :
- Chaque issue validée par ses tests d'intégration (H2) **et** par un parcours réel dans un navigateur piloté (backend + frontend réellement lancés, formulaires réellement remplis et soumis, réponses de l'API réellement affichées) — pas une simple lecture de code.
- Le contrat d'API vérifié à la main contre `api/contrat.yaml` pour chaque endpoint (codes de statut, codes d'erreur exacts).
- Le chemin de démarrage du README testé depuis un vrai clone du dépôt distant, pas depuis mon répertoire de travail — c'est ce test qui a révélé l'incident de port ci-dessus.

## Étape 3 — Enveloppe (bug + changement de besoin)

**Fait** :
- Bug (course de présence concurrente) : issue #25 ouverte en traduisant le rapport client en cause technique (race condition check-then-act) *avant* tout code, test de concurrence écrit et exécuté — a échoué en démontrant la violation SQL brute qui fuit — puis correctif (capture de `DataIntegrityViolationException`, traduite en 409 propre), test relancé — passe. Branche et PR séparées du changement de besoin.
- Changement de besoin (deux relecteurs) : cahier des charges et D2 mis à jour *avant* le contrat et le code (RG6 révisée, nouvelles RG17/RG7bis, EF10 révisée, nouvelle EF19), `api/contrat.yaml` mis à jour, migration V7 additive (V6 jamais modifiée), issues #27 et #28 ouvertes. Branche et PR séparées du bug.

**Re-priorisation et sacrifice de périmètre** : ce Must tardif (deux relecteurs) a un coût réel en temps qui doit être compensé. Je sacrifie explicitement les quatre stories `Should` jamais attaquées depuis la v0.1 — EF5 (blocage après 5 erreurs), EF6 (présence manuelle par le formateur), EF9 (remplacer le lien d'un exercice), EF14 (corriger une relecture déjà rendue) — elles ne seront pas tentées non plus à l'étape 4. Elles restent dans le backlog GitHub, non fermées, priorité inchangée, pour rester visibles et honnêtes plutôt que supprimées silencieusement. Le temps ainsi protégé va à la finalisation propre de la v1.0 (CHANGELOG, backlog trié) et à la soumission, qui pèsent plus lourd au barème que des Should supplémentaires.

**Bloqué** : la contrainte `UNIQUE(exercice_id)` posée par V6 a un nom généré automatiquement différent entre H2 (`CONSTRAINT_54`, séquentiel donc imprévisible) et PostgreSQL (`relecture_exercice_id_key`, convention `table_colonne_key`) — un `DROP CONSTRAINT <nom>` n'aurait donc pas été portable. Vérifié empiriquement (petit programme Java autonome contre H2 en mode PostgreSQL) avant de choisir la solution portable : renommer l'ancienne table, recréer avec la bonne contrainte, copier les données, supprimer l'ancienne — testée avec succès contre H2 *et* contre le Postgres réel déjà rempli par toute la session de tests précédente.

**IA** : utilisée pour rédiger le test de concurrence (issue #25) et pour la migration V7. Vérifications : le test de course a été **exécuté avant** le correctif pour confirmer qu'il échouait réellement (pas supposé) ; la migration a été testée contre les deux moteurs réellement utilisés (H2 des tests, Postgres réel) plutôt que relue seulement — c'est ce qui a révélé le problème de nom de contrainte non portable, invisible à la simple lecture du SQL.


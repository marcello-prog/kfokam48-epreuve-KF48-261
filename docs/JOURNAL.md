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


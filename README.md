# KFOKAM48 — Plateforme de suivi de sessions

Gestion de sessions de cours : présence par code, dépôt d'exercice, relecture par les pairs, tableau de bord formateur. Voir `docs/CAHIER_DES_CHARGES.md` pour l'analyse complète et `api/contrat.yaml` pour le contrat d'API.

**Frontend : React + TypeScript (Vite)**, parce que l'application ne compte que trois écrans avec des appels API simples — pas besoin de rendu serveur ni de routing avancé, React est l'écosystème le plus direct pour ce périmètre.

## Démarrage (3 commandes, testé depuis un clone vierge)

Prérequis : Docker, Java 17+, Node 18+.

```bash
# 1. Base de données Postgres (créée avec les données de démo au premier démarrage)
docker compose up -d

# 2. Backend Spring Boot — http://localhost:8080
cd backend && ./mvnw spring-boot:run

# 3. Frontend React — http://localhost:5173
cd frontend && npm install && npm run dev
```

Ouvrir http://localhost:5173/formateur pour ouvrir une session, `/etudiant` pour marquer sa présence et déposer un exercice, `/relecteur` pour rendre une relecture.

Des données de démonstration (1 promotion, 1 formateur, 6 étudiants) sont chargées automatiquement au démarrage du backend via les migrations Flyway (`backend/src/main/resources/db/migration`).

## Tests

```bash
cd backend && ./mvnw test
```

## Structure du dépôt

```
/docs         CAHIER_DES_CHARGES.md, JOURNAL.md, diagrammes/
/api          contrat.yaml
/backend      Spring Boot (Java 17, Maven, wrapper mvnw commité)
/frontend     React + TypeScript (Vite)
```

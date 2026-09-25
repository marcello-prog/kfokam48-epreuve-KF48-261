import { get, post } from "../../../api/client";

export interface DeposerExerciceRequest {
  sessionId: number;
  etudiantId: number;
  lien: string;
}

export interface ExerciceResponse {
  id: number;
  statut: "DEPOSE" | "EN_ATTENTE_RELECTURE" | "RELU";
}

export interface ExerciceAvecNote {
  id: number;
  sessionId: number;
  lien: string;
  statut: "DEPOSE" | "EN_ATTENTE_RELECTURE" | "RELU";
  note: number | null;
  provisoire: boolean;
  commentaires: string[];
}

export function deposerExercice(payload: DeposerExerciceRequest): Promise<ExerciceResponse> {
  return post<ExerciceResponse>("/api/exercices", payload);
}

export function listExercicesEtudiant(etudiantId: number): Promise<ExerciceAvecNote[]> {
  return get<ExerciceAvecNote[]>(`/api/etudiants/${etudiantId}/exercices`);
}

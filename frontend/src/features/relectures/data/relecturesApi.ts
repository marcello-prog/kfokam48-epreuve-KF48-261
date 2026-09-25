import { get, post } from "../../../api/client";

export interface Relecture {
  id: number;
  exerciceId: number;
  lien: string;
  statut: "EN_ATTENTE" | "RENDUE";
  note: number | null;
  commentaire: string | null;
}

export function listRelecturesEtudiant(etudiantId: number): Promise<Relecture[]> {
  return get<Relecture[]>(`/api/etudiants/${etudiantId}/relectures`);
}

export function rendreRelecture(id: number, note: number, commentaire: string): Promise<void> {
  return post<void>(`/api/relectures/${id}`, { note, commentaire });
}

import { get } from "../../../api/client";

export interface Etudiant {
  id: number;
  nom: string;
}

export function listEtudiants(promotionId: number): Promise<Etudiant[]> {
  return get<Etudiant[]>(`/api/promotions/${promotionId}/etudiants`);
}

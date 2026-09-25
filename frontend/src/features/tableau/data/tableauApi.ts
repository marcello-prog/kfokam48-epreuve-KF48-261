import { get } from "../../../api/client";

export interface TableauLigne {
  etudiantId: number;
  nom: string;
  presences: number;
  exercicesDeposes: number;
  moyenne: number | null;
  relecturesEnAttente: number;
}

export function getTableau(promotionId: number): Promise<TableauLigne[]> {
  return get<TableauLigne[]>(`/api/tableau?promotionId=${promotionId}`);
}

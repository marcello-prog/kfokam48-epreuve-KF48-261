import { patch, post } from "../../../api/client";

export interface OuvrirSessionRequest {
  titre: string;
  promotionId: number;
}

export interface SessionResponse {
  id: number;
  code: string;
  ouvertureAt: string;
  expirationAt: string;
}

export interface SessionClotureResponse {
  id: number;
  statut: "OUVERTE" | "EXPIREE" | "CLOTUREE";
  clotureAt: string;
}

export function ouvrirSession(payload: OuvrirSessionRequest): Promise<SessionResponse> {
  return post<SessionResponse>("/api/sessions", payload);
}

export function cloturerSession(id: number): Promise<SessionClotureResponse> {
  return patch<SessionClotureResponse>(`/api/sessions/${id}/cloture`);
}

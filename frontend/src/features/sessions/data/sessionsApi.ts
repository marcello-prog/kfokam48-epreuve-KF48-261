import { post } from "../../../api/client";

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

export function ouvrirSession(payload: OuvrirSessionRequest): Promise<SessionResponse> {
  return post<SessionResponse>("/api/sessions", payload);
}

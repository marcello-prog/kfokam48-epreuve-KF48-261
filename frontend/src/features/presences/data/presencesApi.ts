import { post } from "../../../api/client";

export interface MarquerPresenceRequest {
  code: string;
  etudiantId: number;
}

export interface PresenceResponse {
  id: number;
  sessionId: number;
  etudiantId: number;
  source: "ETUDIANT" | "FORMATEUR";
}

export function marquerPresence(payload: MarquerPresenceRequest): Promise<PresenceResponse> {
  return post<PresenceResponse>("/api/presences", payload);
}

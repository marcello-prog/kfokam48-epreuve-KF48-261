import { useState, type FormEvent } from "react";
import { ouvrirSession, type SessionResponse } from "../data/sessionsApi";
import { ApiError } from "../../../api/client";

// v0.1 : une seule promotion de démo existe (id 1). Le sujet ne fournit
// aucune opération pour lister les promotions ; à revoir si un besoin
// multi-promotion apparaît (cf. enveloppe étape 3).
const PROMOTION_ID = 1;

interface Props {
  onSucces: (session: SessionResponse) => void;
}

export default function OuvrirSessionForm({ onSucces }: Props) {
  const [titre, setTitre] = useState("");
  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [session, setSession] = useState<SessionResponse | null>(null);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setChargement(true);
    setErreur(null);
    try {
      const resultat = await ouvrirSession({ titre, promotionId: PROMOTION_ID });
      setSession(resultat);
      onSucces(resultat);
    } catch (e) {
      setErreur(e instanceof ApiError ? e.message : "Une erreur est survenue.");
    } finally {
      setChargement(false);
    }
  }

  return (
    <div>
      <h2>Ouvrir une session</h2>
      <form onSubmit={onSubmit}>
        <label>
          Titre
          <input value={titre} onChange={(e) => setTitre(e.target.value)} required />
        </label>
        <button type="submit" disabled={chargement}>
          {chargement ? "Ouverture..." : "Ouvrir la session"}
        </button>
      </form>

      {erreur && <p role="alert">{erreur}</p>}

      {session && (
        <p>
          Code : <strong>{session.code}</strong> — expire à{" "}
          {new Date(session.expirationAt).toLocaleTimeString()}
        </p>
      )}
    </div>
  );
}

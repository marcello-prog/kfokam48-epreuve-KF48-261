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
    <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <h2 className="mb-4 text-lg font-semibold text-slate-900">Ouvrir une session</h2>
      <form onSubmit={onSubmit} className="flex flex-wrap items-end gap-3">
        <label className="flex-1 min-w-[200px] text-sm font-medium text-slate-700">
          Titre
          <input
            value={titre}
            onChange={(e) => setTitre(e.target.value)}
            required
            className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
        </label>
        <button
          type="submit"
          disabled={chargement}
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {chargement ? "Ouverture..." : "Ouvrir la session"}
        </button>
      </form>

      {erreur && (
        <p role="alert" className="mt-3 text-sm text-red-600">
          {erreur}
        </p>
      )}

      {session && (
        <p className="mt-4 rounded-md bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
          Code : <strong className="font-semibold">{session.code}</strong> — expire à{" "}
          {new Date(session.expirationAt).toLocaleTimeString()}
        </p>
      )}
    </div>
  );
}

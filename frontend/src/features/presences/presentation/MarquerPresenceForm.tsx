import { useState, type FormEvent } from "react";
import { marquerPresence } from "../data/presencesApi";
import { ApiError } from "../../../api/client";

const MESSAGES_ERREUR: Record<string, string> = {
  CODE_INCONNU: "Ce code de présence n'existe pas.",
  CODE_EXPIRE: "Ce code a expiré (15 minutes après l'ouverture de la session).",
  DEJA_PRESENT: "Vous avez déjà marqué votre présence pour cette session.",
};

interface Props {
  etudiantId: number;
  onSucces: (sessionId: number) => void;
}

export default function MarquerPresenceForm({ etudiantId, onSucces }: Props) {
  const [code, setCode] = useState("");
  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [confirme, setConfirme] = useState(false);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setChargement(true);
    setErreur(null);
    setConfirme(false);
    try {
      const presence = await marquerPresence({ code, etudiantId });
      setConfirme(true);
      onSucces(presence.sessionId);
    } catch (e) {
      if (e instanceof ApiError) {
        setErreur(MESSAGES_ERREUR[e.code] ?? e.message);
      } else {
        setErreur("Une erreur est survenue.");
      }
    } finally {
      setChargement(false);
    }
  }

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <h2 className="mb-4 text-lg font-semibold text-slate-900">Marquer ma présence</h2>
      <form onSubmit={onSubmit} className="flex flex-wrap items-end gap-3">
        <label className="flex-1 min-w-[200px] text-sm font-medium text-slate-700">
          Code de présence
          <input
            value={code}
            onChange={(e) => setCode(e.target.value)}
            required
            className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
        </label>
        <button
          type="submit"
          disabled={chargement}
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {chargement ? "Envoi..." : "Marquer ma présence"}
        </button>
      </form>

      {erreur && (
        <p role="alert" className="mt-3 text-sm text-red-600">
          {erreur}
        </p>
      )}
      {confirme && <p className="mt-3 rounded-md bg-emerald-50 px-3 py-2 text-sm text-emerald-700">Présence enregistrée.</p>}
    </div>
  );
}

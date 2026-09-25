import { useState, type FormEvent } from "react";
import { deposerExercice } from "../data/exercicesApi";
import { ApiError } from "../../../api/client";

const MESSAGES_ERREUR: Record<string, string> = {
  LIEN_INVALIDE: "Ce lien n'est pas une URL valide (http:// ou https://).",
  EXERCICE_DEJA_DEPOSE: "Vous avez déjà déposé un exercice pour cette session.",
};

interface Props {
  sessionId: number;
  etudiantId: number;
}

export default function DeposerExerciceForm({ sessionId, etudiantId }: Props) {
  const [lien, setLien] = useState("");
  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [confirme, setConfirme] = useState(false);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setChargement(true);
    setErreur(null);
    try {
      await deposerExercice({ sessionId, etudiantId, lien });
      setConfirme(true);
    } catch (e) {
      setErreur(e instanceof ApiError ? (MESSAGES_ERREUR[e.code] ?? e.message) : "Une erreur est survenue.");
    } finally {
      setChargement(false);
    }
  }

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <h2 className="mb-4 text-lg font-semibold text-slate-900">Déposer mon exercice</h2>
      <form onSubmit={onSubmit} className="flex flex-wrap items-end gap-3">
        <label className="flex-1 min-w-[200px] text-sm font-medium text-slate-700">
          Lien de l'exercice
          <input
            type="url"
            value={lien}
            onChange={(e) => setLien(e.target.value)}
            placeholder="https://..."
            required
            className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
        </label>
        <button
          type="submit"
          disabled={chargement}
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {chargement ? "Envoi..." : "Déposer"}
        </button>
      </form>

      {erreur && (
        <p role="alert" className="mt-3 text-sm text-red-600">
          {erreur}
        </p>
      )}
      {confirme && <p className="mt-3 rounded-md bg-emerald-50 px-3 py-2 text-sm text-emerald-700">Exercice déposé.</p>}
    </div>
  );
}

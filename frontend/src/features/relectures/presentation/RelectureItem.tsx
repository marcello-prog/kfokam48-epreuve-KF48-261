import { useState, type FormEvent } from "react";
import { rendreRelecture, type Relecture } from "../data/relecturesApi";
import { ApiError } from "../../../api/client";

const MESSAGES_ERREUR: Record<string, string> = {
  NOTE_INVALIDE: "La note doit être un entier compris entre 0 et 20.",
  AUTO_RELECTURE: "Vous ne pouvez pas relire votre propre exercice.",
  RELECTURE_DEJA_RENDUE: "Cette relecture a déjà été rendue.",
};

interface Props {
  relecture: Relecture;
  onRendue: () => void;
}

export default function RelectureItem({ relecture, onRendue }: Props) {
  const [note, setNote] = useState("");
  const [commentaire, setCommentaire] = useState("");
  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);

  if (relecture.statut === "RENDUE") {
    return (
      <li className="rounded-md bg-slate-50 px-4 py-3 text-sm text-slate-600">
        Exercice {relecture.lien} — relecture rendue (note : {relecture.note}/20)
      </li>
    );
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setChargement(true);
    setErreur(null);
    try {
      await rendreRelecture(relecture.id, Number(note), commentaire);
      onRendue();
    } catch (e) {
      setErreur(e instanceof ApiError ? (MESSAGES_ERREUR[e.code] ?? e.message) : "Une erreur est survenue.");
    } finally {
      setChargement(false);
    }
  }

  return (
    <li className="rounded-md border border-slate-200 p-4">
      <p className="mb-3 text-sm text-slate-700">
        Exercice à relire :{" "}
        <a href={relecture.lien} className="text-indigo-600 underline">
          {relecture.lien}
        </a>
      </p>
      <form onSubmit={onSubmit} className="flex flex-wrap items-end gap-3">
        <label className="w-24 text-sm font-medium text-slate-700">
          Note (0-20)
          <input
            type="number"
            min={0}
            max={20}
            value={note}
            onChange={(e) => setNote(e.target.value)}
            required
            className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
        </label>
        <label className="flex-1 min-w-[200px] text-sm font-medium text-slate-700">
          Commentaire
          <textarea
            value={commentaire}
            onChange={(e) => setCommentaire(e.target.value)}
            required
            className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
        </label>
        <button
          type="submit"
          disabled={chargement}
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {chargement ? "Envoi..." : "Envoyer la relecture"}
        </button>
      </form>
      {erreur && (
        <p role="alert" className="mt-2 text-sm text-red-600">
          {erreur}
        </p>
      )}
    </li>
  );
}

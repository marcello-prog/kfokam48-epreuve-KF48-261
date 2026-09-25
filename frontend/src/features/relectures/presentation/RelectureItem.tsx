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
      <li>
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
    <li>
      <p>
        Exercice à relire : <a href={relecture.lien}>{relecture.lien}</a>
      </p>
      <form onSubmit={onSubmit}>
        <label>
          Note (0-20)
          <input type="number" min={0} max={20} value={note} onChange={(e) => setNote(e.target.value)} required />
        </label>
        <label>
          Commentaire
          <textarea value={commentaire} onChange={(e) => setCommentaire(e.target.value)} required />
        </label>
        <button type="submit" disabled={chargement}>
          {chargement ? "Envoi..." : "Envoyer la relecture"}
        </button>
      </form>
      {erreur && <p role="alert">{erreur}</p>}
    </li>
  );
}

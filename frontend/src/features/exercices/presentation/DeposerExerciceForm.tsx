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
    <div>
      <h2>Déposer mon exercice</h2>
      <form onSubmit={onSubmit}>
        <label>
          Lien de l'exercice
          <input
            type="url"
            value={lien}
            onChange={(e) => setLien(e.target.value)}
            placeholder="https://..."
            required
          />
        </label>
        <button type="submit" disabled={chargement}>
          {chargement ? "Envoi..." : "Déposer"}
        </button>
      </form>

      {erreur && <p role="alert">{erreur}</p>}
      {confirme && <p>Exercice déposé.</p>}
    </div>
  );
}

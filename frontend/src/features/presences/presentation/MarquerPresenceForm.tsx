import { useState, type FormEvent } from "react";
import { marquerPresence } from "../data/presencesApi";
import SelecteurEtudiant from "../../promotions/presentation/SelecteurEtudiant";
import { ApiError } from "../../../api/client";

const MESSAGES_ERREUR: Record<string, string> = {
  CODE_INCONNU: "Ce code de présence n'existe pas.",
  CODE_EXPIRE: "Ce code a expiré (15 minutes après l'ouverture de la session).",
  DEJA_PRESENT: "Vous avez déjà marqué votre présence pour cette session.",
};

export default function MarquerPresenceForm() {
  const [etudiantId, setEtudiantId] = useState<number | null>(null);
  const [code, setCode] = useState("");
  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [confirme, setConfirme] = useState(false);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    if (!etudiantId) return;

    setChargement(true);
    setErreur(null);
    setConfirme(false);
    try {
      await marquerPresence({ code, etudiantId });
      setConfirme(true);
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
    <div>
      <h2>Marquer ma présence</h2>
      <form onSubmit={onSubmit}>
        <SelecteurEtudiant value={etudiantId} onChange={setEtudiantId} />
        <label>
          Code de présence
          <input value={code} onChange={(e) => setCode(e.target.value)} required />
        </label>
        <button type="submit" disabled={chargement || !etudiantId}>
          {chargement ? "Envoi..." : "Marquer ma présence"}
        </button>
      </form>

      {erreur && <p role="alert">{erreur}</p>}
      {confirme && <p>Présence enregistrée.</p>}
    </div>
  );
}

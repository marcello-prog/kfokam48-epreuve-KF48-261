import { useState } from "react";
import { cloturerSession } from "../data/sessionsApi";
import { ApiError } from "../../../api/client";

interface Props {
  sessionId: number;
  onClotureee?: () => void;
}

export default function ClotureSessionButton({ sessionId, onClotureee }: Props) {
  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [cloturee, setCloturee] = useState(false);

  async function onClick() {
    setChargement(true);
    setErreur(null);
    try {
      await cloturerSession(sessionId);
      setCloturee(true);
      onClotureee?.();
    } catch (e) {
      setErreur(e instanceof ApiError ? e.message : "Une erreur est survenue.");
    } finally {
      setChargement(false);
    }
  }

  if (cloturee) {
    return <p className="rounded-md bg-slate-100 px-3 py-2 text-sm text-slate-700">Session clôturée.</p>;
  }

  return (
    <div>
      <button
        type="button"
        onClick={onClick}
        disabled={chargement}
        className="rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-50"
      >
        {chargement ? "Clôture..." : "Clôturer la session"}
      </button>
      {erreur && (
        <p role="alert" className="mt-2 text-sm text-red-600">
          {erreur}
        </p>
      )}
    </div>
  );
}

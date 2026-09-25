import { useCallback, useEffect, useState } from "react";
import { listRelecturesEtudiant, type Relecture } from "../data/relecturesApi";
import { ApiError } from "../../../api/client";
import RelectureItem from "./RelectureItem";

interface Props {
  etudiantId: number;
}

export default function ListeRelectures({ etudiantId }: Props) {
  const [relectures, setRelectures] = useState<Relecture[]>([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState<string | null>(null);

  const charger = useCallback(() => {
    setChargement(true);
    listRelecturesEtudiant(etudiantId)
      .then(setRelectures)
      .catch((e) => setErreur(e instanceof ApiError ? e.message : "Impossible de charger les relectures."))
      .finally(() => setChargement(false));
  }, [etudiantId]);

  useEffect(() => {
    charger();
  }, [charger]);

  if (chargement) return <p className="text-sm text-slate-500">Chargement des relectures...</p>;
  if (erreur)
    return (
      <p role="alert" className="text-sm text-red-600">
        {erreur}
      </p>
    );

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <h2 className="mb-4 text-lg font-semibold text-slate-900">Mes relectures assignées</h2>
      {relectures.length === 0 ? (
        <p className="text-sm text-slate-500">Aucune relecture assignée pour le moment.</p>
      ) : (
        <ul className="space-y-4">
          {relectures.map((relecture) => (
            <RelectureItem key={relecture.id} relecture={relecture} onRendue={charger} />
          ))}
        </ul>
      )}
    </div>
  );
}

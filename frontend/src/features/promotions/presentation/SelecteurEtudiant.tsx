import { useEffect, useState } from "react";
import { listEtudiants, type Etudiant } from "../data/promotionsApi";
import { ApiError } from "../../../api/client";

// v0.1 : une seule promotion de démo (id 1), cf. OuvrirSessionForm.
const PROMOTION_ID = 1;

interface Props {
  value: number | null;
  onChange: (etudiantId: number) => void;
}

export default function SelecteurEtudiant({ value, onChange }: Props) {
  const [etudiants, setEtudiants] = useState<Etudiant[]>([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState<string | null>(null);

  useEffect(() => {
    listEtudiants(PROMOTION_ID)
      .then(setEtudiants)
      .catch((e) => setErreur(e instanceof ApiError ? e.message : "Impossible de charger la liste des étudiants."))
      .finally(() => setChargement(false));
  }, []);

  if (chargement) return <p className="text-sm text-slate-500">Chargement de la liste des étudiants...</p>;
  if (erreur)
    return (
      <p role="alert" className="text-sm text-red-600">
        {erreur}
      </p>
    );

  return (
    <label className="block max-w-xs text-sm font-medium text-slate-700">
      Mon nom
      <select
        value={value ?? ""}
        onChange={(e) => onChange(Number(e.target.value))}
        required
        className="mt-1 block w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
      >
        <option value="" disabled>
          -- choisir --
        </option>
        {etudiants.map((etudiant) => (
          <option key={etudiant.id} value={etudiant.id}>
            {etudiant.nom}
          </option>
        ))}
      </select>
    </label>
  );
}

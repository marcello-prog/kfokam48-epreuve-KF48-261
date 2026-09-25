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

  if (chargement) return <p>Chargement de la liste des étudiants...</p>;
  if (erreur) return <p role="alert">{erreur}</p>;

  return (
    <label>
      Mon nom
      <select value={value ?? ""} onChange={(e) => onChange(Number(e.target.value))} required>
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

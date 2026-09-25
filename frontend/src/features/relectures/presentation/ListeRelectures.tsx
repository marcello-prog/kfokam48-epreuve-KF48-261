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

  if (chargement) return <p>Chargement des relectures...</p>;
  if (erreur) return <p role="alert">{erreur}</p>;

  return (
    <div>
      <h2>Mes relectures assignées</h2>
      {relectures.length === 0 ? (
        <p>Aucune relecture assignée pour le moment.</p>
      ) : (
        <ul>
          {relectures.map((relecture) => (
            <RelectureItem key={relecture.id} relecture={relecture} onRendue={charger} />
          ))}
        </ul>
      )}
    </div>
  );
}

import { useEffect, useState } from "react";
import { getTableau, type TableauLigne } from "../data/tableauApi";
import { ApiError } from "../../../api/client";

// v0.1 : une seule promotion de démo (id 1), cf. OuvrirSessionForm.
const PROMOTION_ID = 1;

interface Props {
  rafraichir?: unknown;
}

export default function TableauBord({ rafraichir }: Props) {
  const [lignes, setLignes] = useState<TableauLigne[]>([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState<string | null>(null);

  useEffect(() => {
    setChargement(true);
    getTableau(PROMOTION_ID)
      .then(setLignes)
      .catch((e) => setErreur(e instanceof ApiError ? e.message : "Impossible de charger le tableau."))
      .finally(() => setChargement(false));
  }, [rafraichir]);

  if (chargement) return <p className="text-sm text-slate-500">Chargement du tableau...</p>;
  if (erreur)
    return (
      <p role="alert" className="text-sm text-red-600">
        {erreur}
      </p>
    );

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <h2 className="mb-4 text-lg font-semibold text-slate-900">Tableau de bord</h2>
      <table className="w-full border-collapse text-left text-sm">
        <thead>
          <tr className="border-b border-slate-200 text-slate-500">
            <th className="py-2 pr-4 font-medium">Étudiant</th>
            <th className="py-2 pr-4 font-medium">Présences</th>
            <th className="py-2 pr-4 font-medium">Exercices déposés</th>
            <th className="py-2 pr-4 font-medium">Moyenne</th>
            <th className="py-2 pr-4 font-medium">Relectures en attente</th>
          </tr>
        </thead>
        <tbody>
          {lignes.map((ligne) => (
            <tr key={ligne.etudiantId} className="border-b border-slate-100 last:border-0">
              <td className="py-2 pr-4 text-slate-900">{ligne.nom}</td>
              <td className="py-2 pr-4">{ligne.presences}</td>
              <td className="py-2 pr-4">{ligne.exercicesDeposes}</td>
              <td className="py-2 pr-4">{ligne.moyenne !== null ? ligne.moyenne.toFixed(1) : "—"}</td>
              <td className="py-2 pr-4">{ligne.relecturesEnAttente}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

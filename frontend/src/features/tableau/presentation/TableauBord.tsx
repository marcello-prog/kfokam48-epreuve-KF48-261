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

  if (chargement) return <p>Chargement du tableau...</p>;
  if (erreur) return <p role="alert">{erreur}</p>;

  return (
    <div>
      <h2>Tableau de bord</h2>
      <table>
        <thead>
          <tr>
            <th>Étudiant</th>
            <th>Présences</th>
            <th>Exercices déposés</th>
            <th>Moyenne</th>
            <th>Relectures en attente</th>
          </tr>
        </thead>
        <tbody>
          {lignes.map((ligne) => (
            <tr key={ligne.etudiantId}>
              <td>{ligne.nom}</td>
              <td>{ligne.presences}</td>
              <td>{ligne.exercicesDeposes}</td>
              <td>{ligne.moyenne !== null ? ligne.moyenne.toFixed(1) : "—"}</td>
              <td>{ligne.relecturesEnAttente}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

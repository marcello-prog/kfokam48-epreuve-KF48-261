import { useEffect, useState } from "react";
import { listExercicesEtudiant, type ExerciceAvecNote } from "../data/exercicesApi";
import { ApiError } from "../../../api/client";

interface Props {
  etudiantId: number;
  refreshSignal?: unknown;
}

// EF17, EF19 (étape 3) : la note et les commentaires viennent tels quels de
// l'API (RG17) — jamais recalculés côté client (F3). L'identité des
// relecteurs n'est jamais exposée (RG8).
export default function MaNote({ etudiantId, refreshSignal }: Props) {
  const [exercices, setExercices] = useState<ExerciceAvecNote[]>([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState<string | null>(null);

  useEffect(() => {
    setChargement(true);
    listExercicesEtudiant(etudiantId)
      .then(setExercices)
      .catch((e) => setErreur(e instanceof ApiError ? e.message : "Impossible de charger mes notes."))
      .finally(() => setChargement(false));
  }, [etudiantId, refreshSignal]);

  if (chargement) return <p className="text-sm text-slate-500">Chargement de mes notes...</p>;
  if (erreur)
    return (
      <p role="alert" className="text-sm text-red-600">
        {erreur}
      </p>
    );

  const avecNote = exercices.filter((e) => e.note !== null);
  if (avecNote.length === 0) return null;

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <h2 className="mb-4 text-lg font-semibold text-slate-900">Mes notes reçues</h2>
      <ul className="space-y-3">
        {avecNote.map((exercice) => (
          <li key={exercice.id} className="rounded-md bg-slate-50 px-4 py-3 text-sm">
            <p>
              Note : <strong>{exercice.note}/20</strong>
              {exercice.provisoire && (
                <span className="ml-2 rounded bg-amber-100 px-2 py-0.5 text-xs font-medium text-amber-700">
                  provisoire
                </span>
              )}
            </p>
            {exercice.commentaires.map((commentaire, i) => (
              <p key={i} className="mt-1 text-slate-600">
                « {commentaire} »
              </p>
            ))}
          </li>
        ))}
      </ul>
    </div>
  );
}

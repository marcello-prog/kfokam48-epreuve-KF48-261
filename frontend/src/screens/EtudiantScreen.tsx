import { useState } from "react";
import SelecteurEtudiant from "../features/promotions/presentation/SelecteurEtudiant";
import MarquerPresenceForm from "../features/presences/presentation/MarquerPresenceForm";
import DeposerExerciceForm from "../features/exercices/presentation/DeposerExerciceForm";

export default function EtudiantScreen() {
  const [etudiantId, setEtudiantId] = useState<number | null>(null);
  const [sessionId, setSessionId] = useState<number | null>(null);

  return (
    <section className="space-y-6 py-8">
      <h1 className="text-2xl font-semibold text-slate-900">Étudiant</h1>
      <SelecteurEtudiant value={etudiantId} onChange={setEtudiantId} />

      {etudiantId && <MarquerPresenceForm etudiantId={etudiantId} onSucces={setSessionId} />}

      {etudiantId && sessionId && <DeposerExerciceForm etudiantId={etudiantId} sessionId={sessionId} />}
    </section>
  );
}

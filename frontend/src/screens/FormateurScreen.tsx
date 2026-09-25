import { useState } from "react";
import OuvrirSessionForm from "../features/sessions/presentation/OuvrirSessionForm";
import ClotureSessionButton from "../features/sessions/presentation/ClotureSessionButton";
import TableauBord from "../features/tableau/presentation/TableauBord";
import type { SessionResponse } from "../features/sessions/data/sessionsApi";

export default function FormateurScreen() {
  const [session, setSession] = useState<SessionResponse | null>(null);
  const [rafraichissement, setRafraichissement] = useState(0);

  function onSessionOuverte(nouvelleSession: SessionResponse) {
    setSession(nouvelleSession);
    setRafraichissement((n) => n + 1);
  }

  return (
    <section className="space-y-6 py-8">
      <h1 className="text-2xl font-semibold text-slate-900">Formateur</h1>
      <OuvrirSessionForm onSucces={onSessionOuverte} />
      {session && (
        <ClotureSessionButton sessionId={session.id} onClotureee={() => setRafraichissement((n) => n + 1)} />
      )}
      <TableauBord rafraichir={rafraichissement} />
    </section>
  );
}

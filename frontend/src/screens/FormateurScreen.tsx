import { useState } from "react";
import OuvrirSessionForm from "../features/sessions/presentation/OuvrirSessionForm";
import ClotureSessionButton from "../features/sessions/presentation/ClotureSessionButton";
import type { SessionResponse } from "../features/sessions/data/sessionsApi";

export default function FormateurScreen() {
  const [session, setSession] = useState<SessionResponse | null>(null);

  return (
    <section>
      <h1>Formateur</h1>
      <OuvrirSessionForm onSucces={setSession} />
      {session && <ClotureSessionButton sessionId={session.id} />}
    </section>
  );
}

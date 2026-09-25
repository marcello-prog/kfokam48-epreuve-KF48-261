import { useState } from "react";
import SelecteurEtudiant from "../features/promotions/presentation/SelecteurEtudiant";
import ListeRelectures from "../features/relectures/presentation/ListeRelectures";

export default function RelecteurScreen() {
  const [etudiantId, setEtudiantId] = useState<number | null>(null);

  return (
    <section>
      <h1>Relecteur</h1>
      <SelecteurEtudiant value={etudiantId} onChange={setEtudiantId} />
      {etudiantId && <ListeRelectures etudiantId={etudiantId} />}
    </section>
  );
}

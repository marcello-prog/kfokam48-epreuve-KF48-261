import { BrowserRouter, Navigate, NavLink, Route, Routes } from "react-router-dom";
import FormateurScreen from "./screens/FormateurScreen";
import EtudiantScreen from "./screens/EtudiantScreen";
import RelecteurScreen from "./screens/RelecteurScreen";

const lienClasses = ({ isActive }: { isActive: boolean }) =>
  `rounded-md px-3 py-2 text-sm font-medium transition-colors ${
    isActive ? "bg-indigo-600 text-white" : "text-slate-600 hover:bg-slate-100"
  }`;

function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-slate-50">
        <nav className="mx-auto flex max-w-4xl items-center gap-2 px-6 py-4">
          <NavLink to="/formateur" className={lienClasses}>
            Formateur
          </NavLink>
          <NavLink to="/etudiant" className={lienClasses}>
            Étudiant
          </NavLink>
          <NavLink to="/relecteur" className={lienClasses}>
            Relecteur
          </NavLink>
        </nav>
        <main className="mx-auto max-w-4xl px-6 pb-16">
          <Routes>
            <Route path="/" element={<Navigate to="/formateur" replace />} />
            <Route path="/formateur" element={<FormateurScreen />} />
            <Route path="/etudiant" element={<EtudiantScreen />} />
            <Route path="/relecteur" element={<RelecteurScreen />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;

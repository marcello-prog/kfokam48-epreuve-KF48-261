import { BrowserRouter, Link, Navigate, Route, Routes } from "react-router-dom";
import FormateurScreen from "./screens/FormateurScreen";
import EtudiantScreen from "./screens/EtudiantScreen";
import RelecteurScreen from "./screens/RelecteurScreen";

function App() {
  return (
    <BrowserRouter>
      <nav>
        <Link to="/formateur">Formateur</Link>
        {" | "}
        <Link to="/etudiant">Étudiant</Link>
        {" | "}
        <Link to="/relecteur">Relecteur</Link>
      </nav>
      <Routes>
        <Route path="/" element={<Navigate to="/formateur" replace />} />
        <Route path="/formateur" element={<FormateurScreen />} />
        <Route path="/etudiant" element={<EtudiantScreen />} />
        <Route path="/relecteur" element={<RelecteurScreen />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;

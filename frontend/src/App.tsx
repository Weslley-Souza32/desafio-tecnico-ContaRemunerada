import { Navigate, Route, Routes } from "react-router";
import { PlaceholderPage } from "./pages/PlaceholderPage";
import { ProtectedRoute } from "./routes/ProtectedRoute";
import { LoginPage } from "./pages/LoginPage";
import { CadastroPage } from "./pages/CadastroPage";
import { PainelPage } from "./pages/PainelPage";

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />

      <Route
        path="/login"
        element={<LoginPage />}
      />

      <Route
        path="/cadastro"
        element={<CadastroPage />}
      />

      <Route element={<ProtectedRoute />}>
        <Route
          path="/painel"
          element={<PainelPage />}
        />
      </Route>

      <Route
        path="*"
        element={<PlaceholderPage title="Página não encontrada" />}
      />
    </Routes>
  );
}

export default App;

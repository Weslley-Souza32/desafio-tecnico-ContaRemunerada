import { Navigate, Route, Routes } from "react-router";
import { PlaceholderPage } from "./pages/PlaceholderPage";
import { ProtectedRoute } from "./routes/ProtectedRoute";

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />

      <Route
        path="/login"
        element={<PlaceholderPage title="Entrar na conta" />}
      />

      <Route
        path="/cadastro"
        element={<PlaceholderPage title="Abrir conta" />}
      />

      <Route element={<ProtectedRoute />}>
        <Route
          path="/painel"
          element={<PlaceholderPage title="Painel da conta" />}
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

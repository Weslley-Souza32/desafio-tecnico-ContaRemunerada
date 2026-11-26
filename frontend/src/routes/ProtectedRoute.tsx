import { Navigate, Outlet } from "react-router";
import { tokenStorage } from "../auth/tokenStorage";

export function ProtectedRoute() {
    const token = tokenStorage.get();

    if (!token) {
        return <Navigate to="/login" replace/>
    }

    return <Outlet/>
}
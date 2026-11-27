import type { CriarContaRequest, CriarContaResponse, LoginRequest, LoginResponse } from "../types/auth";
import { httpClient } from "./httpClient";

export const authService = {
    async criarConta(
        dados: CriarContaRequest
    ): Promise<CriarContaResponse> {

        const response = await httpClient.post<CriarContaResponse>(
            '/contas',
            dados
        );

        return response.data;
    },

    async login(
        dados: LoginRequest
    ): Promise<LoginResponse> {

        const response = await httpClient.post<LoginResponse>(
            '/auth/login',
            dados
        );

        return response.data;
    }
}
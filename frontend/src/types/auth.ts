export interface CriarContaRequest {
    titular: string;
    senha: string;
}

export interface CriarContaResponse {
    id: number;
    numero: string;
}

export interface LoginRequest {
    numero: string;
    senha: string;
}

export interface LoginResponse {
    token: string;
    contaId: number;
}
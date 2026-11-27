import type { ExtratoResponse } from "../types/extrato";
import type { SaldoResponse } from "../types/saldo";
import { httpClient } from "./httpClient";

export const contaService = {
    async consultarSaldo(contaId: number): Promise<SaldoResponse> {
        const response = await httpClient.get<SaldoResponse>(
            `/contas/${contaId}/saldo`
        );

        return response.data;
    },

    async consultaExtrato(contaId: number, page = 0, size = 20): Promise<ExtratoResponse> {
        const response = await httpClient.get<ExtratoResponse>(
            `/contas/${contaId}/extrato`,
            {
                params: {page, size},
            }
        );

        return response.data;
    }
}

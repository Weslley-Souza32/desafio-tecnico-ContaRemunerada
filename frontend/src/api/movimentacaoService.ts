import type { MovimentacaoRequest } from "../types/movimentacao";
import { httpClient } from "./httpClient";

export const movimentacaoService = {
  async depositar(contaId: number, dados: MovimentacaoRequest): Promise<void> {
    await httpClient.post(`/contas/${contaId}/depositos`, dados);
  },

  async sacar(contaId: number, dados: MovimentacaoRequest): Promise<void> {
    await httpClient.post(`/contas/${contaId}/saques`, dados);
  },
};
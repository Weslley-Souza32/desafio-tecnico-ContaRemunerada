export type TipoMovimentacao = 'DEPOSITO' | 'SAQUE';

export interface MovimentacaoRequest {
    valor: string;
}
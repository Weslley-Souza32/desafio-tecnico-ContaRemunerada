export type TipoLancamento =
    | 'DEPOSITO'
    | 'SAQUE'
    | 'JUROS';

export interface LancamentoResponse {
    tipo: TipoLancamento;
    dataHora: string;
    valor: string;
}

export interface ExtratoResponse {
    content: LancamentoResponse[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}
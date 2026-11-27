export function formatarMoeda(valor: string): string {
    const resultado = /^(-?)(\d+)(?:\.(\d{1,2}))?$/.exec(valor);

    if (!resultado) {
        return 'Valor indispónivel';
    }

    const [, sinal, parteInteira, parteDecimal = ''] = resultado;

    const inteiroFormatado = parteInteira
        .replace(/\B(?=(\d{3})+(?!\d))/g, '.');

    const centavos = parteDecimal.padEnd(2, '0');

    const valorFormatado = `R$ ${inteiroFormatado},${centavos}`;

    return sinal === '-' ? `-${valorFormatado}` : valorFormatado;
}
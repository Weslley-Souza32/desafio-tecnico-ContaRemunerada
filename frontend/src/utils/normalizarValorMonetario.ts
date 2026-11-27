export function normalizarValorMonetario(
    entrada: string
): string | null {
    const valor = entrada.trim();

    const formatoBrasileiro = /^(?:[1-9]\d{0,2}(?:\.\d{3})+|(?:0|[1-9]\d*))(?:,\d{1,2})?$/;

    if (!formatoBrasileiro.test(valor)) {
        return null;
    }

    const normalizado = valor
        .replace(/\./g, '')
        .replace(',', '.');

        if (!/[1-9]/.test(normalizado)) {
            return null;
        }

        return normalizado;
}
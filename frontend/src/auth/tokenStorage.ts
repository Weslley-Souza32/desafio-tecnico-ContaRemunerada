const TOKEN_KEY = 'conta_remunerada_access_token';
const CONTA_ID__KEY = 'conta_remunerada_conta_id';

export const tokenStorage = {
    get(): string | null {
        return sessionStorage.getItem(TOKEN_KEY);
    },

    getContaId(): number | null {
        const contaId = sessionStorage.getItem(CONTA_ID__KEY);

        if (!contaId) {
            return null;
        }

        const id = Number(contaId);

        return Number.isSafeInteger(id) && id > 0 ? id : null;
    },

    setSession(token: string, contaId: number): void {
        sessionStorage.setItem(TOKEN_KEY, token);
        sessionStorage.setItem(CONTA_ID__KEY, String(contaId));
    },

    remove(): void {
        sessionStorage.removeItem(TOKEN_KEY);
        sessionStorage.removeItem(CONTA_ID__KEY);
    },
};

import axios from 'axios';
import type { ApiError } from '../types/apiError';

export interface HttpError {
  status?: number;
  codigo: string;
  mensagem: string;
}

function isApiError(data: unknown): data is ApiError {
  if (typeof data !== 'object' || data === null) {
    return false;
  }

  return (
    'codigo' in data &&
    typeof data.codigo === 'string' &&
    'mensagem' in data &&
    typeof data.mensagem === 'string'
  );
}

export function handleApiError(error: unknown): HttpError {
  if (!axios.isAxiosError(error)) {
    return {
      codigo: 'ERRO_INESPERADO',
      mensagem: 'Ocorreu um erro inesperado.',
    };
  }

  const status = error.response?.status;
  const data: unknown = error.response?.data;

  if (isApiError(data)) {
    return {
      status,
      codigo: data.codigo,
      mensagem: data.mensagem,
    };
  }

  if (!error.response) {
    return {
      codigo: 'FALHA_COMUNICACAO',
      mensagem: 'Não foi possível comunicar com o servidor.',
    };
  }

  if (status === 401) {
    return {
      status,
      codigo: 'NAO_AUTENTICADO',
      mensagem: 'Não foi possível autenticar a requisição.',
    };
  }

  if (status === 403) {
    return {
      status,
      codigo: 'ACESSO_NEGADO',
      mensagem: 'Você não tem permissão para realizar esta operação.',
    };
  }

  if (status === 400) {
    return {
      status,
      codigo: 'REQUISICAO_INVALIDA',
      mensagem: 'Verifique os dados informados.',
    };
  }

  return {
    status,
    codigo: 'ERRO_HTTP',
    mensagem: 'Não foi possível concluir a operação.',
  };
}

import apiClient from '@/shared/infrastructure/http/client';

export interface AlterarMinhaSenhaRequest {
  senhaAtual: string;
  novaSenha: string;
}

export interface SolicitarResetSenhaRequest {
  username: string;
}

/**
 * Altera a senha do usuário autenticado no sistema.
 */
export const alterarMinhaSenha = async (data: AlterarMinhaSenhaRequest): Promise<void> => {
  await apiClient.post('/autenticacao/alterar-senha', data);
};

/**
 * Solicita publicamente a redefinição de acesso na tela de login.
 */
export const solicitarResetSenha = async (username: string): Promise<void> => {
  await apiClient.post('/autenticacao/solicitar-reset-senha', { username });
};

import apiClient from '@/api/client';
export interface Notificacao { id: string; tipo: string; mensagem: string; criadaEmUtc: string; lidaEmUtc: string | null; }
export const listarNotificacoes = async (): Promise<Notificacao[]> => (await apiClient.get<Notificacao[]>('/notificacoes')).data;
export const marcarNotificacaoLida = async (id: string): Promise<void> => { await apiClient.post(`/notificacoes/${id}/marcar-lida`); };

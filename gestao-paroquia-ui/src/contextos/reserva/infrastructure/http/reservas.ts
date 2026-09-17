import apiClient from '@/shared/infrastructure/http/client';
import {
  CancelarReservaPayload,
  CriarReservaPayload,
  CriarReservaRecorrentePayload,
  DevolverAtivoPayload,
  DisponibilidadeSlot,
  ReservaDetalhe,
  ReservaMetricas,
  StatusReserva,
  TipoItemReserva,
} from '@/shared/domain/types/types';

export interface ListarReservasFiltro {
  tipoItem?: TipoItemReserva;
  itemId?: string;
  status?: StatusReserva;
  inicio?: string;
  fim?: string;
  pastoralMovimentoId?: string;
  solicitanteId?: string;
  eventoId?: string;
}

export async function listarReservas(filtro?: ListarReservasFiltro): Promise<ReservaDetalhe[]> {
  const { data } = await apiClient.get<ReservaDetalhe[]>('/reservas', { params: filtro });
  return data;
}

export async function buscarReservaPorId(id: string): Promise<ReservaDetalhe> {
  const { data } = await apiClient.get<ReservaDetalhe>(`/reservas/${id}`);
  return data;
}

export async function criarReserva(payload: CriarReservaPayload): Promise<ReservaDetalhe> {
  const { data } = await apiClient.post<ReservaDetalhe>('/reservas', payload);
  return data;
}

export async function criarReservaRecorrente(payload: CriarReservaRecorrentePayload): Promise<ReservaDetalhe[]> {
  const { data } = await apiClient.post<ReservaDetalhe[]>('/reservas/recorrente', payload);
  return data;
}

export async function aprovarReserva(id: string): Promise<ReservaDetalhe> {
  const { data } = await apiClient.patch<ReservaDetalhe>(`/reservas/${id}/aprovar`);
  return data;
}

export async function rejeitarReserva(id: string, motivo: string): Promise<ReservaDetalhe> {
  const { data } = await apiClient.patch<ReservaDetalhe>(`/reservas/${id}/rejeitar`, { motivo });
  return data;
}

export async function cancelarReserva(id: string, payload: CancelarReservaPayload): Promise<ReservaDetalhe> {
  const { data } = await apiClient.patch<ReservaDetalhe>(`/reservas/${id}/cancelar`, payload);
  return data;
}

export async function registrarRetiradaAtivo(id: string): Promise<ReservaDetalhe> {
  const { data } = await apiClient.patch<ReservaDetalhe>(`/reservas/${id}/retirar`);
  return data;
}

export async function registrarDevolucaoAtivo(id: string, payload: DevolverAtivoPayload): Promise<ReservaDetalhe> {
  const { data } = await apiClient.patch<ReservaDetalhe>(`/reservas/${id}/devolver`, payload);
  return data;
}

export async function obterMetricasReservas(): Promise<ReservaMetricas> {
  const { data } = await apiClient.get<ReservaMetricas>('/reservas/metricas');
  return data;
}

export async function obterDisponibilidadeItem(
  tipoItem: TipoItemReserva,
  itemId: string,
  data: string
): Promise<DisponibilidadeSlot[]> {
  const { data: slots } = await apiClient.get<DisponibilidadeSlot[]>('/reservas/disponibilidade', {
    params: { tipoItem, itemId, data },
  });
  return slots;
}

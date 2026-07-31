import apiClient from './client';
import { ApiSolenidadeDTO } from '../utils/liturgicalCalendar';

export async function fetchSolenidadesLiturgicasAtivas(): Promise<ApiSolenidadeDTO[]> {
  const response = await apiClient.get<ApiSolenidadeDTO[]>('/solenidades-liturgicas/ativas');
  return response.data;
}

export async function fetchAllSolenidadesLiturgicas(): Promise<ApiSolenidadeDTO[]> {
  const response = await apiClient.get<ApiSolenidadeDTO[]>('/solenidades-liturgicas');
  return response.data;
}

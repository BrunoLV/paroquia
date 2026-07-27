import apiClient from './client';
import {AnoParoquial, AnoParoquialStatus} from './types';

export const getAnosParoquiais = async (): Promise<AnoParoquial[]> => {
  const response = await apiClient.get<AnoParoquial[]>('/anos-paroquiais');
  return response.data;
};

export const updateAnoParoquialStatus = async (
  ano: number,
  status: AnoParoquialStatus
): Promise<AnoParoquial> => {
  const response = await apiClient.patch<AnoParoquial>(`/anos-paroquiais/${ano}`, { status });
  return response.data;
};

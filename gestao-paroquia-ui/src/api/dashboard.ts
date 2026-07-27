import apiClient from './client';
import {DashboardMetrics} from './types';

export const getDashboardMetrics = async (): Promise<DashboardMetrics> => {
  const response = await apiClient.get<DashboardMetrics>('/dashboard/metrics');
  return response.data;
};

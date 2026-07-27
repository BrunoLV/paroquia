import apiClient from './client';
import {AprovacaoDecisionRequest, AprovacaoDecisionResponse, AprovacaoResponse} from './types';

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const getApprovals = async (
  status?: string,
  page: number = 0,
  size: number = 10
): Promise<PaginatedResponse<AprovacaoResponse>> => {
  const params: Record<string, any> = { page, size };
  if (status) {
    params.status = status;
  }

  const response = await apiClient.get<PaginatedResponse<AprovacaoResponse>>('/aprovacoes', {
    params,
  });
  return response.data;
};

export const decideApproval = async (
  id: string,
  request: AprovacaoDecisionRequest
): Promise<AprovacaoDecisionResponse> => {
  const response = await apiClient.patch<AprovacaoDecisionResponse>(`/aprovacoes/${id}`, request);
  return response.data;
};

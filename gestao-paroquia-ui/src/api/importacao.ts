import api from './client';

export interface ImportacaoItemResultado {
  linha: number;
  valido: boolean;
  mensagemErro?: string;
  dados: Record<string, string>;
}

export interface ImportacaoValidacaoResultado {
  totalLinhas: number;
  totalValidos: number;
  totalInvalidos: number;
  itens: ImportacaoItemResultado[];
}

export const downloadTemplate = async (tipo: string, formato: 'CSV' | 'XLSX'): Promise<void> => {
  const response = await api.get(`/importacao/template`, {
    params: { tipo, formato },
    responseType: 'blob',
  });

  const url = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement('a');
  link.href = url;
  const ext = formato === 'XLSX' ? 'xlsx' : 'csv';
  link.setAttribute('download', `template_${tipo.toLowerCase()}.${ext}`);
  document.body.appendChild(link);
  link.click();
  link.remove();
};

export const validarImportacao = async (tipo: string, file: File): Promise<ImportacaoValidacaoResultado> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await api.post<ImportacaoValidacaoResultado>(`/importacao/validar?tipo=${tipo}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

  return response.data;
};

export const confirmarImportacao = async (tipo: string, validItems: Record<string, string>[]): Promise<{ registrosImportados: number }> => {
  const response = await api.post<{ registrosImportados: number }>(`/importacao/confirmar?tipo=${tipo}`, validItems);
  return response.data;
};

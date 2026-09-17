import api from '@/shared/infrastructure/http/client';

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

/**
 * Passo 1 do fluxo de importação em massa: baixa a planilha-modelo (CSV/XLSX) para o
 * tipo de entidade escolhido, já com as colunas esperadas pelo backend, e dispara o
 * download no navegador (não retorna dados — efeito colateral é o único propósito).
 * @param tipo Tipo de entidade a importar (ex.: membros, organizações) — define o layout do modelo.
 * @param formato Formato do arquivo a baixar, 'CSV' ou 'XLSX'.
 */
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

/**
 * Passo 2 do fluxo de importação em massa: envia o arquivo preenchido (baixado no
 * passo 1) para validação em modo "dry-run" no backend — nenhum registro é persistido
 * aqui. O resultado traz, linha a linha, quais itens são válidos e quais têm erro,
 * para que o usuário revise antes de confirmar a importação (passo 3).
 * @param tipo Tipo de entidade sendo importada, deve ser o mesmo do template baixado.
 * @param file Arquivo preenchido pelo usuário (CSV ou XLSX).
 */
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

/**
 * Passo 3 (final) do fluxo de importação em massa: persiste efetivamente os registros
 * previamente validados no passo 2. Deve receber apenas os itens marcados como válidos
 * (`valido: true`) retornados por `validarImportacao` — itens inválidos não devem ser
 * reenviados aqui, pois este endpoint não revalida, apenas grava.
 * @param tipo Tipo de entidade sendo importada, deve ser o mesmo dos passos anteriores.
 * @param validItems Linhas já validadas e aprovadas para importação (campo `dados` de cada item válido).
 */
export const confirmarImportacao = async (tipo: string, validItems: Record<string, string>[]): Promise<{ registrosImportados: number }> => {
  const response = await api.post<{ registrosImportados: number }>(`/importacao/confirmar?tipo=${tipo}`, validItems);
  return response.data;
};

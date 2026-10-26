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

export interface ImportacaoLinha {
  linha: number;
  dados: Record<string, string>;
}

export interface ImportacaoErroLinha {
  linha: number;
  mensagem: string;
}

export interface ImportacaoConfirmacaoResultado {
  registrosImportados: number;
  enviadosParaAprovacao: number;
  erros: ImportacaoErroLinha[];
}

/**
 * Passo 1 do fluxo de importação em massa: baixa a planilha-modelo (CSV/XLSX) para o
 * tipo de entidade escolhido, já com as colunas esperadas pelo backend, e dispara o
 * download no navegador (não retorna dados — efeito colateral é o único propósito).
 * @param tipo Tipo de entidade a importar (ex.: membros, organizações) — define o layout do modelo.
 * @param formato Formato do arquivo a baixar, 'CSV' ou 'XLSX'.
 */
export const downloadTemplate = async (tipo: string, formato: 'CSV' | 'XLSX'): Promise<void> => {
  const response = await api.get(`/importacao/modelo`, {
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
 * Passo 3 (final) do fluxo de importação em massa: grava os registros validados no passo 2. O backend NÃO confia
 * no que recebe: cada linha é reinterpretada e submetida às mesmas regras de domínio da validação, e grava em
 * transação própria. O erro de uma linha não impede a gravação das demais; os motivos voltam em `erros`.
 * @param tipo Tipo de entidade sendo importada, deve ser o mesmo dos passos anteriores.
 * @param itens Linhas marcadas como válidas em `validarImportacao`, com o número da linha do arquivo original.
 */
export const confirmarImportacao = async (tipo: string, itens: ImportacaoLinha[]): Promise<ImportacaoConfirmacaoResultado> => {
  const response = await api.post<ImportacaoConfirmacaoResultado>(`/importacao/confirmar?tipo=${tipo}`, { itens });
  return response.data;
};

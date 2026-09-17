/**
 * Rótulos amigáveis para os enums do painel de Métricas Estratégicas (`api/metricasTypes.ts`).
 * O backend já resolve a classificação executiva (nível/status/diagnóstico); aqui só traduzimos
 * o valor bruto para texto de exibição — cor e ícone continuam decididos no componente chamador.
 */

const NIVEL_DEMANDA_LABELS: Record<string, string> = {
  BAIXA: 'Baixa',
  NORMAL: 'Normal',
  ALTA: 'Alta',
  PICO_OPERACIONAL: 'Pico Operacional',
};

const AVALIACAO_EXECUTIVA_LABELS: Record<string, string> = {
  SAUDAVEL: 'Saudável',
  ALERTA: 'Alerta',
  CRITICO: 'Crítico',
};

const STATUS_SLA_LABELS: Record<string, string> = {
  EXCELENTE: 'Excelente',
  DENTRO_DO_ESPERADO: 'Dentro do Esperado',
  GARGALO_BUROCRATICO: 'Gargalo Burocrático',
};

const DIAGNOSTICO_PASTORAL_LABELS: Record<string, string> = {
  MISSIONARIA_EM_SAIDA: 'Missionária em Saída',
  EQUILIBRADA: 'Equilibrada',
  EXCESSIVAMENTE_BUROCRATICA: 'Excessivamente Burocrática',
};

const NIVEL_RISCO_LABELS: Record<string, string> = {
  BAIXO: 'Baixo',
  MEDIO: 'Médio',
  ALTO: 'Alto',
};

const RECOMENDACAO_CAE_LABELS: Record<string, string> = {
  EXCELENTE_APROVEITAMENTO: 'Excelente Aproveitamento',
  OPORTUNIDADE_DE_NOVAS_FRENTES: 'Oportunidade de Novas Frentes',
  SUBAPROVEITAMENTO_CRITICO: 'Subaproveitamento Crítico',
};

export const getNivelDemandaLabel = (value: string): string => NIVEL_DEMANDA_LABELS[value] ?? value;
export const getAvaliacaoExecutivaLabel = (value: string): string => AVALIACAO_EXECUTIVA_LABELS[value] ?? value;
export const getStatusSlaLabel = (value: string): string => STATUS_SLA_LABELS[value] ?? value;
export const getDiagnosticoPastoralLabel = (value: string): string => DIAGNOSTICO_PASTORAL_LABELS[value] ?? value;
export const getNivelRiscoLabel = (value: string): string => NIVEL_RISCO_LABELS[value] ?? value;
export const getRecomendacaoCaeLabel = (value: string): string => RECOMENDACAO_CAE_LABELS[value] ?? value;

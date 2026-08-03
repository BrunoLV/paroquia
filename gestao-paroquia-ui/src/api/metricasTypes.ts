/**
 * Classificação dos ciclos do calendário litúrgico usados para explicar picos/vales de demanda
 * de eventos ao longo do ano. Espelha o enum equivalente no backend
 * (`gestao-paroquia/.../calendario/api/v1/dto/metrics`), então qualquer novo ciclo litúrgico
 * adicionado lá precisa ser replicado aqui para não quebrar o mapeamento de rótulos na UI.
 */
export type TipoCicloLiturgico = 'ADVENTO' | 'NATAL' | 'QUARESMA' | 'PASCOA' | 'FESTA_PADROEIRA' | 'TEMPO_COMUM';

/**
 * Indicador de sazonalidade litúrgica: compara a demanda de eventos observada historicamente
 * em cada ciclo litúrgico com a prevista, permitindo à coordenação pastoral antecipar picos
 * (ex.: Semana Santa, festa da padroeira) em vez de reagir a eles.
 *
 * `indiceSazonalidade` é a razão entre eventos previstos e o baseline do ciclo — usado para
 * colorir/ordenar o gráfico de sazonalidade; `nivelDemanda` é a categorização executiva desse
 * índice já calculada no backend, para a UI não precisar reimplementar os limiares.
 */
export interface SazonalidadeLiturgicaResponse {
  cicloNome: string;
  tipoCiclo: TipoCicloLiturgico;
  fatorDemanda: number;
  quantidadeEventosHistoricos: number;
  quantidadeEventosPrevistos: number;
  indiceSazonalidade: number;
  nivelDemanda: 'BAIXA' | 'NORMAL' | 'ALTA' | 'PICO_OPERACIONAL';
}

/**
 * Taxa de conclusão de projetos pastorais: mede quantos projetos chegam ao fim versus quantos
 * são abandonados/cancelados, servindo de termômetro de saúde da gestão de projetos da paróquia.
 *
 * `avaliacaoExecutiva` é o diagnóstico já classificado pelo backend (saudável/alerta/crítico)
 * a partir das taxas percentuais, evitando duplicar a regra de negócio no frontend.
 */
export interface TaxaConclusaoProjetosResponse {
  totalProjetos: number;
  projetosConcluidos: number;
  projetosEmAndamento: number;
  projetosCancelados: number;
  taxaConclusaoPercentual: number;
  taxaAbandonoPercentual: number;
  avaliacaoExecutiva: 'SAUDAVEL' | 'ALERTA' | 'CRITICO';
}

/**
 * SLA de despacho pastoral: mede a velocidade com que solicitações de aprovação (eventos/projetos)
 * são decididas, para identificar gargalos burocráticos no fluxo de aprovação.
 *
 * `slaMetaHoras` é o alvo configurado de tempo de resposta; `statusSla` classifica o desempenho
 * real (`tempoMedioAprovacaoHoras`/`percentualDentroSla`) frente a essa meta, já resolvido no backend.
 */
export interface SlaDespachoPastoralResponse {
  totalSolicitacoes: number;
  solicitacoesAprovadas: number;
  solicitacoesRejeitadas: number;
  tempoMedioAprovacaoHoras: number;
  slaMetaHoras: number;
  percentualDentroSla: number;
  statusSla: 'EXCELENTE' | 'DENTRO_DO_ESPERADO' | 'GARGALO_BUROCRATICO';
}

/**
 * Coeficiente de impacto social: proporção de eventos voltados a ação social/caridade em
 * relação a reuniões internas, usado para avaliar se a paróquia está mais voltada à "saída
 * missionária" ou concentrada em burocracia interna.
 *
 * `diagnosticoPastoral` é a leitura executiva desse coeficiente já calculada no backend.
 */
export interface CoeficienteImpactoSocialResponse {
  totalEventos: number;
  eventosSociaisCaridade: number;
  reunioesInternas: number;
  coeficienteImpactoSocialPercentual: number;
  diagnosticoPastoral: 'MISSIONARIA_EM_SAIDA' | 'EQUILIBRADA' | 'EXCESSIVAMENTE_BUROCRATICA';
}

/**
 * Um conflito de agenda detectado entre dois eventos de pastorais diferentes que competem
 * pelo mesmo público-alvo no mesmo dia (ex.: dois grupos jovens marcando encontro na mesma
 * data) — não é um conflito de local/recurso, e sim de sobreposição de público-alvo.
 *
 * `mensagemAlerta` já vem formatada pelo backend para exibição direta na UI, sem necessidade
 * de montar a frase no frontend.
 */
export interface ConflitoItemDto {
  eventoIdA: string;
  tituloEventoA: string;
  pastoralA: string;
  eventoIdB: string;
  tituloEventoB: string;
  pastoralB: string;
  dataEvento: string;
  publicoAlvoCompartilhado: string;
  mensagemAlerta: string;
}

/**
 * Painel de concorrência interpastoral: agrega todos os conflitos de agenda detectados entre
 * pastorais no período analisado, com um nível de risco geral já classificado pelo backend
 * (`nivelRiscoConcorrencia`) a partir da quantidade/gravidade dos conflitos em `conflitos`.
 */
export interface ConcorrenciaInterpastoralResponse {
  totalConflitosDetectados: number;
  conflitos: ConflitoItemDto[];
  nivelRiscoConcorrencia: 'BAIXO' | 'MEDIO' | 'ALTO';
}

/**
 * Ociosidade patrimonial: mede o percentual de horas úteis semanais em que os locais/espaços
 * da paróquia ficam sem uso, para embasar decisões do CAE (Conselho de Assuntos Econômicos)
 * sobre abrir novas frentes de uso ou revisar o aproveitamento dos espaços existentes.
 *
 * `recomendacaoCae` é a leitura executiva já resolvida no backend a partir do
 * `indiceOciosidadePercentual`.
 */
export interface OciosidadePatrimonialResponse {
  totalLocais: number;
  totalHorasUteisSemana: number;
  horasOcupadasSemana: number;
  horasOciosasSemana: number;
  indiceOciosidadePercentual: number;
  recomendacaoCae: 'EXCELENTE_APROVEITAMENTO' | 'OPORTUNIDADE_DE_NOVAS_FRENTES' | 'SUBAPROVEITAMENTO_CRITICO';
}

/**
 * Payload consolidado do painel de métricas estratégicas (endpoint único que agrega todos os
 * indicadores executivos da paróquia), evitando que a página de relatórios precise disparar
 * seis requisições separadas — ver `fetchPainelMetricasConsolidado` em `api/metricas.ts` e o
 * hook `usePainelMetricasEstrategicas` em `hooks/useMetricasEstrategicas.ts`.
 */
export interface MetricasEstrategicasPainelResponse {
  sazonalidadeLiturgica: SazonalidadeLiturgicaResponse[];
  taxaProjetos: TaxaConclusaoProjetosResponse;
  slaDespacho: SlaDespachoPastoralResponse;
  impactoSocial: CoeficienteImpactoSocialResponse;
  concorrenciaInterpastoral: ConcorrenciaInterpastoralResponse;
  ociosidadePatrimonial: OciosidadePatrimonialResponse;
}

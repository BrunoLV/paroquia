import { DashboardMetrics } from '../api/types';
import { MetricasEstrategicasPainelResponse } from '../api/metricasTypes';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import {
  AlignmentType,
  Document,
  HeadingLevel,
  Packer,
  Paragraph,
  Table,
  TableCell,
  TableRow,
  TextRun,
  WidthType,
} from 'docx';

/**
 * Converte o objeto de métricas do dashboard em uma string CSV formatada.
 */
export const generateMetricsCSV = (metrics: DashboardMetrics): string => {
  const lines: string[] = [];

  // Cabeçalho / UTF-8 BOM para garantir acentuação no Excel
  lines.push('RESUMO GERAL DE METRICAS');
  lines.push('Indicador,Valor');
  lines.push(`Total Eventos no Mes,${metrics.resumoGeral.totalEventosMes}`);
  lines.push(`Projetos Ativos,${metrics.resumoGeral.projetosAtivos}`);
  lines.push(`Solicitacoes Pendentes,${metrics.resumoGeral.solicitacoesPendentes}`);
  lines.push('');

  // Distribuição de Eventos por Organização
  lines.push('DISTRIBUICAO DE EVENTOS POR ORGANIZACAO');
  lines.push('Organizacao,Quantidade de Eventos');
  if (metrics.eventosPorOrganizacao && metrics.eventosPorOrganizacao.length > 0) {
    metrics.eventosPorOrganizacao.forEach((item) => {
      lines.push(`"${item.organizacaoNome}",${item.quantidadeEventos}`);
    });
  } else {
    lines.push('Nenhuma organizacao registrada,0');
  }
  lines.push('');

  // Projetos Ativos
  lines.push('PROJETOS ATIVOS');
  lines.push('Nome do Projeto,Status,Percentual Concluido,Total Eventos,Eventos Realizados,Em Risco');
  if (metrics.projetosAtivosDetalhes && metrics.projetosAtivosDetalhes.length > 0) {
    metrics.projetosAtivosDetalhes.forEach((proj) => {
      const emRiscoText = proj.emRisco ? 'Sim' : 'Nao';
      lines.push(
        `"${proj.nome}",${proj.status},${proj.percentualTempoDecorrido}%,${proj.totalEventos},${proj.eventosRealizados},${emRiscoText}`
      );
    });
  } else {
    lines.push('Nenhum projeto ativo,N/A,0%,0,0,Nao');
  }

  return lines.join('\n');
};

/**
 * Converte o painel de métricas estratégicas em uma string CSV formatada.
 */
export const generateStrategicMetricsCSV = (data: MetricasEstrategicasPainelResponse): string => {
  const lines: string[] = [];

  lines.push('RELATORIO DE METRICAS ESTRATEGICAS E PREDITIVAS');
  lines.push('');

  // 1. Sazonalidade Litúrgica
  lines.push('1. SAZONALIDADE LITURGICA (PREDICTIVE ANALYTICS)');
  lines.push('Ciclo Liturgico,Fator Demanda,Eventos Historicos,Eventos Previstos,Nivel Demanda');
  if (data.sazonalidadeLiturgica && data.sazonalidadeLiturgica.length > 0) {
    data.sazonalidadeLiturgica.forEach((s) => {
      lines.push(`"${s.cicloNome}",${s.fatorDemanda}x,${s.quantidadeEventosHistoricos},${s.quantidadeEventosPrevistos},${s.nivelDemanda}`);
    });
  }
  lines.push('');

  // 2. Taxa de Conclusão de Projetos
  lines.push('2. CONCLUSAO E ABANDONO DE PROJETOS (CPP/CAE)');
  lines.push('Indicador,Valor');
  lines.push(`Total Projetos,${data.taxaProjetos?.totalProjetos ?? 0}`);
  lines.push(`Projetos Concluidos,${data.taxaProjetos?.projetosConcluidos ?? 0}`);
  lines.push(`Projetos em Andamento,${data.taxaProjetos?.projetosEmAndamento ?? 0}`);
  lines.push(`Projetos Cancelados,${data.taxaProjetos?.projetosCancelados ?? 0}`);
  lines.push(`Taxa Conclusao %,${data.taxaProjetos?.taxaConclusaoPercentual ?? 0}%`);
  lines.push(`Taxa Abandono %,${data.taxaProjetos?.taxaAbandonoPercentual ?? 0}%`);
  lines.push(`Avaliacao Executiva,${data.taxaProjetos?.avaliacaoExecutiva ?? 'N/A'}`);
  lines.push('');

  // 3. SLA de Despacho Pastoral
  lines.push('3. SLA DE DESPACHO PASTORAL');
  lines.push('Indicador,Valor');
  lines.push(`Tempo Medio Aprovacao (Horas),${data.slaDespacho?.tempoMedioAprovacaoHoras ?? 0}`);
  lines.push(`Meta SLA (Horas),${data.slaDespacho?.slaMetaHoras ?? 0}`);
  lines.push(`Percentual Dentro do SLA,${data.slaDespacho?.percentualDentroSla ?? 0}%`);
  lines.push(`Status SLA,${data.slaDespacho?.statusSla ?? 'N/A'}`);
  lines.push('');

  // 4. Coeficiente de Impacto Social
  lines.push('4. COEFICIENTE DE IMPACTO SOCIAL (PASTORAL EM SAIDA)');
  lines.push('Indicador,Valor');
  lines.push(`Total Eventos Analisados,${data.impactoSocial?.totalEventos ?? 0}`);
  lines.push(`Eventos Sociais e Caridade,${data.impactoSocial?.eventosSociaisCaridade ?? 0}`);
  lines.push(`Reunioes Internas Burocraticas,${data.impactoSocial?.reunioesInternas ?? 0}`);
  lines.push(`Coeficiente de Acao Missionaria %,${data.impactoSocial?.coeficienteImpactoSocialPercentual ?? 0}%`);
  lines.push(`Diagnostico Pastoral,${data.impactoSocial?.diagnosticoPastoral ?? 'N/A'}`);
  lines.push('');

  // 5. Concorrência Interpastoral
  lines.push('5. CONCORRENCIA INTERPASTORAL');
  lines.push(`Conflitos Detectados,${data.concorrenciaInterpastoral?.totalConflitosDetectados ?? 0}`);
  lines.push(`Nivel de Risco,${data.concorrenciaInterpastoral?.nivelRiscoConcorrencia ?? 'BAIXO'}`);
  lines.push('');

  // 6. Ociosidade Patrimonial
  lines.push('6. OCIOSIDADE PATRIMONIAL (CAE)');
  lines.push('Indicador,Valor');
  lines.push(`Horas Ociosas por Semana,${data.ociosidadePatrimonial?.horasOciosasSemana ?? 0}`);
  lines.push(`Indice Ociosidade %,${data.ociosidadePatrimonial?.indiceOciosidadePercentual ?? 0}%`);
  lines.push(`Recomendacao CAE,${data.ociosidadePatrimonial?.recomendacaoCae ?? 'N/A'}`);

  return lines.join('\n');
};

/**
 * Dispara o download de um conteúdo CSV no navegador.
 */
export const downloadCSV = (content: string, filename: string): void => {
  const blob = new Blob(['\uFEFF' + content], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.setAttribute('href', url);
  link.setAttribute('download', filename);
  link.style.visibility = 'hidden';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
};

/**
 * Gera um documento PDF vetorial estruturado com tabelas, cabeçalhos e numeração de páginas nativas.
 */
export const generateStructuredPDF = async (
  metrics: DashboardMetrics,
  filename: string = 'relatorio-gerencial.pdf'
): Promise<boolean> => {
  try {
    const doc = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
    const primaryColor: [number, number, number] = [26, 35, 126]; // #1a237e

    // Header institucional
    doc.setFillColor(...primaryColor);
    doc.rect(0, 0, 210, 24, 'F');

    doc.setTextColor(255, 255, 255);
    doc.setFontSize(15);
    doc.setFont('helvetica', 'bold');
    doc.text('PARÓQUIA NOSSA SENHORA DE FÁTIMA', 14, 12);

    doc.setFontSize(9);
    doc.setFont('helvetica', 'normal');
    doc.text('Relatório Gerencial & Operacional Paroquial', 14, 18);

    const now = new Date().toLocaleString('pt-BR');
    doc.setFontSize(8);
    doc.text(`Gerado em: ${now}`, 196, 18, { align: 'right' });

    let currentY = 32;

    // Resumo Geral (KPIs)
    doc.setTextColor(...primaryColor);
    doc.setFontSize(12);
    doc.setFont('helvetica', 'bold');
    doc.text('1. Resumo Geral de Indicadores', 14, currentY);
    currentY += 4;

    autoTable(doc, {
      startY: currentY,
      head: [['Indicador', 'Valor']],
      body: [
        ['Total de Eventos no Mês / Período', String(metrics.resumoGeral.totalEventosMes)],
        ['Projetos Pastorais Ativos', String(metrics.resumoGeral.projetosAtivos)],
        ['Solicitações Pendentes de Aprovação', String(metrics.resumoGeral.solicitacoesPendentes)],
      ],
      headStyles: { fillColor: primaryColor, textColor: 255, fontStyle: 'bold' },
      styles: { fontSize: 9, cellPadding: 3 },
      theme: 'grid',
    });

    currentY = (doc as any).lastAutoTable.finalY + 8;

    // Eventos por Organização
    doc.setTextColor(...primaryColor);
    doc.setFontSize(12);
    doc.setFont('helvetica', 'bold');
    doc.text('2. Distribuição de Eventos por Organização / Pastoral', 14, currentY);
    currentY += 4;

    const orgRows = metrics.eventosPorOrganizacao?.map((item) => [
      item.organizacaoNome,
      String(item.quantidadeEventos),
    ]) || [['Nenhuma organização registrada', '0']];

    autoTable(doc, {
      startY: currentY,
      head: [['Organização / Pastoral', 'Quantidade de Eventos']],
      body: orgRows,
      headStyles: { fillColor: primaryColor, textColor: 255, fontStyle: 'bold' },
      styles: { fontSize: 9, cellPadding: 3 },
      theme: 'grid',
    });

    currentY = (doc as any).lastAutoTable.finalY + 8;

    // Projetos Ativos
    doc.setTextColor(...primaryColor);
    doc.setFontSize(12);
    doc.setFont('helvetica', 'bold');
    doc.text('3. Acompanhamento de Projetos Pastorais', 14, currentY);
    currentY += 4;

    const projRows = metrics.projetosAtivosDetalhes?.map((proj) => [
      proj.nome,
      proj.status || '-',
      `${Math.round(proj.percentualTempoDecorrido)}%`,
      `${proj.eventosRealizados} / ${proj.totalEventos}`,
      proj.emRisco ? 'EM RISCO' : 'Normal',
    ]) || [['Nenhum projeto ativo', '-', '0%', '0/0', 'Normal']];

    autoTable(doc, {
      startY: currentY,
      head: [['Projeto', 'Status', 'Tempo Decorrido', 'Eventos (Realizados/Total)', 'Situação']],
      body: projRows,
      headStyles: { fillColor: primaryColor, textColor: 255, fontStyle: 'bold' },
      styles: { fontSize: 8, cellPadding: 3 },
      theme: 'grid',
    });

    currentY = (doc as any).lastAutoTable.finalY + 8;

    // Atividades e Eventos no Período
    if (metrics.eventosSemana && metrics.eventosSemana.length > 0) {
      if (currentY > 230) {
        doc.addPage();
        currentY = 20;
      }

      doc.setTextColor(...primaryColor);
      doc.setFontSize(12);
      doc.setFont('helvetica', 'bold');
      doc.text('4. Lista de Atividades e Eventos no Período', 14, currentY);
      currentY += 4;

      const eventRows = metrics.eventosSemana.map((ev) => [
        ev.titulo,
        ev.organizacaoNome || 'Paróquia',
        ev.localNome || 'Sem Local',
        ev.inicio
          ? new Date(ev.inicio).toLocaleString('pt-BR', {
              day: '2-digit',
              month: '2-digit',
              year: 'numeric',
              hour: '2-digit',
              minute: '2-digit',
            })
          : '-',
      ]);

      autoTable(doc, {
        startY: currentY,
        head: [['Título do Evento', 'Organização', 'Local', 'Data / Horário']],
        body: eventRows,
        headStyles: { fillColor: primaryColor, textColor: 255, fontStyle: 'bold' },
        styles: { fontSize: 8, cellPadding: 2.5 },
        theme: 'grid',
      });
    }

    // Numeração de páginas no rodapé
    const totalPages = doc.getNumberOfPages();
    for (let i = 1; i <= totalPages; i++) {
      doc.setPage(i);
      doc.setFontSize(8);
      doc.setTextColor(150, 150, 150);
      doc.text(
        `Página ${i} de ${totalPages} — Sistema de Gestão Paroquial`,
        105,
        290,
        { align: 'center' }
      );
    }

    doc.save(filename);
    return true;
  } catch (error) {
    console.error('Erro ao gerar PDF estruturado:', error);
    return false;
  }
};

/**
 * Gera PDF vetorial estruturado das Métricas Estratégicas & Preditivas.
 */
export const generateStrategicStructuredPDF = async (
  data: MetricasEstrategicasPainelResponse,
  filename: string = 'metricas-estrategicas.pdf'
): Promise<boolean> => {
  try {
    const doc = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
    const primaryColor: [number, number, number] = [26, 35, 126];

    // Header institucional
    doc.setFillColor(...primaryColor);
    doc.rect(0, 0, 210, 24, 'F');

    doc.setTextColor(255, 255, 255);
    doc.setFontSize(15);
    doc.setFont('helvetica', 'bold');
    doc.text('PARÓQUIA NOSSA SENHORA DE FÁTIMA', 14, 12);

    doc.setFontSize(9);
    doc.setFont('helvetica', 'normal');
    doc.text('Relatório Executivo — Métricas Estratégicas & Preditivas', 14, 18);

    const now = new Date().toLocaleString('pt-BR');
    doc.setFontSize(8);
    doc.text(`Gerado em: ${now}`, 196, 18, { align: 'right' });

    let currentY = 32;

    // 1. Sazonalidade Litúrgica
    doc.setTextColor(...primaryColor);
    doc.setFontSize(12);
    doc.setFont('helvetica', 'bold');
    doc.text('1. Sazonalidade Litúrgica (Predictive Analytics)', 14, currentY);
    currentY += 4;

    const sazRows = data.sazonalidadeLiturgica?.map((s) => [
      s.cicloNome,
      `${s.fatorDemanda}x`,
      String(s.quantidadeEventosHistoricos),
      String(s.quantidadeEventosPrevistos),
      s.nivelDemanda,
    ]) || [];

    autoTable(doc, {
      startY: currentY,
      head: [['Ciclo Litúrgico', 'Fator Demanda', 'Histórico', 'Projeção', 'Nível Demanda']],
      body: sazRows,
      headStyles: { fillColor: primaryColor, textColor: 255, fontStyle: 'bold' },
      styles: { fontSize: 8.5, cellPadding: 2.5 },
      theme: 'grid',
    });

    currentY = (doc as any).lastAutoTable.finalY + 8;

    // 2. Conclusão & Abandono de Projetos
    doc.setTextColor(...primaryColor);
    doc.setFontSize(12);
    doc.setFont('helvetica', 'bold');
    doc.text('2. Conclusão & Abandono de Projetos (CPP/CAE)', 14, currentY);
    currentY += 4;

    autoTable(doc, {
      startY: currentY,
      head: [['Indicador', 'Valor']],
      body: [
        ['Total de Projetos Registrados', String(data.taxaProjetos?.totalProjetos ?? 0)],
        ['Projetos Concluídos com Sucesso', String(data.taxaProjetos?.projetosConcluidos ?? 0)],
        ['Projetos Cancelados / Abandonados', String(data.taxaProjetos?.projetosCancelados ?? 0)],
        ['Taxa de Conclusão %', `${data.taxaProjetos?.taxaConclusaoPercentual ?? 0}%`],
        ['Avaliação Executiva CPP/CAE', data.taxaProjetos?.avaliacaoExecutiva ?? 'N/A'],
      ],
      headStyles: { fillColor: primaryColor, textColor: 255, fontStyle: 'bold' },
      styles: { fontSize: 8.5, cellPadding: 2.5 },
      theme: 'grid',
    });

    currentY = (doc as any).lastAutoTable.finalY + 8;

    // 3. SLA, Impacto Social, Concorrência & Ociosidade
    doc.setTextColor(...primaryColor);
    doc.setFontSize(12);
    doc.setFont('helvetica', 'bold');
    doc.text('3. SLA Pastoral, Impacto Social & Patrimônio', 14, currentY);
    currentY += 4;

    autoTable(doc, {
      startY: currentY,
      head: [['Indicador', 'Resultado', 'Meta / Classificação']],
      body: [
        ['SLA Médio de Aprovação Pastoral', `${data.slaDespacho?.tempoMedioAprovacaoHoras ?? 0}h`, `Meta: ${data.slaDespacho?.slaMetaHoras ?? 0}h (${data.slaDespacho?.statusSla ?? 'N/A'})`],
        ['Percentual de Aprovações no Prazo', `${data.slaDespacho?.percentualDentroSla ?? 0}%`, data.slaDespacho?.statusSla ?? 'N/A'],
        ['Índice de Ação Missionária (Social)', `${data.impactoSocial?.coeficienteImpactoSocialPercentual ?? 0}%`, data.impactoSocial?.diagnosticoPastoral ?? 'N/A'],
        ['Ociosidade Patrimonial (Salas/Capelas)', `${data.ociosidadePatrimonial?.indiceOciosidadePercentual ?? 0}% (${data.ociosidadePatrimonial?.horasOciosasSemana ?? 0}h/sem)`, data.ociosidadePatrimonial?.recomendacaoCae ?? 'N/A'],
      ],
      headStyles: { fillColor: primaryColor, textColor: 255, fontStyle: 'bold' },
      styles: { fontSize: 8.5, cellPadding: 2.5 },
      theme: 'grid',
    });

    // Numeração de páginas no rodapé
    const totalPages = doc.getNumberOfPages();
    for (let i = 1; i <= totalPages; i++) {
      doc.setPage(i);
      doc.setFontSize(8);
      doc.setTextColor(150, 150, 150);
      doc.text(
        `Página ${i} de ${totalPages} — Sistema de Gestão Paroquial`,
        105,
        290,
        { align: 'center' }
      );
    }

    doc.save(filename);
    return true;
  } catch (error) {
    console.error('Erro ao gerar PDF de métricas estratégicas:', error);
    return false;
  }
};

/**
 * Gera um documento Microsoft Word (.docx) nativo formatado com tabelas e cabeçalhos estilizados.
 */
export const generateWordDocument = async (
  metrics: DashboardMetrics,
  filename: string = 'relatorio-gerencial.docx'
): Promise<boolean> => {
  try {
    const doc = new Document({
      sections: [
        {
          properties: {},
          children: [
            new Paragraph({
              text: 'PARÓQUIA NOSSA SENHORA DE FÁTIMA',
              heading: HeadingLevel.HEADING_1,
              alignment: AlignmentType.CENTER,
              spacing: { after: 100 },
            }),
            new Paragraph({
              children: [
                new TextRun({
                  text: 'Relatório Gerencial & Operacional Paroquial',
                  bold: true,
                  size: 24,
                  color: '1A237E',
                }),
              ],
              alignment: AlignmentType.CENTER,
              spacing: { after: 200 },
            }),
            new Paragraph({
              children: [
                new TextRun({
                  text: `Gerado em: ${new Date().toLocaleString('pt-BR')}`,
                  italics: true,
                  size: 18,
                  color: '666666',
                }),
              ],
              alignment: AlignmentType.RIGHT,
              spacing: { after: 300 },
            }),

            // 1. Resumo Geral
            new Paragraph({
              text: '1. Resumo Geral de Indicadores',
              heading: HeadingLevel.HEADING_2,
              spacing: { before: 200, after: 100 },
            }),
            new Table({
              width: { size: 100, type: WidthType.PERCENTAGE },
              rows: [
                new TableRow({
                  children: [
                    new TableCell({ children: [new Paragraph({ text: 'Indicador', bold: true })] }),
                    new TableCell({ children: [new Paragraph({ text: 'Valor', bold: true })] }),
                  ],
                }),
                new TableRow({
                  children: [
                    new TableCell({ children: [new Paragraph('Total de Eventos no Mês / Período')] }),
                    new TableCell({ children: [new Paragraph(String(metrics.resumoGeral.totalEventosMes))] }),
                  ],
                }),
                new TableRow({
                  children: [
                    new TableCell({ children: [new Paragraph('Projetos Pastorais Ativos')] }),
                    new TableCell({ children: [new Paragraph(String(metrics.resumoGeral.projetosAtivos))] }),
                  ],
                }),
                new TableRow({
                  children: [
                    new TableCell({ children: [new Paragraph('Solicitações Pendentes de Aprovação')] }),
                    new TableCell({ children: [new Paragraph(String(metrics.resumoGeral.solicitacoesPendentes))] }),
                  ],
                }),
              ],
            }),

            // 2. Distribuição de Eventos
            new Paragraph({
              text: '2. Distribuição de Eventos por Organização / Pastoral',
              heading: HeadingLevel.HEADING_2,
              spacing: { before: 300, after: 100 },
            }),
            new Table({
              width: { size: 100, type: WidthType.PERCENTAGE },
              rows: [
                new TableRow({
                  children: [
                    new TableCell({ children: [new Paragraph({ text: 'Organização / Pastoral', bold: true })] }),
                    new TableCell({ children: [new Paragraph({ text: 'Quantidade de Eventos', bold: true })] }),
                  ],
                }),
                ...(metrics.eventosPorOrganizacao && metrics.eventosPorOrganizacao.length > 0
                  ? metrics.eventosPorOrganizacao.map(
                      (item) =>
                        new TableRow({
                          children: [
                            new TableCell({ children: [new Paragraph(item.organizacaoNome)] }),
                            new TableCell({ children: [new Paragraph(String(item.quantidadeEventos))] }),
                          ],
                        })
                    )
                  : [
                      new TableRow({
                        children: [
                          new TableCell({ children: [new Paragraph('Nenhuma organização registrada')] }),
                          new TableCell({ children: [new Paragraph('0')] }),
                        ],
                      }),
                    ]),
              ],
            }),

            // 3. Projetos Ativos
            new Paragraph({
              text: '3. Acompanhamento de Projetos Pastorais',
              heading: HeadingLevel.HEADING_2,
              spacing: { before: 300, after: 100 },
            }),
            new Table({
              width: { size: 100, type: WidthType.PERCENTAGE },
              rows: [
                new TableRow({
                  children: [
                    new TableCell({ children: [new Paragraph({ text: 'Projeto', bold: true })] }),
                    new TableCell({ children: [new Paragraph({ text: 'Status', bold: true })] }),
                    new TableCell({ children: [new Paragraph({ text: 'Tempo Decorrido', bold: true })] }),
                    new TableCell({ children: [new Paragraph({ text: 'Eventos (Realizados/Total)', bold: true })] }),
                    new TableCell({ children: [new Paragraph({ text: 'Situação', bold: true })] }),
                  ],
                }),
                ...(metrics.projetosAtivosDetalhes && metrics.projetosAtivosDetalhes.length > 0
                  ? metrics.projetosAtivosDetalhes.map(
                      (proj) =>
                        new TableRow({
                          children: [
                            new TableCell({ children: [new Paragraph(proj.nome)] }),
                            new TableCell({ children: [new Paragraph(proj.status || '-')] }),
                            new TableCell({ children: [new Paragraph(`${Math.round(proj.percentualTempoDecorrido)}%`)] }),
                            new TableCell({ children: [new Paragraph(`${proj.eventosRealizados} / ${proj.totalEventos}`)] }),
                            new TableCell({ children: [new Paragraph(proj.emRisco ? 'EM RISCO' : 'Normal')] }),
                          ],
                        })
                    )
                  : [
                      new TableRow({
                        children: [
                          new TableCell({ children: [new Paragraph('Nenhum projeto ativo')] }),
                          new TableCell({ children: [new Paragraph('-')] }),
                          new TableCell({ children: [new Paragraph('0%')] }),
                          new TableCell({ children: [new Paragraph('0/0')] }),
                          new TableCell({ children: [new Paragraph('Normal')] }),
                        ],
                      }),
                    ]),
              ],
            }),

            // 4. Lista de Atividades e Eventos no Período
            ...(metrics.eventosSemana && metrics.eventosSemana.length > 0
              ? [
                  new Paragraph({
                    text: '4. Lista de Atividades e Eventos no Período',
                    heading: HeadingLevel.HEADING_2,
                    spacing: { before: 300, after: 100 },
                  }),
                  new Table({
                    width: { size: 100, type: WidthType.PERCENTAGE },
                    rows: [
                      new TableRow({
                        children: [
                          new TableCell({ children: [new Paragraph({ text: 'Título do Evento', bold: true })] }),
                          new TableCell({ children: [new Paragraph({ text: 'Organização', bold: true })] }),
                          new TableCell({ children: [new Paragraph({ text: 'Local', bold: true })] }),
                          new TableCell({ children: [new Paragraph({ text: 'Data / Horário', bold: true })] }),
                        ],
                      }),
                      ...metrics.eventosSemana.map(
                        (ev) =>
                          new TableRow({
                            children: [
                              new TableCell({ children: [new Paragraph(ev.titulo)] }),
                              new TableCell({ children: [new Paragraph(ev.organizacaoNome || 'Paróquia')] }),
                              new TableCell({ children: [new Paragraph(ev.localNome || 'Sem Local')] }),
                              new TableCell({
                                children: [
                                  new Paragraph(
                                    ev.inicio
                                      ? new Date(ev.inicio).toLocaleString('pt-BR', {
                                          day: '2-digit',
                                          month: '2-digit',
                                          year: 'numeric',
                                          hour: '2-digit',
                                          minute: '2-digit',
                                        })
                                      : '-'
                                  ),
                                ],
                              }),
                            ],
                          })
                      ),
                    ],
                  }),
                ]
              : []),
          ],
        },
      ],
    });

    const blob = await Packer.toBlob(doc);
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename.endsWith('.docx') ? filename : `${filename}.docx`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    return true;
  } catch (error) {
    console.error('Erro ao gerar documento Word:', error);
    return false;
  }
};

/**
 * Gera documento Word (.docx) nativo das Métricas Estratégicas & Preditivas.
 */
export const generateStrategicWordDocument = async (
  data: MetricasEstrategicasPainelResponse,
  filename: string = 'metricas-estrategicas.docx'
): Promise<boolean> => {
  try {
    const doc = new Document({
      sections: [
        {
          properties: {},
          children: [
            new Paragraph({
              text: 'PARÓQUIA NOSSA SENHORA DE FÁTIMA',
              heading: HeadingLevel.HEADING_1,
              alignment: AlignmentType.CENTER,
              spacing: { after: 100 },
            }),
            new Paragraph({
              children: [
                new TextRun({
                  text: 'Relatório Executivo — Métricas Estratégicas & Preditivas',
                  bold: true,
                  size: 24,
                  color: '1A237E',
                }),
              ],
              alignment: AlignmentType.CENTER,
              spacing: { after: 200 },
            }),
            new Paragraph({
              children: [
                new TextRun({
                  text: `Gerado em: ${new Date().toLocaleString('pt-BR')}`,
                  italics: true,
                  size: 18,
                  color: '666666',
                }),
              ],
              alignment: AlignmentType.RIGHT,
              spacing: { after: 300 },
            }),

            // 1. Sazonalidade Litúrgica
            new Paragraph({
              text: '1. Sazonalidade Litúrgica (Predictive Analytics)',
              heading: HeadingLevel.HEADING_2,
              spacing: { before: 200, after: 100 },
            }),
            new Table({
              width: { size: 100, type: WidthType.PERCENTAGE },
              rows: [
                new TableRow({
                  children: [
                    new TableCell({ children: [new Paragraph({ text: 'Ciclo Litúrgico', bold: true })] }),
                    new TableCell({ children: [new Paragraph({ text: 'Fator Demanda', bold: true })] }),
                    new TableCell({ children: [new Paragraph({ text: 'Histórico', bold: true })] }),
                    new TableCell({ children: [new Paragraph({ text: 'Projeção', bold: true })] }),
                    new TableCell({ children: [new Paragraph({ text: 'Nível Demanda', bold: true })] }),
                  ],
                }),
                ...(data.sazonalidadeLiturgica
                  ? data.sazonalidadeLiturgica.map(
                      (s) =>
                        new TableRow({
                          children: [
                            new TableCell({ children: [new Paragraph(s.cicloNome)] }),
                            new TableCell({ children: [new Paragraph(`${s.fatorDemanda}x`)] }),
                            new TableCell({ children: [new Paragraph(String(s.quantidadeEventosHistoricos))] }),
                            new TableCell({ children: [new Paragraph(String(s.quantidadeEventosPrevistos))] }),
                            new TableCell({ children: [new Paragraph(s.nivelDemanda)] }),
                          ],
                        })
                    )
                  : []),
              ],
            }),

            // 2. Conclusão de Projetos & SLA
            new Paragraph({
              text: '2. Projetos Pastorais, SLA & Impacto Social',
              heading: HeadingLevel.HEADING_2,
              spacing: { before: 300, after: 100 },
            }),
            new Table({
              width: { size: 100, type: WidthType.PERCENTAGE },
              rows: [
                new TableRow({
                  children: [
                    new TableCell({ children: [new Paragraph({ text: 'Indicador', bold: true })] }),
                    new TableCell({ children: [new Paragraph({ text: 'Valor', bold: true })] }),
                    new TableCell({ children: [new Paragraph({ text: 'Avaliação Executiva', bold: true })] }),
                  ],
                }),
                new TableRow({
                  children: [
                    new TableCell({ children: [new Paragraph('Taxa de Conclusão de Projetos')] }),
                    new TableCell({ children: [new Paragraph(`${data.taxaProjetos?.taxaConclusaoPercentual ?? 0}% (${data.taxaProjetos?.projetosConcluidos ?? 0}/${data.taxaProjetos?.totalProjetos ?? 0})`)] }),
                    new TableCell({ children: [new Paragraph(data.taxaProjetos?.avaliacaoExecutiva ?? 'N/A')] }),
                  ],
                }),
                new TableRow({
                  children: [
                    new TableCell({ children: [new Paragraph('SLA Médio de Aprovação Pastoral')] }),
                    new TableCell({ children: [new Paragraph(`${data.slaDespacho?.tempoMedioAprovacaoHoras ?? 0}h (${data.slaDespacho?.percentualDentroSla ?? 0}% dentro do prazo)`)] }),
                    new TableCell({ children: [new Paragraph(data.slaDespacho?.statusSla ?? 'N/A')] }),
                  ],
                }),
                new TableRow({
                  children: [
                    new TableCell({ children: [new Paragraph('Coeficiente de Impacto Social (Caridade/Saída)')] }),
                    new TableCell({ children: [new Paragraph(`${data.impactoSocial?.coeficienteImpactoSocialPercentual ?? 0}% (${data.impactoSocial?.eventosSociaisCaridade ?? 0} sociais / ${data.impactoSocial?.reunioesInternas ?? 0} reuniões)`)] }),
                    new TableCell({ children: [new Paragraph(data.impactoSocial?.diagnosticoPastoral ?? 'N/A')] }),
                  ],
                }),
                new TableRow({
                  children: [
                    new TableCell({ children: [new Paragraph('Ociosidade Patrimonial (Locais)')] }),
                    new TableCell({ children: [new Paragraph(`${data.ociosidadePatrimonial?.indiceOciosidadePercentual ?? 0}% (${data.ociosidadePatrimonial?.horasOciosasSemana ?? 0}h ociosas/sem)`)] }),
                    new TableCell({ children: [new Paragraph(data.ociosidadePatrimonial?.recomendacaoCae ?? 'N/A')] }),
                  ],
                }),
              ],
            }),
          ],
        },
      ],
    });

    const blob = await Packer.toBlob(doc);
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename.endsWith('.docx') ? filename : `${filename}.docx`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    return true;
  } catch (error) {
    console.error('Erro ao gerar Word de métricas estratégicas:', error);
    return false;
  }
};

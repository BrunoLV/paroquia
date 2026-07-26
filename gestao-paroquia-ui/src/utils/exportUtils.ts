import { DashboardMetrics } from '../api/types';
import * as htmlToImage from 'html-to-image';

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
 * Captura um elemento pelo ID e gera uma versão impressa/PDF via html-to-image.
 */
export const exportElementAsPDF = async (elementId: string, filename: string): Promise<boolean> => {
  const element = document.getElementById(elementId);
  if (!element) {
    console.error(`Element with id "${elementId}" not found for PDF export.`);
    return false;
  }

  try {
    const dataUrl = await htmlToImage.toPng(element, { quality: 0.95, backgroundColor: '#ffffff' });
    
    // Abre uma janela limpa com a imagem formatada para impressão/PDF
    const printWindow = window.open('', '_blank');
    if (!printWindow) {
      // Caso popups estejam bloqueados, dispara download da imagem diretamente
      const link = document.createElement('a');
      link.download = filename.replace(/\.pdf$/i, '.png');
      link.href = dataUrl;
      link.click();
      return true;
    }

    printWindow.document.write(`
      <!DOCTYPE html>
      <html>
        <head>
          <title>${filename}</title>
          <style>
            body { margin: 0; padding: 20px; text-align: center; font-family: sans-serif; }
            img { max-width: 100%; height: auto; border: 1px solid #ccc; }
            @media print {
              body { padding: 0; }
              img { border: none; width: 100%; }
            }
          </style>
        </head>
        <body>
          <img src="${dataUrl}" alt="Relatório" onload="window.print(); window.close();" />
        </body>
      </html>
    `);
    printWindow.document.close();
    printWindow.focus();
    return true;
  } catch (error) {
    console.error('Error rendering PDF/image export:', error);
    return false;
  }
};

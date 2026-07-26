import { generateMetricsCSV, downloadCSV, exportElementAsPDF } from './exportUtils';
import { DashboardMetrics } from '../api/types';
import * as htmlToImage from 'html-to-image';

jest.mock('html-to-image');

describe('exportUtils', () => {
  const mockMetrics: DashboardMetrics = {
    resumoGeral: {
      totalEventosMes: 15,
      projetosAtivos: 3,
      solicitacoesPendentes: 2,
    },
    eventosPorOrganizacao: [
      { organizacaoNome: 'Catequese', quantidadeEventos: 10 },
      { organizacaoNome: 'Pastoral da Família', quantidadeEventos: 5 },
    ],
    projetosAtivosDetalhes: [
      {
        nome: 'Reforma da Igreja',
        status: 'EM_ANDAMENTO',
        percentualTempoDecorrido: 45,
        totalEventos: 4,
        eventosRealizados: 2,
        emRisco: false,
        envolvidos: ['João', 'Maria'],
      },
    ],
    eventosSemana: [
      {
        titulo: 'Missa Dominical',
        inicio: '2026-07-26T10:00:00Z',
        localNome: 'Matriz',
        organizacaoNome: 'Clero',
      },
    ],
  };

  describe('generateMetricsCSV', () => {
    it('should format metrics into CSV string containing general summary and projects', () => {
      const csv = generateMetricsCSV(mockMetrics);

      expect(csv).toContain('RESUMO GERAL DE METRICAS');
      expect(csv).toContain('Total Eventos no Mes,15');
      expect(csv).toContain('Projetos Ativos,3');
      expect(csv).toContain('Solicitacoes Pendentes,2');
      expect(csv).toContain('DISTRIBUICAO DE EVENTOS POR ORGANIZACAO');
      expect(csv).toContain('"Catequese",10');
      expect(csv).toContain('PROJETOS ATIVOS');
      expect(csv).toContain('"Reforma da Igreja",EM_ANDAMENTO,45%,4,2,Nao');
    });

    it('should handle empty or null metrics gracefully', () => {
      const emptyMetrics: DashboardMetrics = {
        resumoGeral: { totalEventosMes: 0, projetosAtivos: 0, solicitacoesPendentes: 0 },
        eventosPorOrganizacao: [],
        projetosAtivosDetalhes: [],
        eventosSemana: [],
      };

      const csv = generateMetricsCSV(emptyMetrics);
      expect(csv).toContain('Total Eventos no Mes,0');
    });
  });

  describe('downloadCSV', () => {
    it('should create blob link and click it to download', () => {
      const createObjectURLMock = jest.fn(() => 'blob:http://localhost/mock-url');
      const revokeObjectURLMock = jest.fn();
      global.URL.createObjectURL = createObjectURLMock;
      global.URL.revokeObjectURL = revokeObjectURLMock;

      const linkEl = document.createElement('a');
      const clickMock = jest.fn();
      linkEl.click = clickMock;

      const createElementSpy = jest.spyOn(document, 'createElement').mockReturnValue(linkEl);

      downloadCSV('header1,header2\nval1,val2', 'relatorio.csv');

      expect(createElementSpy).toHaveBeenCalledWith('a');
      expect(clickMock).toHaveBeenCalled();
      expect(createObjectURLMock).toHaveBeenCalled();

      createElementSpy.mockRestore();
    });
  });

  describe('exportElementAsPDF', () => {
    beforeEach(() => {
      document.body.innerHTML = '<div id="report-container"><h1>Relatorio</h1></div>';
    });

    it('should capture element using html-to-image toPng and open print / download image', async () => {
      (htmlToImage.toPng as jest.Mock).mockResolvedValue('data:image/png;base64,fakeimage');

      const windowOpenMock = jest.fn().mockReturnValue({
        document: {
          write: jest.fn(),
          close: jest.fn(),
        },
        focus: jest.fn(),
        print: jest.fn(),
      });
      window.open = windowOpenMock;

      const result = await exportElementAsPDF('report-container', 'relatorio.pdf');

      expect(result).toBe(true);
      expect(htmlToImage.toPng).toHaveBeenCalled();
    });

    it('should return false if element is not found', async () => {
      const spyConsole = jest.spyOn(console, 'error').mockImplementation(() => {});
      const result = await exportElementAsPDF('invalid-id', 'relatorio.pdf');
      expect(result).toBe(false);
      spyConsole.mockRestore();
    });

    it('should return false if htmlToImage fails', async () => {
      const spyConsole = jest.spyOn(console, 'error').mockImplementation(() => {});
      (htmlToImage.toPng as jest.Mock).mockRejectedValue(new Error('Canvas error'));
      const result = await exportElementAsPDF('report-container', 'relatorio.pdf');
      expect(result).toBe(false);
      spyConsole.mockRestore();
    });
  });
});

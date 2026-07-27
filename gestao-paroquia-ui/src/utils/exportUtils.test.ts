import {downloadCSV, exportElementAsPDF, generateMetricsCSV} from './exportUtils';
import {DashboardMetrics} from '../api/types';
import {toPng} from 'html-to-image';
import {vi} from 'vitest';

vi.mock('html-to-image', () => ({
  toPng: vi.fn(),
  toSvg: vi.fn(),
}));

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
    eventosSemana: [],
    projetosAtivosDetalhes: [
      {
        nome: 'Reforma da Igreja',
        status: 'EM_ANDAMENTO',
        percentualTempoDecorrido: 75,
        totalEventos: 4,
        eventosRealizados: 3,
        emRisco: false,
        envolvidos: [],
      },
    ],
  };

  describe('generateMetricsCSV', () => {
    it('should format metrics into CSV string containing general summary and projects', () => {
      const csv = generateMetricsCSV(mockMetrics);

      expect(csv).toContain('RESUMO GERAL DE METRICAS');
      expect(csv).toContain('Total Eventos no Mes,15');
      expect(csv).toContain('"Catequese",10');
      expect(csv).toContain('"Reforma da Igreja",EM_ANDAMENTO,75%,4,3,Nao');
    });

    it('should handle empty or null metrics gracefully', () => {
      const emptyMetrics: DashboardMetrics = {
        resumoGeral: { totalEventosMes: 0, projetosAtivos: 0, solicitacoesPendentes: 0 },
        eventosPorOrganizacao: [],
        projetosAtivosDetalhes: [],
        eventosSemana: [],
      };
      const csv = generateMetricsCSV(emptyMetrics);
      expect(csv).toContain('RESUMO GERAL DE METRICAS');
    });
  });

  describe('downloadCSV', () => {
    it('should create blob link and click it to download', () => {
      const createObjectURLMock = vi.fn().mockReturnValue('blob:http://localhost/fake-url');
      const revokeObjectURLMock = vi.fn();
      window.URL.createObjectURL = createObjectURLMock;
      window.URL.revokeObjectURL = revokeObjectURLMock;

      const mockLink = document.createElement('a');
      const clickSpy = vi.spyOn(mockLink, 'click').mockImplementation(() => {});
      const createElementSpy = vi.spyOn(document, 'createElement').mockReturnValue(mockLink);

      downloadCSV('test,data', 'export.csv');

      expect(createElementSpy).toHaveBeenCalledWith('a');
      expect(clickSpy).toHaveBeenCalled();
      expect(createObjectURLMock).toHaveBeenCalled();

      createElementSpy.mockRestore();
    });
  });

  describe('exportElementAsPDF', () => {
    beforeEach(() => {
      document.body.innerHTML = '<div id="report-container"><h1>Relatorio</h1></div>';
    });

    it('should capture element using html-to-image toPng and open print / download image', async () => {
      vi.mocked(toPng).mockResolvedValue('data:image/png;base64,fakeimage');

      const windowOpenMock = vi.fn().mockReturnValue({
        document: {
          write: vi.fn(),
          close: vi.fn(),
        },
        focus: vi.fn(),
        print: vi.fn(),
      });
      window.open = windowOpenMock;

      const result = await exportElementAsPDF('report-container', 'relatorio.pdf');

      expect(result).toBe(true);
      expect(toPng).toHaveBeenCalled();
    });

    it('should return false if element is not found', async () => {
      const spyConsole = vi.spyOn(console, 'error').mockImplementation(() => {});
      const result = await exportElementAsPDF('invalid-id', 'relatorio.pdf');
      expect(result).toBe(false);
      spyConsole.mockRestore();
    });

    it('should return false if htmlToImage fails', async () => {
      const spyConsole = vi.spyOn(console, 'error').mockImplementation(() => {});
      vi.mocked(toPng).mockRejectedValue(new Error('Canvas error'));
      const result = await exportElementAsPDF('report-container', 'relatorio.pdf');
      expect(result).toBe(false);
      spyConsole.mockRestore();
    });
  });
});

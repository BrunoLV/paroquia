import {
  downloadCSV,
  generateMetricsCSV,
  generateStrategicMetricsCSV,
  generateStrategicStructuredPDF,
  generateStrategicWordDocument,
  generateStructuredPDF,
  generateWordDocument,
} from './exportUtils';
import { DashboardMetrics } from '../api/types';
import { MetricasEstrategicasPainelResponse } from '../api/metricasTypes';
import { describe, expect, it, vi } from 'vitest';

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
    eventosSemana: [
      {
        titulo: 'Missa das Crianças',
        inicio: '2026-08-10T10:00:00Z',
        localNome: 'Igreja Matriz',
        organizacaoNome: 'Catequese',
      },
    ],
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

  const mockStrategicMetrics: MetricasEstrategicasPainelResponse = {
    sazonalidadeLiturgica: [
      {
        cicloNome: 'Quaresma',
        tipoCiclo: 'QUARESMA',
        fatorDemanda: 1.8,
        quantidadeEventosHistoricos: 25,
        quantidadeEventosPrevistos: 45,
        indiceSazonalidade: 1.8,
        nivelDemanda: 'PICO_OPERACIONAL',
      },
    ],
    taxaProjetos: {
      totalProjetos: 10,
      projetosConcluidos: 8,
      projetosEmAndamento: 2,
      projetosCancelados: 0,
      taxaConclusaoPercentual: 80,
      taxaAbandonoPercentual: 0,
      avaliacaoExecutiva: 'SAUDAVEL',
    },
    slaDespacho: {
      totalSolicitacoes: 30,
      solicitacoesAprovadas: 28,
      solicitacoesRejeitadas: 2,
      tempoMedioAprovacaoHoras: 12,
      slaMetaHoras: 24,
      percentualDentroSla: 93,
      statusSla: 'EXCELENTE',
    },
    impactoSocial: {
      totalEventos: 50,
      eventosSociaisCaridade: 35,
      reunioesInternas: 15,
      coeficienteImpactoSocialPercentual: 70,
      diagnosticoPastoral: 'MISSIONARIA_EM_SAIDA',
    },
    concorrenciaInterpastoral: {
      totalConflitosDetectados: 0,
      conflitos: [],
      nivelRiscoConcorrencia: 'BAIXO',
    },
    ociosidadePatrimonial: {
      totalLocais: 5,
      totalHorasUteisSemana: 200,
      horasOcupadasSemana: 120,
      horasOciosasSemana: 80,
      indiceOciosidadePercentual: 40,
      recomendacaoCae: 'EXCELENTE_APROVEITAMENTO',
    },
  };

  describe('generateMetricsCSV', () => {
    it('should format metrics into CSV string containing general summary and projects', () => {
      const csv = generateMetricsCSV(mockMetrics);

      expect(csv).toContain('RESUMO GERAL DE METRICAS');
      expect(csv).toContain('Total Eventos no Mes,15');
      expect(csv).toContain('"Catequese",10');
      expect(csv).toContain('"Reforma da Igreja",EM_ANDAMENTO,75%,4,3,Nao');
    });
  });

  describe('generateStrategicMetricsCSV', () => {
    it('should format strategic metrics into CSV string containing all 6 executive indicators', () => {
      const csv = generateStrategicMetricsCSV(mockStrategicMetrics);

      expect(csv).toContain('RELATORIO DE METRICAS ESTRATEGICAS E PREDITIVAS');
      expect(csv).toContain('"Quaresma",1.8x,25,45,Pico Operacional');
      expect(csv).toContain('Taxa Conclusao %,80%');
      expect(csv).toContain('Status SLA,Excelente');
      expect(csv).toContain('Diagnostico Pastoral,Missionária em Saída');
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

      createElementSpy.mockRestore();
    });
  });

  describe('generateStructuredPDF & generateStrategicStructuredPDF', () => {
    it('should generate a structured PDF for operational metrics', async () => {
      const result = await generateStructuredPDF(mockMetrics, 'relatorio-test.pdf');
      expect(result).toBe(true);
    });

    it('should generate a structured PDF for strategic metrics', async () => {
      const result = await generateStrategicStructuredPDF(mockStrategicMetrics, 'metricas-estrategicas-test.pdf');
      expect(result).toBe(true);
    });
  });

  describe('generateWordDocument & generateStrategicWordDocument', () => {
    it('should generate a docx blob for operational metrics', async () => {
      const createObjectURLMock = vi.fn().mockReturnValue('blob:http://localhost/fake-url');
      const revokeObjectURLMock = vi.fn();
      window.URL.createObjectURL = createObjectURLMock;
      window.URL.revokeObjectURL = revokeObjectURLMock;

      const mockLink = document.createElement('a');
      const clickSpy = vi.spyOn(mockLink, 'click').mockImplementation(() => {});
      const createElementSpy = vi.spyOn(document, 'createElement').mockReturnValue(mockLink);

      const result = await generateWordDocument(mockMetrics, 'relatorio-test.docx');

      expect(result).toBe(true);
      expect(clickSpy).toHaveBeenCalled();

      createElementSpy.mockRestore();
    });

    it('should generate a docx blob for strategic metrics', async () => {
      const createObjectURLMock = vi.fn().mockReturnValue('blob:http://localhost/fake-url');
      const revokeObjectURLMock = vi.fn();
      window.URL.createObjectURL = createObjectURLMock;
      window.URL.revokeObjectURL = revokeObjectURLMock;

      const mockLink = document.createElement('a');
      const clickSpy = vi.spyOn(mockLink, 'click').mockImplementation(() => {});
      const createElementSpy = vi.spyOn(document, 'createElement').mockReturnValue(mockLink);

      const result = await generateStrategicWordDocument(mockStrategicMetrics, 'metricas-estrategicas-test.docx');

      expect(result).toBe(true);
      expect(clickSpy).toHaveBeenCalled();

      createElementSpy.mockRestore();
    });
  });
});

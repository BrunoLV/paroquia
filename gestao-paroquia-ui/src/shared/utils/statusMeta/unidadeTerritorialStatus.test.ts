import { describe, it, expect } from 'vitest';
import {
  getUnidadeTerritorialTipoMeta,
  getSacrarioMeta,
  getPiaBatismalMeta,
  getAvcbStatusMeta,
} from './unidadeTerritorialStatus';

describe('unidadeTerritorialStatus', () => {
  describe('getUnidadeTerritorialTipoMeta', () => {
    it('deve retornar metadados de Igreja Matriz', () => {
      const meta = getUnidadeTerritorialTipoMeta('MATRIZ');
      expect(meta.label).toBe('Igreja Matriz');
      expect(meta.color).toBe('primary');
      expect(meta.tooltip).toContain('Matriz');
    });

    it('deve retornar metadados de Comunidade', () => {
      const meta = getUnidadeTerritorialTipoMeta('COMUNIDADE');
      expect(meta.label).toBe('Comunidade');
      expect(meta.color).toBe('default');
    });

    it('deve usar fallback de Comunidade para tipos desconhecidos ou nulos', () => {
      expect(getUnidadeTerritorialTipoMeta(null).label).toBe('Comunidade');
      expect(getUnidadeTerritorialTipoMeta('').label).toBe('Comunidade');
      expect(getUnidadeTerritorialTipoMeta('OUTRO').label).toBe('Comunidade');
    });
  });

  describe('getSacrarioMeta & getPiaBatismalMeta', () => {
    it('deve retornar metadados para Sacrário', () => {
      const meta = getSacrarioMeta();
      expect(meta.label).toBe('Sacrário');
      expect(meta.color).toBe('primary');
    });

    it('deve retornar metadados para Pia Batismal', () => {
      const meta = getPiaBatismalMeta();
      expect(meta.label).toBe('Pia Batismal');
      expect(meta.color).toBe('info');
    });
  });

  describe('getAvcbStatusMeta', () => {
    it('deve retornar Não informado quando ambos forem nulos ou vazios', () => {
      const meta = getAvcbStatusMeta(null, null);
      expect(meta.label).toBe('Não informado');
      expect(meta.color).toBe('default');
    });

    it('deve retornar Nº quando não houver data de validade', () => {
      const meta = getAvcbStatusMeta('98765', null);
      expect(meta.label).toBe('Nº 98765');
      expect(meta.color).toBe('default');
    });

    it('deve retornar AVCB Vencido quando data for no passado', () => {
      const dataPassada = '2020-01-01';
      const meta = getAvcbStatusMeta('12345', dataPassada);
      expect(meta.label).toBe('AVCB Vencido');
      expect(meta.color).toBe('error');
    });

    it('deve retornar AVCB Regular quando data for bem futura (> 60 dias)', () => {
      const dataFutura = '2030-12-31';
      const meta = getAvcbStatusMeta('12345', dataFutura);
      expect(meta.label).toBe('AVCB Regular');
      expect(meta.color).toBe('success');
    });

    it('deve retornar AVCB a Vencer quando faltarem até 60 dias', () => {
      const dataProxima = new Date();
      dataProxima.setDate(dataProxima.getDate() + 30);
      const dataStr = dataProxima.toISOString().slice(0, 10);
      const meta = getAvcbStatusMeta('12345', dataStr);
      expect(meta.label).toBe('AVCB a Vencer');
      expect(meta.color).toBe('warning');
    });
  });
});

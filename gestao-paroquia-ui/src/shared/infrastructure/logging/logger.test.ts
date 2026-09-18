import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import logger from './logger';

describe('ClientLogger (Telemetria e Observabilidade)', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    (globalThis as Record<string, unknown>).__TEST_ENABLE_LOGGER_NETWORK__ = true;
    logger.clearBuffer();
    vi.spyOn(console, 'info').mockImplementation(() => {});
    vi.spyOn(console, 'warn').mockImplementation(() => {});
    vi.spyOn(console, 'error').mockImplementation(() => {});
    vi.spyOn(console, 'debug').mockImplementation(() => {});
  });

  afterEach(() => {
    delete (globalThis as Record<string, unknown>).__TEST_ENABLE_LOGGER_NETWORK__;
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  it('deve acumular registros de log no buffer', () => {
    logger.info('Acesso a tela inicial', { componente: 'Dashboard' });
    logger.warn('Token expirando em 5 minutos');

    expect(logger.getBufferLength()).toBe(2);
  });

  it('deve formatar erro com stack trace no logger.error', () => {
    const fakeError = new Error('Falha simulada');
    logger.error('Erro de teste', fakeError, { componente: 'TestComponent' });

    expect(logger.getBufferLength()).toBe(1);
    expect(console.error).toHaveBeenCalled();
  });

  it('deve realizar flush automatico apos o intervalo configurado', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true });
    vi.stubGlobal('fetch', fetchMock);

    logger.info('Evento 1');
    expect(logger.getBufferLength()).toBe(1);

    vi.advanceTimersByTime(3500);

    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(logger.getBufferLength()).toBe(0);
  });

  it('deve realizar flush imediatamente ao atingir o limite de batch', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true });
    vi.stubGlobal('fetch', fetchMock);

    for (let i = 0; i < 10; i++) {
      logger.info(`Evento ${i}`);
    }

    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(logger.getBufferLength()).toBe(0);
  });

  it('deve tratar falha na API graciosamente sem propagar excecao', async () => {
    const fetchMock = vi.fn().mockRejectedValue(new Error('Network offline'));
    vi.stubGlobal('fetch', fetchMock);

    logger.info('Evento com rede indisponivel');
    await expect(logger.flush()).resolves.not.toThrow();
  });
});

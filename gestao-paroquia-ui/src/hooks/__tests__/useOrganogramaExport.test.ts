import {act, renderHook} from '@testing-library/react';
import {toPng, toSvg} from 'html-to-image';
import {useOrganogramaExport} from '../useOrganogramaExport';
import {vi} from 'vitest';

vi.mock('html-to-image', () => ({
  toPng: vi.fn(),
  toSvg: vi.fn(),
}));

describe('useOrganogramaExport', () => {
  let mockElement: HTMLElement;
  let elementRef: { current: HTMLElement | null };

  beforeEach(() => {
    vi.clearAllMocks();
    mockElement = document.createElement('div');
    elementRef = { current: mockElement };

    // Mock HTMLAnchorElement click for downloads
    vi.spyOn(document, 'createElement').mockImplementation((tagName: string) => {
      const elem = document.createElementNS('http://www.w3.org/1999/xhtml', tagName);
      if (tagName === 'a') {
        (elem as HTMLAnchorElement).click = vi.fn();
      }
      return elem;
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  test('deve iniciar com isExporting = false', () => {
    const { result } = renderHook(() => useOrganogramaExport());
    expect(result.current.isExporting).toBe(false);
  });

  test('deve gerar nome de arquivo no formato organograma_<nome>_<data>.<ext>', () => {
    const { result } = renderHook(() => useOrganogramaExport());
    const today = new Date().toISOString().split('T')[0];

    const filenamePng = result.current.generateFilename('Paróquia São José', 'png');
    expect(filenamePng).toBe(`organograma_paroquia-sao-jose_${today}.png`);

    const filenameSvg = result.current.generateFilename('Catedral', 'svg');
    expect(filenameSvg).toBe(`organograma_catedral_${today}.svg`);
  });

  test('deve definir isExporting = true durante a exportacao e restaurar para false ao completar', async () => {
    vi.mocked(toPng).mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve('data:image/png;base64,fake'), 50))
    );

    const { result } = renderHook(() => useOrganogramaExport());

    let exportPromise: Promise<void>;
    act(() => {
      exportPromise = result.current.exportOrganograma(elementRef, 'png', 'test.png');
    });

    expect(result.current.isExporting).toBe(true);

    await act(async () => {
      await exportPromise;
    });

    expect(result.current.isExporting).toBe(false);
  });

  test('deve chamar html-to-image toPng com o elemento e configuracao correta', async () => {
    vi.mocked(toPng).mockResolvedValue('data:image/png;base64,fake');

    const { result } = renderHook(() => useOrganogramaExport());

    await act(async () => {
      await result.current.exportOrganograma(elementRef, 'png', 'organograma.png');
    });

    expect(toPng).toHaveBeenCalledWith(mockElement, expect.any(Object));
  });

  test('deve chamar html-to-image toSvg com o elemento e configuracao correta', async () => {
    vi.mocked(toSvg).mockResolvedValue('data:image/svg+xml;base64,fake');

    const { result } = renderHook(() => useOrganogramaExport());

    await act(async () => {
      await result.current.exportOrganograma(elementRef, 'svg', 'organograma.svg');
    });

    expect(toSvg).toHaveBeenCalledWith(mockElement, expect.any(Object));
  });

  test('deve tratar erro de captura e restaurar isExporting = false', async () => {
    vi.mocked(toPng).mockRejectedValue(new Error('Erro ao capturar DOM'));

    const { result } = renderHook(() => useOrganogramaExport());

    await act(async () => {
      await expect(
        result.current.exportOrganograma(elementRef, 'png', 'organograma.png')
      ).rejects.toThrow('Erro ao capturar DOM');
    });

    expect(result.current.isExporting).toBe(false);
  });
});

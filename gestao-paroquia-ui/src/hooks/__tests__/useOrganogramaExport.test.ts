import { renderHook, act } from '@testing-library/react';
import * as htmlToImage from 'html-to-image';
import { useOrganogramaExport } from '../useOrganogramaExport';

jest.mock('html-to-image', () => ({
  toPng: jest.fn(),
  toSvg: jest.fn(),
}));

describe('useOrganogramaExport', () => {
  let mockElement: HTMLElement;
  let elementRef: { current: HTMLElement | null };

  beforeEach(() => {
    jest.clearAllMocks();
    mockElement = document.createElement('div');
    elementRef = { current: mockElement };

    // Mock HTMLAnchorElement click for downloads
    jest.spyOn(document, 'createElement').mockImplementation((tagName: string) => {
      const elem = document.createElementNS('http://www.w3.org/1999/xhtml', tagName);
      if (tagName === 'a') {
        (elem as HTMLAnchorElement).click = jest.fn();
      }
      return elem;
    });
  });

  afterEach(() => {
    jest.restoreAllMocks();
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
    (htmlToImage.toPng as jest.Mock).mockImplementation(
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
    (htmlToImage.toPng as jest.Mock).mockResolvedValue('data:image/png;base64,fake');

    const { result } = renderHook(() => useOrganogramaExport());

    await act(async () => {
      await result.current.exportOrganograma(elementRef, 'png', 'organograma.png');
    });

    expect(htmlToImage.toPng).toHaveBeenCalledWith(mockElement, expect.any(Object));
  });

  test('deve chamar html-to-image toSvg com o elemento e configuracao correta', async () => {
    (htmlToImage.toSvg as jest.Mock).mockResolvedValue('data:image/svg+xml;base64,fake');

    const { result } = renderHook(() => useOrganogramaExport());

    await act(async () => {
      await result.current.exportOrganograma(elementRef, 'svg', 'organograma.svg');
    });

    expect(htmlToImage.toSvg).toHaveBeenCalledWith(mockElement, expect.any(Object));
  });

  test('deve tratar erro de captura e restaurar isExporting = false', async () => {
    const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
    (htmlToImage.toPng as jest.Mock).mockRejectedValue(new Error('Erro ao capturar DOM'));

    const { result } = renderHook(() => useOrganogramaExport());

    await act(async () => {
      await expect(
        result.current.exportOrganograma(elementRef, 'png', 'organograma.png')
      ).rejects.toThrow('Erro ao capturar DOM');
    });

    expect(result.current.isExporting).toBe(false);
    consoleErrorSpy.mockRestore();
  });
});

import { useState, RefObject, useCallback } from 'react';
import { toPng, toSvg } from 'html-to-image';

export type ExportFormat = 'png' | 'svg';

export interface UseOrganogramaExportReturn {
  isExporting: boolean;
  exportOrganograma: (
    elementRef: RefObject<HTMLElement | null>,
    format: ExportFormat,
    customFilename?: string
  ) => Promise<void>;
  generateFilename: (orgName?: string, format?: ExportFormat) => string;
}

type ExtendedCSSStyleDeclaration = CSSStyleDeclaration & {
  zoom?: string;
};

interface ModifiedElementState {
  el: HTMLElement;
  origZoom: string;
  origOverflowX: string;
  origOverflowY: string;
  origOverflow: string;
  origWidth: string;
  origMaxWidth: string;
}

export function useOrganogramaExport(): UseOrganogramaExportReturn {
  const [isExporting, setIsExporting] = useState<boolean>(false);

  const generateFilename = useCallback((orgName: string = 'paroquia', format: ExportFormat = 'png'): string => {
    const today = new Date().toISOString().split('T')[0];
    const sanitizedOrg = orgName
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-z0-9]/g, '-')
      .replace(/-+/g, '-')
      .replace(/^-|-$/g, '');
    return `organograma_${sanitizedOrg || 'paroquia'}_${today}.${format}`;
  }, []);

  const downloadFile = (dataUrl: string, filename: string): void => {
    const link = document.createElement('a');
    link.download = filename;
    link.href = dataUrl;
    link.click();
  };

  const exportOrganograma = useCallback(
    async (
      elementRef: RefObject<HTMLElement | null>,
      format: ExportFormat,
      customFilename?: string
    ): Promise<void> => {
      const element = elementRef.current;
      if (!element) {
        throw new Error('Elemento do organograma não encontrado para exportação');
      }

      setIsExporting(true);

      const modifiedElements: ModifiedElementState[] = [];

      try {
        // 1. Encontrar as maiores dimensões reais de conteúdo unclipped
        let fullWidth = element.scrollWidth;
        let fullHeight = element.scrollHeight;

        const allDescendants = element.querySelectorAll('*');
        allDescendants.forEach((child) => {
          if (child instanceof HTMLElement) {
            if (child.scrollWidth > fullWidth) {
              fullWidth = child.scrollWidth;
            }
            if (child.scrollHeight > fullHeight) {
              fullHeight = child.scrollHeight;
            }
          }
        });

        // Adicionar padding extra de 32px para garantir margens limpas
        fullWidth = Math.max(fullWidth + 32, element.offsetWidth);
        fullHeight = Math.max(fullHeight + 32, element.offsetHeight);

        // 2. Desativar qualquer barra de rolagem / overflow em todos os contêineres filhos
        // e remover limites de largura para impedir que o navegador renderize barras de rolagem
        allDescendants.forEach((child) => {
          if (child instanceof HTMLElement) {
            const style = child.style as ExtendedCSSStyleDeclaration;
            const computedStyle = window.getComputedStyle(child);

            const isScrollContainer =
              computedStyle.overflowX === 'auto' ||
              computedStyle.overflowX === 'scroll' ||
              computedStyle.overflowY === 'auto' ||
              computedStyle.overflowY === 'scroll' ||
              computedStyle.overflow === 'auto' ||
              computedStyle.overflow === 'scroll' ||
              Boolean(style.overflowX) ||
              Boolean(style.overflowY) ||
              Boolean(style.overflow);

            const hasZoom = Boolean(style.zoom);

            if (isScrollContainer || hasZoom) {
              modifiedElements.push({
                el: child,
                origZoom: style.zoom || '',
                origOverflowX: style.overflowX,
                origOverflowY: style.overflowY,
                origOverflow: style.overflow,
                origWidth: style.width,
                origMaxWidth: style.maxWidth,
              });

              style.zoom = '1';
              style.overflow = 'visible';
              style.overflowX = 'visible';
              style.overflowY = 'visible';
              if (isScrollContainer) {
                style.width = 'max-content';
                style.maxWidth = 'none';
              }
            }
          }
        });

        const options = {
          quality: 0.95,
          pixelRatio: 2,
          backgroundColor: '#ffffff',
          cacheBust: true,
          width: fullWidth,
          height: fullHeight,
          style: {
            transform: 'none',
            zoom: '1',
            overflow: 'visible',
            width: `${fullWidth}px`,
            height: `${fullHeight}px`,
            maxHeight: 'none',
            maxWidth: 'none',
          },
          filter: (node: Node) => {
            if (node instanceof HTMLElement && node.classList.contains('no-export')) {
              return false;
            }
            return true;
          },
        };

        let dataUrl: string;
        if (format === 'svg') {
          dataUrl = await toSvg(element, options);
        } else {
          dataUrl = await toPng(element, options);
        }

        const filename = customFilename || generateFilename('paroquia', format);
        downloadFile(dataUrl, filename);
      } catch (error) {
        console.error('Erro ao exportar organograma:', error);
        throw error;
      } finally {
        // 3. Restaurar fielmente todos os estilos originais dos elementos
        modifiedElements.forEach(({ el, origZoom, origOverflowX, origOverflowY, origOverflow, origWidth, origMaxWidth }) => {
          const style = el.style as ExtendedCSSStyleDeclaration;
          style.zoom = origZoom;
          style.overflowX = origOverflowX;
          style.overflowY = origOverflowY;
          style.overflow = origOverflow;
          style.width = origWidth;
          style.maxWidth = origMaxWidth;
        });

        setIsExporting(false);
      }
    },
    [generateFilename]
  );

  return {
    isExporting,
    exportOrganograma,
    generateFilename,
  };
}

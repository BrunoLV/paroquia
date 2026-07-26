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

      const modifiedElements: { el: HTMLElement; origZoom: string; origOverflowX: string; origOverflowY: string }[] = [];

      try {
        // Encontrar as maiores dimensões reais de conteúdo unclipped
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

        // Temporariamente resetar zoom e remover scrollbars/clipping durante a captura
        allDescendants.forEach((child) => {
          if (child instanceof HTMLElement) {
            const style = child.style as ExtendedCSSStyleDeclaration;
            if (style.zoom || style.overflowX || style.overflowY || style.overflow) {
              modifiedElements.push({
                el: child,
                origZoom: style.zoom || '',
                origOverflowX: style.overflowX,
                origOverflowY: style.overflowY,
              });
              style.zoom = '1';
              style.overflowX = 'visible';
              style.overflowY = 'visible';
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
        // Restaurar os estilos originais dos elementos modificados
        modifiedElements.forEach(({ el, origZoom, origOverflowX, origOverflowY }) => {
          const style = el.style as ExtendedCSSStyleDeclaration;
          style.zoom = origZoom;
          style.overflowX = origOverflowX;
          style.overflowY = origOverflowY;
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

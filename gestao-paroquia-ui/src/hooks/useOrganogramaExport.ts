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

      try {
        const options = {
          quality: 0.95,
          pixelRatio: 2,
          backgroundColor: '#ffffff',
          cacheBust: true,
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

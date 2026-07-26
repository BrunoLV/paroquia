import { useState, RefObject } from 'react';

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

  const generateFilename = (orgName: string = 'paroquia', format: ExportFormat = 'png'): string => {
    const today = new Date().toISOString().split('T')[0];
    const sanitizedOrg = orgName
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-z0-9]/g, '-')
      .replace(/-+/g, '-')
      .replace(/^-|-$/g, '');
    return `organograma_${sanitizedOrg || 'paroquia'}_${today}.${format}`;
  };

  const exportOrganograma = async (
    _elementRef: RefObject<HTMLElement | null>,
    _format: ExportFormat,
    _customFilename?: string
  ): Promise<void> => {
    setIsExporting(true);
    try {
      // Stub a ser implementado em detalhes na Phase 2
    } finally {
      setIsExporting(false);
    }
  };

  return {
    isExporting,
    exportOrganograma,
    generateFilename,
  };
}

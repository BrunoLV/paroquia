import {RefObject, useCallback, useState} from 'react';
import {toPng, toSvg} from 'html-to-image';

/** Formatos suportados de exportação do organograma: imagem rasterizada (`png`) ou vetorial (`svg`). */
export type ExportFormat = 'png' | 'svg';

/**
 * Contrato retornado por `useOrganogramaExport`: estado de carregamento da exportação e as
 * duas ações que a tela do organograma (`Organograma.tsx`) precisa — gerar o arquivo e, se
 * quiser, montar um nome de arquivo padronizado antes de chamar a exportação.
 */
export interface UseOrganogramaExportReturn {
  isExporting: boolean;
  exportOrganograma: (
    elementRef: RefObject<HTMLElement | null>,
    format: ExportFormat,
    customFilename?: string
  ) => Promise<void>;
  generateFilename: (orgName?: string, format?: ExportFormat) => string;
}

/** `zoom` não faz parte do tipo padrão `CSSStyleDeclaration`, mas é usado (via inline style) por
 * alguns navegadores/bibliotecas para compensar escala de tela; estendido aqui só para poder
 * salvar/restaurar esse valor ao redor da captura de imagem. */
type ExtendedCSSStyleDeclaration = CSSStyleDeclaration & {
  zoom?: string;
};

/**
 * Snapshot do estado inline original de um elemento do DOM que precisou ser temporariamente
 * alterado (zoom e overflow removidos) para a captura funcionar sem cortar conteúdo com
 * scroll/zoom aplicado. Usado para reverter os estilos no `finally` da exportação, garantindo
 * que a UI volte ao normal mesmo se a exportação falhar no meio do caminho.
 */
interface ModifiedElementState {
  el: HTMLElement;
  origZoom: string;
  origOverflowX: string;
  origOverflowY: string;
  origOverflow: string;
  origWidth: string;
  origMaxWidth: string;
}

/**
 * Hook de exportação do organograma (`Organograma.tsx`) para arquivo de imagem (PNG) ou
 * vetorial (SVG), usando a biblioteca `html-to-image` para rasterizar/serializar o DOM do
 * componente `@dabeng/react-orgchart`.
 *
 * Não envolve `useQuery`/`useMutation` — não há chamada de rede nem cache do TanStack Query
 * aqui, é um efeito puramente client-side sobre o DOM. `isExporting` existe para a tela
 * desabilitar o botão de exportar e evitar cliques concorrentes enquanto a captura (que pode
 * levar segundos em organogramas grandes) está em andamento.
 *
 * Antes de capturar, o hook precisa contornar uma limitação do `html-to-image`: containers
 * com `overflow: auto/scroll` ou `zoom` aplicado são renderizados "recortados" (do jeito que
 * aparecem na tela, com scrollbar), então a captura ficaria cortada. Por isso o fluxo:
 * 1. mede a maior largura/altura real de conteúdo entre todos os descendentes (`scrollWidth`/
 *    `scrollHeight`), não apenas o elemento raiz, para descobrir as dimensões completas do
 *    organograma sem cortes;
 * 2. remove temporariamente `overflow`/`zoom`/limites de largura dos containers de scroll
 *    encontrados, guardando o estado original em `modifiedElements`;
 * 3. captura a imagem já expandida com essas dimensões completas;
 * 4. no `finally`, restaura fielmente os estilos originais — mesmo em caso de erro — para não
 *    deixar a tela do organograma com scroll/zoom quebrados após a exportação.
 */
export function useOrganogramaExport(): UseOrganogramaExportReturn {
  const [isExporting, setIsExporting] = useState<boolean>(false);

  /**
   * Monta um nome de arquivo padronizado `organograma_<org>_<data-iso>.<formato>`, normalizando
   * o nome da organização (minúsculas, sem acentos, sem caracteres especiais) para evitar
   * nomes de arquivo inválidos/estranhos no sistema operacional do usuário.
   */
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

  /**
   * Dispara o download do arquivo no navegador criando um link `<a>` temporário com o `dataUrl`
   * gerado pelo `html-to-image` — não sobe nada ao backend, a exportação é 100% client-side.
   */
  const downloadFile = (dataUrl: string, filename: string): void => {
    const link = document.createElement('a');
    link.download = filename;
    link.href = dataUrl;
    link.click();
  };

  /**
   * Executa a exportação do elemento referenciado por `elementRef` para PNG ou SVG e dispara
   * o download no navegador. Lança erro se o elemento não estiver montado (`elementRef.current`
   * nulo) — a tela chamadora deve tratar essa exceção (ex.: exibir notificação de erro).
   *
   * `options.filter` exclui da captura elementos marcados com as classes `no-export`/
   * `oc-export-btn` (ex.: o próprio botão de exportar, tooltips de ação), para o arquivo final
   * não incluir controles de UI que só fazem sentido na tela.
   */
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
            if (node instanceof HTMLElement) {
              if (
                node.classList.contains('no-export') ||
                node.classList.contains('oc-export-btn')
              ) {
                return false;
              }
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

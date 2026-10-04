import { useEffect } from 'react';
import { useConfiguracaoParoquia } from '@/contextos/governanca/presentation/hooks/useConfiguracaoParoquia';

export const API_LOGO_CLARO_URL = '/api/v1/configuracao-paroquia/logo/claro.png';
export const API_LOGO_ESCURO_URL = '/api/v1/configuracao-paroquia/logo/escuro.png';
export const STATIC_FAVICON_CLARO = '/logo-paroquia-claro.png';
export const STATIC_FAVICON_ESCURO = '/logo-paroquia-escuro.png';

export interface ResolveFaviconOptions {
  logoFundoClaroUrl?: string;
  logoFundoEscuroUrl?: string;
  temLogoFundoClaro?: boolean;
  temLogoFundoEscuro?: boolean;
}

/**
 * Determina as URLs de favicon para temas claro e escuro obtidas diretamente
 * via serviço REST da paróquia.
 */
export const resolveFaviconUrls = (options?: ResolveFaviconOptions) => {
  const lightUrl = options?.logoFundoClaroUrl || API_LOGO_CLARO_URL;
  const darkUrl = options?.logoFundoEscuroUrl || API_LOGO_ESCURO_URL;
  return { lightUrl, darkUrl };
};

/**
 * Atualiza no DOM os elementos <link rel="icon"> e <link rel="apple-touch-icon">.
 * Remove os nós antigos e insere novos nós recém-criados para forçar o repaint do
 * favicon no motor Blink (Google Chrome / Chromium).
 */
export const applyFaviconToDocument = (
  lightUrl: string,
  darkUrl: string,
  isBrowserDark: boolean
) => {
  if (typeof document === 'undefined') return;

  const activeUrl = isBrowserDark ? darkUrl : lightUrl;

  const existingLinks = document.querySelectorAll<HTMLLinkElement>(
    'link[rel*="icon"], link[rel="apple-touch-icon"]'
  );
  existingLinks.forEach((link) => {
    link.remove();
  });

  const newFavicon = document.createElement('link');
  newFavicon.id = 'app-favicon';
  newFavicon.rel = 'icon';
  newFavicon.type = 'image/png';
  newFavicon.href = activeUrl;
  document.head.appendChild(newFavicon);

  const newAppleTouchIcon = document.createElement('link');
  newAppleTouchIcon.id = 'app-apple-touch-icon';
  newAppleTouchIcon.rel = 'apple-touch-icon';
  newAppleTouchIcon.href = activeUrl;
  document.head.appendChild(newAppleTouchIcon);
};

/**
 * Hook que sincroniza os favicons com a preferência de contraste do navegador
 * obtendo os logotipos oficiais via serviço REST.
 */
export const useDynamicFavicon = () => {
  const { configuracao } = useConfiguracaoParoquia();

  useEffect(() => {
    if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') return;

    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');

    const update = () => {
      const isDark = mediaQuery.matches;
      const { lightUrl, darkUrl } = resolveFaviconUrls({
        logoFundoClaroUrl: configuracao?.logoFundoClaroUrl,
        logoFundoEscuroUrl: configuracao?.logoFundoEscuroUrl,
        temLogoFundoClaro: configuracao?.temLogoFundoClaro,
        temLogoFundoEscuro: configuracao?.temLogoFundoEscuro,
      });
      applyFaviconToDocument(lightUrl, darkUrl, isDark);
    };

    update();

    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', update);
    } else if ('addListener' in mediaQuery && typeof (mediaQuery as { addListener: unknown }).addListener === 'function') {
      (mediaQuery as unknown as { addListener: (listener: () => void) => void }).addListener(update);
    }

    return () => {
      if (mediaQuery.removeEventListener) {
        mediaQuery.removeEventListener('change', update);
      } else if ('removeListener' in mediaQuery && typeof (mediaQuery as { removeListener: unknown }).removeListener === 'function') {
        (mediaQuery as unknown as { removeListener: (listener: () => void) => void }).removeListener(update);
      }
    };
  }, [
    configuracao?.logoFundoClaroUrl,
    configuracao?.logoFundoEscuroUrl,
    configuracao?.temLogoFundoClaro,
    configuracao?.temLogoFundoEscuro,
  ]);
};

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook } from '@testing-library/react';
import React from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import {
  resolveFaviconUrls,
  applyFaviconToDocument,
  useDynamicFavicon,
  API_LOGO_CLARO_URL,
  API_LOGO_ESCURO_URL,
} from './useDynamicFavicon';
import * as useConfigHook from '@/contextos/governanca/presentation/hooks/useConfiguracaoParoquia';

describe('useDynamicFavicon', () => {
  describe('resolveFaviconUrls', () => {
    it('deve retornar URLs REST padrão para claro e escuro', () => {
      const urls = resolveFaviconUrls();
      expect(urls.lightUrl).toBe(API_LOGO_CLARO_URL);
      expect(urls.darkUrl).toBe(API_LOGO_ESCURO_URL);
    });

    it('deve usar as URLs fornecidas na configuração quando disponíveis', () => {
      const urls = resolveFaviconUrls({
        logoFundoClaroUrl: '/api/v1/configuracao-paroquia/logo/claro?v=2',
        logoFundoEscuroUrl: '/api/v1/configuracao-paroquia/logo/escuro?v=2',
      });
      expect(urls.lightUrl).toBe('/api/v1/configuracao-paroquia/logo/claro?v=2');
      expect(urls.darkUrl).toBe('/api/v1/configuracao-paroquia/logo/escuro?v=2');
    });
  });

  describe('applyFaviconToDocument', () => {
    beforeEach(() => {
      document.head.innerHTML = `
        <link id="app-favicon" rel="icon" type="image/png" href="/api/v1/configuracao-paroquia/logo/claro.png" />
        <link id="app-apple-touch-icon" rel="apple-touch-icon" href="/api/v1/configuracao-paroquia/logo/claro.png" />
      `;
    });

    afterEach(() => {
      document.head.innerHTML = '';
    });

    it('deve recriar os elementos de link e aplicar favicon claro quando navegador estiver em tema claro', () => {
      applyFaviconToDocument(API_LOGO_CLARO_URL, API_LOGO_ESCURO_URL, false);

      const favicon = document.querySelector<HTMLLinkElement>('link[rel="icon"]');
      const appleIcon = document.querySelector<HTMLLinkElement>('link[rel="apple-touch-icon"]');

      expect(favicon?.href).toContain(API_LOGO_CLARO_URL);
      expect(appleIcon?.href).toContain(API_LOGO_CLARO_URL);
    });

    it('deve recriar os elementos de link e aplicar favicon escuro quando navegador estiver em tema escuro', () => {
      applyFaviconToDocument(API_LOGO_CLARO_URL, API_LOGO_ESCURO_URL, true);

      const favicon = document.querySelector<HTMLLinkElement>('link[rel="icon"]');
      const appleIcon = document.querySelector<HTMLLinkElement>('link[rel="apple-touch-icon"]');

      expect(favicon?.href).toContain(API_LOGO_ESCURO_URL);
      expect(appleIcon?.href).toContain(API_LOGO_ESCURO_URL);
    });

    it('deve criar links no head se o documento estiver sem nenhuma tag de favicon', () => {
      document.head.innerHTML = '';
      applyFaviconToDocument(API_LOGO_CLARO_URL, API_LOGO_ESCURO_URL, false);

      const link = document.querySelector<HTMLLinkElement>('link[rel="icon"]');
      const appleIcon = document.querySelector<HTMLLinkElement>('link[rel="apple-touch-icon"]');
      expect(link).not.toBeNull();
      expect(link?.href).toContain(API_LOGO_CLARO_URL);
      expect(appleIcon).not.toBeNull();
      expect(appleIcon?.href).toContain(API_LOGO_CLARO_URL);
    });
  });

  describe('hook useDynamicFavicon', () => {
    let addEventListenerSpy: ReturnType<typeof vi.fn>;
    let removeEventListenerSpy: ReturnType<typeof vi.fn>;

    beforeEach(() => {
      document.head.innerHTML = `
        <link id="app-favicon" rel="icon" type="image/png" href="/api/v1/configuracao-paroquia/logo/claro.png" />
      `;

      addEventListenerSpy = vi.fn();
      removeEventListenerSpy = vi.fn();

      window.matchMedia = vi.fn().mockImplementation((query: string) => ({
        matches: query.includes('dark'),
        media: query,
        onchange: null,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: addEventListenerSpy,
        removeEventListener: removeEventListenerSpy,
        dispatchEvent: vi.fn(),
      }));
    });

    afterEach(() => {
      vi.restoreAllMocks();
      document.head.innerHTML = '';
    });

    it('deve registrar listener de mudança de esquema de cores e aplicar favicon adequado via REST', () => {
      vi.spyOn(useConfigHook, 'useConfiguracaoParoquia').mockReturnValue({
        // @ts-expect-error fixture parcial: o teste só preenche os campos que exercita
        configuracao: {
          id: '1',
          nome: 'Paróquia Teste',
          diocese: null,
          endereco: null,
          telefone: null,
          email: null,
          cnpj: null,
          corPrimaria: '#1A237E',
          corSecundaria: '#C5A059',
          temLogoFundoClaro: true,
          temLogoFundoEscuro: true,
          logoFundoClaroUrl: API_LOGO_CLARO_URL,
          logoFundoEscuroUrl: API_LOGO_ESCURO_URL,
        },
        isLoading: false,
        isError: false,
        error: null,
        refetch: vi.fn(),
        atualizarCores: {} as any,
        atualizarLogo: {} as any,
      });

      const queryClient = new QueryClient();
      const wrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
      );

      const { unmount } = renderHook(() => useDynamicFavicon(), { wrapper });

      expect(addEventListenerSpy).toHaveBeenCalledWith('change', expect.any(Function));

      const favicon = document.querySelector<HTMLLinkElement>('link[rel="icon"]');
      // matches=true para dark, então deve conter API_LOGO_ESCURO_URL
      expect(favicon?.href).toContain(API_LOGO_ESCURO_URL);

      unmount();
      expect(removeEventListenerSpy).toHaveBeenCalledWith('change', expect.any(Function));
    });
  });
});

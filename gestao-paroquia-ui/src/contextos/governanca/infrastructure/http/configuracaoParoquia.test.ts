import { describe, it, expect, vi, beforeEach } from 'vitest';
import apiClient from '@/shared/infrastructure/http/client';
import {
  obterConfiguracaoParoquia,
  atualizarIdentidadeVisual,
  atualizarLogoParoquia,
  normalizarConfiguracao,
  ParoquiaConfiguracao,
} from './configuracaoParoquia';

vi.mock('@/shared/infrastructure/http/client');

describe('configuracaoParoquia HTTP client', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('normalizarConfiguracao deve mapear propriedades com sufixo Hex e possui/tem', () => {
    const raw = {
      id: 'uuid-1',
      nome: 'Paróquia Teste',
      diocese: 'Diocese',
      endereco: 'Rua',
      telefone: '123',
      email: 'a@b.com',
      cnpj: '00',
      corPrimariaHex: '#001122',
      corSecundariaHex: '#334455',
      possuiLogoFundoClaro: true,
      possuiLogoFundoEscuro: false,
      urlLogoFundoClaro: '/api/v1/configuracao-paroquia/logo/claro',
      urlLogoFundoEscuro: '/api/v1/configuracao-paroquia/logo/escuro',
    };

    const normalizado = normalizarConfiguracao(raw);

    expect(normalizado.corPrimaria).toBe('#001122');
    expect(normalizado.corSecundaria).toBe('#334455');
    expect(normalizado.temLogoFundoClaro).toBe(true);
    expect(normalizado.temLogoFundoEscuro).toBe(false);
  });

  it('obterConfiguracaoParoquia deve chamar GET /configuracao-paroquia e normalizar', async () => {
    const rawBackendData = {
      id: 'uuid-1',
      nome: 'Paróquia Nossa Senhora de Fátima',
      diocese: 'Diocese de Teste',
      endereco: 'Rua Principal, 123',
      telefone: '(11) 9999-8888',
      email: 'contato@nsfatima.com',
      cnpj: '12.345.678/0001-90',
      corPrimariaHex: '#1976D2',
      corSecundariaHex: '#9C27B0',
      possuiLogoFundoClaro: true,
      possuiLogoFundoEscuro: true,
      urlLogoFundoClaro: '/api/v1/configuracao-paroquia/logo/claro',
      urlLogoFundoEscuro: '/api/v1/configuracao-paroquia/logo/escuro',
    };

    vi.mocked(apiClient.get).mockResolvedValueOnce({ data: rawBackendData });

    const result = await obterConfiguracaoParoquia();

    expect(apiClient.get).toHaveBeenCalledWith('/configuracao-paroquia');
    expect(result.corPrimaria).toBe('#1976D2');
    expect(result.corSecundaria).toBe('#9C27B0');
    expect(result.temLogoFundoClaro).toBe(true);
    expect(result.temLogoFundoEscuro).toBe(true);
  });

  it('atualizarIdentidadeVisual deve chamar PUT /configuracao-paroquia com chaves Hex', async () => {
    const payload = { corPrimaria: '#112233', corSecundaria: '#445566' };
    const rawResponse = {
      id: 'uuid-1',
      nome: 'Paróquia',
      diocese: null,
      endereco: null,
      telefone: null,
      email: null,
      cnpj: null,
      corPrimariaHex: '#112233',
      corSecundariaHex: '#445566',
      possuiLogoFundoClaro: true,
      possuiLogoFundoEscuro: true,
      urlLogoFundoClaro: '/api/v1/configuracao-paroquia/logo/claro',
      urlLogoFundoEscuro: '/api/v1/configuracao-paroquia/logo/escuro',
    };

    vi.mocked(apiClient.put).mockResolvedValueOnce({ data: rawResponse });

    const result = await atualizarIdentidadeVisual(payload);

    expect(apiClient.put).toHaveBeenCalledWith('/configuracao-paroquia', {
      corPrimariaHex: '#112233',
      corSecundariaHex: '#445566',
    });
    expect(result.corPrimaria).toBe('#112233');
    expect(result.corSecundaria).toBe('#445566');
  });

  it('atualizarLogoParoquia deve chamar PUT /configuracao-paroquia/logo/{variante} com FormData', async () => {
    vi.mocked(apiClient.put).mockResolvedValueOnce({ data: null });

    const mockFile = new File(['fake-png-content'], 'logo.png', { type: 'image/png' });
    await atualizarLogoParoquia('claro', mockFile);

    expect(apiClient.put).toHaveBeenCalledWith(
      '/configuracao-paroquia/logo/claro',
      expect.any(FormData),
      expect.objectContaining({
        headers: { 'Content-Type': 'multipart/form-data' },
      })
    );
  });
});

import { describe, it, expect, vi, beforeEach } from 'vitest';
import apiClient from '@/shared/infrastructure/http/client';
import {
  obterConfiguracaoParoquia,
  atualizarIdentidadeVisual,
  atualizarLogoParoquia,
  ParoquiaConfiguracao,
} from './configuracaoParoquia';

vi.mock('@/shared/infrastructure/http/client');

describe('configuracaoParoquia HTTP client', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('obterConfiguracaoParoquia deve chamar GET /configuracao-paroquia', async () => {
    const mockData: ParoquiaConfiguracao = {
      id: 'uuid-1',
      nome: 'Paróquia Nossa Senhora de Fátima',
      diocese: 'Diocese de Teste',
      endereco: 'Rua Principal, 123',
      telefone: '(11) 9999-8888',
      email: 'contato@nsfatima.com',
      cnpj: '12.345.678/0001-90',
      corPrimaria: '#1976D2',
      corSecundaria: '#9C27B0',
      temLogoFundoClaro: true,
      temLogoFundoEscuro: true,
      logoFundoClaroUrl: '/api/v1/configuracao-paroquia/logo/claro',
      logoFundoEscuroUrl: '/api/v1/configuracao-paroquia/logo/escuro',
    };

    vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockData });

    const result = await obterConfiguracaoParoquia();

    expect(apiClient.get).toHaveBeenCalledWith('/configuracao-paroquia');
    expect(result).toEqual(mockData);
  });

  it('atualizarIdentidadeVisual deve chamar PUT /configuracao-paroquia com payload de cores', async () => {
    const payload = { corPrimaria: '#112233', corSecundaria: '#445566' };
    const mockData = { id: 'uuid-1', ...payload };

    vi.mocked(apiClient.put).mockResolvedValueOnce({ data: mockData });

    const result = await atualizarIdentidadeVisual(payload);

    expect(apiClient.put).toHaveBeenCalledWith('/configuracao-paroquia', payload);
    expect(result).toEqual(mockData);
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

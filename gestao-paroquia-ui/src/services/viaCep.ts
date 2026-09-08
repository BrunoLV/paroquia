import { formatCep } from '@/utils/masks';

export interface ViaCepResponse {
  cep?: string;
  logradouro?: string;
  complemento?: string;
  bairro?: string;
  localidade?: string;
  uf?: string;
  erro?: boolean;
}

export interface EnderecoCepResult {
  cep: string;
  logradouro: string;
  bairro: string;
  cidade: string;
  uf: string;
}

/**
 * Consulta endereço por CEP no webservice público ViaCEP.
 * Retorna null se o CEP for inválido, incompleto ou não localizado.
 */
export const fetchAddressByCep = async (rawCep: string): Promise<EnderecoCepResult | null> => {
  const cleanCep = rawCep.replace(/\D/g, '');
  if (cleanCep.length !== 8) {
    return null;
  }

  try {
    const response = await fetch(`https://viacep.com.br/ws/${cleanCep}/json/`);
    if (!response.ok) {
      return null;
    }
    const data: ViaCepResponse = await response.json();
    if (data.erro) {
      return null;
    }

    return {
      cep: data.cep ? formatCep(data.cep) : formatCep(cleanCep),
      logradouro: data.logradouro || '',
      bairro: data.bairro || '',
      cidade: data.localidade || '',
      uf: (data.uf || '').toUpperCase(),
    };
  } catch {
    return null;
  }
};

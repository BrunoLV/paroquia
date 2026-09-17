import { extractErrorMessage } from './errorUtils';

describe('errorUtils', () => {
  it('returns fallback message when err is null or non-object', () => {
    expect(extractErrorMessage(null, 'Mensagem padrão')).toBe('Mensagem padrão');
    expect(extractErrorMessage('string error', 'Mensagem padrão')).toBe('Mensagem padrão');
  });

  it('returns fallback message when err is an Error instance without API response data', () => {
    expect(extractErrorMessage(new Error('Falha de conexão'), 'Mensagem padrão')).toBe('Mensagem padrão');
  });

  it('extracts data.message from Axios-like error object', () => {
    const axiosErr = {
      response: {
        data: {
          message: 'Usuário não possui permissão',
        },
      },
    };
    expect(extractErrorMessage(axiosErr, 'Mensagem padrão')).toBe('Usuário não possui permissão');
  });

  it('extracts list of errors from data.errors in Axios-like error object', () => {
    const axiosErr = {
      response: {
        data: {
          errors: [
            { message: 'Campo título é obrigatório' },
            { defaultMessage: 'Data de término inválida' },
          ],
        },
      },
    };
    const result = extractErrorMessage(axiosErr, 'Mensagem padrão');
    expect(result).toContain('Por favor, corrija os seguintes erros:');
    expect(result).toContain('• Campo título é obrigatório');
    expect(result).toContain('• Data de término inválida');
  });
});

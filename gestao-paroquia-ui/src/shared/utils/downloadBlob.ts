/**
 * Extrai o nome do arquivo do cabeçalho `Content-Disposition` (forma `filename*=UTF-8''...` ou `filename="..."`).
 * Se o navegador não expõe o cabeçalho (CORS) ou ele não traz nome, devolve o nome de reserva.
 */
export const nomeDoArquivo = (contentDisposition: string | undefined, reserva: string): string => {
  if (!contentDisposition) return reserva;
  const codificado = /filename\*=UTF-8''([^;]+)/i.exec(contentDisposition);
  if (codificado) {
    try {
      return decodeURIComponent(codificado[1]);
    } catch {
      return reserva;
    }
  }
  const simples = /filename="?([^";]+)"?/i.exec(contentDisposition);
  return simples ? simples[1] : reserva;
};

/** Entrega um Blob ao usuário como download e libera a URL temporária em seguida. */
export const baixarBlob = (blob: Blob, nome: string): void => {
  const url = URL.createObjectURL(blob);
  const ancora = document.createElement('a');
  ancora.href = url;
  ancora.download = nome;
  document.body.appendChild(ancora);
  ancora.click();
  document.body.removeChild(ancora);
  URL.revokeObjectURL(url);
};

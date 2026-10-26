import type { Mock as VitestMock, Mocked as VitestMocked, vi } from 'vitest';

/**
 * Alias de compatibilidade `jest.*` para a suíte legada: `setupTests.ts` publica `globalThis.jest = vi`,
 * e estes tipos espelham esse alias para que os testes sejam checados pelo `tsc` contra os tipos reais do Vitest.
 * Código novo deve importar `vi` de 'vitest'.
 */
declare global {
  const jest: typeof vi;
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace jest {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    type Mock<T extends (...args: any[]) => any = (...args: any[]) => any> = VitestMock<T>;
    type Mocked<T> = VitestMocked<T>;
  }
}

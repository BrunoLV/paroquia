/**
 * Forma estrutural mínima de um erro HTTP do cliente Axios (`AxiosError`), suficiente para
 * ler `status` e corpo da resposta sem que camadas fora de `infrastructure` precisem importar
 * `axios`. A detecção continua duck-typed (presença de `response`), como o interceptor já faz.
 */
export interface ApiHttpError<TData = unknown> {
  response?: { status?: number; data?: TData };
}

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  atualizarClerigo,
  criarClerigo,
  inativarClerigo,
  listarClerigos,
  listarDiretorioClero,
  obterClerigo,
} from '@/contextos/clero/infrastructure/http/clerigos';
import type { ClerigoPayload } from '@/shared/domain/types/types';

interface UseClerigosOptions {
  enabledConsulta?: boolean;
  enabledDiretorio?: boolean;
  clerigoId?: string;
}

/** Consulta `['clero']` e invalida essa chave após mutações administrativas. */
export const useClerigos = (options?: UseClerigosOptions) => {
  const client = useQueryClient();
  const consulta = useQuery({
    queryKey: ['clero'],
    queryFn: listarClerigos,
    enabled: options?.enabledConsulta ?? true,
  });
  const diretorio = useQuery({
    queryKey: ['clero', 'diretorio'],
    queryFn: listarDiretorioClero,
    enabled: options?.enabledDiretorio ?? true,
  });
  const detalhe = useQuery({
    queryKey: ['clero', options?.clerigoId],
    queryFn: () =>
      options?.clerigoId
        ? obterClerigo(options.clerigoId)
        : Promise.reject('ID não informado'),
    enabled: Boolean(options?.clerigoId),
  });
  const criar = useMutation({
    mutationFn: (payload: ClerigoPayload) => criarClerigo(payload),
    onSuccess: () => client.invalidateQueries({ queryKey: ['clero'] }),
  });
  const atualizar = useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: ClerigoPayload }) =>
      atualizarClerigo(id, payload),
    onSuccess: () => client.invalidateQueries({ queryKey: ['clero'] }),
  });
  const inativar = useMutation({
    mutationFn: inativarClerigo,
    onSuccess: () => client.invalidateQueries({ queryKey: ['clero'] }),
  });
  return { consulta, diretorio, detalhe, criar, atualizar, inativar };
};

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  atualizarIdentidadeVisual,
  atualizarLogoParoquia,
  obterConfiguracaoParoquia,
  type AtualizarIdentidadeVisualPayload,
  type ParoquiaConfiguracao,
} from '@/contextos/governanca/infrastructure/http/configuracaoParoquia';

export const CONFIGURACAO_PAROQUIA_QUERY_KEY = ['configuracao-paroquia'] as const;

export const useConfiguracaoParoquia = () => {
  const queryClient = useQueryClient();

  const query = useQuery<ParoquiaConfiguracao>({
    queryKey: CONFIGURACAO_PAROQUIA_QUERY_KEY,
    queryFn: obterConfiguracaoParoquia,
    staleTime: 1000 * 60 * 5, // 5 minutos de cache
  });

  const atualizarCores = useMutation({
    mutationFn: (payload: AtualizarIdentidadeVisualPayload) => atualizarIdentidadeVisual(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: CONFIGURACAO_PAROQUIA_QUERY_KEY });
    },
  });

  const atualizarLogo = useMutation({
    mutationFn: ({ variante, arquivo }: { variante: 'claro' | 'escuro'; arquivo: File }) =>
      atualizarLogoParoquia(variante, arquivo),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: CONFIGURACAO_PAROQUIA_QUERY_KEY });
    },
  });

  return {
    configuracao: query.data,
    isLoading: query.isLoading,
    isError: query.isError,
    error: query.error,
    refetch: query.refetch,
    atualizarCores,
    atualizarLogo,
  };
};

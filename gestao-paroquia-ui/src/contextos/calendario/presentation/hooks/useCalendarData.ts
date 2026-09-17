import { useEffect, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '@/contextos/iam/presentation/auth/AuthContext';
import { isGlobalCreator } from '@/contextos/iam/presentation/auth/permissions';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import apiClient from '@/shared/infrastructure/http/client';
import { getOrganizacoes } from '@/contextos/organizacao/infrastructure/http/organizacoes';
import { getAtivos } from '@/contextos/patrimonio/infrastructure/http/ativos';
import { getAnosParoquiais } from '@/contextos/governanca/infrastructure/http/anosParoquiais';
import { fetchSolenidadesLiturgicasAtivas } from '@/contextos/calendario/infrastructure/http/solenidades';
import { AprovacaoResponse, Evento, Local, Organizacao, Projeto } from '@/shared/domain/types/types';
import {
  buildSolemnitiesFromApi,
  getLiturgicalCycleLetter,
} from '@/shared/utils/liturgicalCalendar';
import { isSameDay } from '@/shared/utils/calendarHelpers';

type EventosApiResponse = { content: Evento[] } | Evento[];

interface UseCalendarDataParams {
  startOfGrid: Date;
  endOfGrid: Date;
  selectedOrgFilter: string;
  selectedCategoryFilter: string;
  currentYear: number;
}

/**
 * Concentra todo o "server state" do calendário (organizações, locais, projetos, ativos, anos
 * paroquiais, eventos do grid visível, aprovações pendentes e solenidades litúrgicas ativas) via
 * TanStack Query, e as derivações que dependem desses dados: eventos combinados com
 * pré-visualizações "fantasma" de solicitações pendentes, agenda ordenada, informações do ano
 * litúrgico corrente e helpers de exibição (nome de organização/local).
 *
 * A query de eventos usa `enabled: false` quando o usuário não pertence a nenhuma organização e
 * não é criador global (não há o que buscar) — mesma regra do fetch manual original.
 */
export const useCalendarData = ({
  startOfGrid,
  endOfGrid,
  selectedOrgFilter,
  selectedCategoryFilter,
  currentYear,
}: UseCalendarDataParams) => {
  const { user } = useAuth();
  const { showError } = useNotification();

  const organizacoesQuery = useQuery({ queryKey: ['organizacoes'], queryFn: getOrganizacoes });
  const locaisQuery = useQuery({
    queryKey: ['locais'],
    queryFn: async () => (await apiClient.get<Local[]>('/locais')).data ?? [],
  });
  const projetosQuery = useQuery({
    queryKey: ['projetos'],
    queryFn: async () => (await apiClient.get<Projeto[]>('/projetos')).data ?? [],
  });
  const ativosQuery = useQuery({ queryKey: ['ativos'], queryFn: getAtivos });
  const anosParoquiaisQuery = useQuery({ queryKey: ['anos-paroquiais'], queryFn: getAnosParoquiais });

  const organizations: Organizacao[] = organizacoesQuery.data ?? [];
  const locais = locaisQuery.data ?? [];
  const projetos = projetosQuery.data ?? [];
  const ativos = ativosQuery.data ?? [];
  const anosParoquiais = anosParoquiaisQuery.data ?? [];

  const { data: apiSolenidades = [] } = useQuery({
    queryKey: ['solenidadesLiturgicasAtivas'],
    queryFn: fetchSolenidadesLiturgicasAtivas,
  });

  const solemnities = useMemo(
    () => buildSolemnitiesFromApi(apiSolenidades, currentYear),
    [apiSolenidades, currentYear]
  );

  const liturgicalInfo = useMemo(() => {
    const cycleLetter = getLiturgicalCycleLetter(currentYear);
    const anoConfig = anosParoquiais.find((a) => a.ano === currentYear);
    const isClosed = anoConfig?.status === 'FECHADO';
    return {
      year: currentYear,
      cycle: `Ano ${cycleLetter} (${currentYear})`,
      letter: cycleLetter,
      isClosed,
    };
  }, [currentYear, anosParoquiais]);

  // Restrict organizations based on user role
  const filteredOrganizations = useMemo(() => {
    if (!user) return [];
    if (user.roles.includes('ADMIN') || user.roles.includes('ROLE_ADMIN')) return organizations;

    const userOrgIds = user.organizations.map((org) => org.id);
    return organizations.filter((org) => userOrgIds.includes(org.id));
  }, [user, organizations]);

  const userOrgIds = user && Array.isArray(user.organizations) ? user.organizations.map((org) => org.id) : [];
  const semOrganizacaoParaFiltrar = Boolean(user) && !selectedOrgFilter && !isGlobalCreator(user!) && userOrgIds.length === 0;

  const eventosQuery = useQuery({
    queryKey: ['eventos-calendario', startOfGrid.toISOString(), endOfGrid.toISOString(), selectedOrgFilter, selectedCategoryFilter, userOrgIds.join(','), user ? isGlobalCreator(user) : null],
    queryFn: async () => {
      const params: Record<string, unknown> = {
        dataInicio: startOfGrid.toISOString(),
        dataFim: endOfGrid.toISOString(),
        size: 200, // retrieve up to 200 events for full grid density
      };
      if (selectedOrgFilter) {
        params.organizacaoIds = [selectedOrgFilter];
      } else if (user && !isGlobalCreator(user) && userOrgIds.length > 0) {
        params.organizacaoIds = userOrgIds;
      }
      if (selectedCategoryFilter) {
        params.categoria = selectedCategoryFilter;
      }
      const res = await apiClient.get<EventosApiResponse>('/eventos', {
        params,
        // Ensure arrays are serialized as organizacaoIds=id1&organizacaoIds=id2
        paramsSerializer: { indexes: null },
      });
      if (res.data && !Array.isArray(res.data) && Array.isArray(res.data.content)) return res.data.content;
      if (Array.isArray(res.data)) return res.data;
      return [];
    },
    enabled: !semOrganizacaoParaFiltrar,
  });
  const events = semOrganizacaoParaFiltrar ? [] : (eventosQuery.data ?? []);

  const aprovacoesPendentesQuery = useQuery({
    queryKey: ['aprovacoes-pendentes-calendario'],
    queryFn: async () => {
      const res = await apiClient.get<{ content: AprovacaoResponse[] } | AprovacaoResponse[]>('/aprovacoes', {
        params: { status: 'PENDENTE', size: 100 },
      });
      if (res.data && !Array.isArray(res.data) && Array.isArray(res.data.content)) return res.data.content;
      if (Array.isArray(res.data)) return res.data;
      return [];
    },
  });
  const aprovacoesPendentes = aprovacoesPendentesQuery.data ?? [];

  const isLoading = eventosQuery.isFetching
    || locaisQuery.isFetching
    || projetosQuery.isFetching
    || aprovacoesPendentesQuery.isFetching
    || organizacoesQuery.isFetching
    || ativosQuery.isFetching
    || anosParoquiaisQuery.isFetching;

  const isError = eventosQuery.isError
    || locaisQuery.isError
    || projetosQuery.isError
    || aprovacoesPendentesQuery.isError
    || organizacoesQuery.isError
    || ativosQuery.isError
    || anosParoquiaisQuery.isError;

  // TanStack Query v5 removed per-query `onError`; watch `isError` instead to restore
  // the toast that existed before the hook-split refactor.
  useEffect(() => {
    if (isError) {
      showError('Não foi possível carregar as informações do calendário.');
    }
  }, [isError, showError]);

  // Reproduz o antigo `fetchData()` manual (refresh do calendário inteiro após uma
  // mutação, ou pelo botão de atualizar) via refetch de todas as queries do calendário.
  const refetchCalendario = () => {
    eventosQuery.refetch();
    locaisQuery.refetch();
    projetosQuery.refetch();
    aprovacoesPendentesQuery.refetch();
    organizacoesQuery.refetch();
    ativosQuery.refetch();
    anosParoquiaisQuery.refetch();
  };

  // Combine API events with transient events from pending creation requests
  //
  // O calendário precisa mostrar não só os eventos já confirmados (endpoint
  // /eventos), mas também o efeito de solicitações de aprovação ainda PENDENTES
  // (buscadas em aprovacoesPendentes), para que o usuário veja "o que vai acontecer
  // se isso for aprovado" sem que a mudança já tenha sido aplicada de fato — nenhum
  // desses eventos "pendentes" existe de verdade no backend ainda; é só uma
  // pré-visualização client-side construída a partir do payload da solicitação.
  const combinedEvents = useMemo(() => {
    const transientEvents = aprovacoesPendentes
      .filter((a) => a.tipoSolicitacao === 'CRIACAO_EVENTO' && a.actionPayloadJson)
      .map((a): Evento | null => {
        try {
          const payload = JSON.parse(a.actionPayloadJson as string);
          return {
            id: `pending-creation-${a.id}`,
            titulo: payload.titulo || 'Novo Evento (Pendente)',
            descricao: payload.descricao || '',
            categoria: payload.categoria || 'PASTORAL',
            grupoParoquialResponsavelId: payload.grupoParoquialResponsavelId || '',
            projetoId: payload.projetoId || null,
            nomeProjeto: null,
            localId: payload.localId || null,
            inicio: payload.inicio,
            fim: payload.fim,
            status: 'PENDENTE_CRIACAO', // custom status for pending creation
            estadoConflito: null,
            motivoConflito: null,
            aprovacaoPendenteId: a.id,
            solicitantePendenteId: a.solicitanteId,
            tipoSolicitacaoPendente: a.tipoSolicitacao,
          };
        } catch (e) {
          return null;
        }
      })
      .filter((e): e is Evento => e !== null);

    const decoratedEvents = events.map((e) => {
      const acaoPendente = aprovacoesPendentes.find((a) => a.eventoId === e.id);
      if (acaoPendente) {
        return {
          ...e,
          aprovacaoPendenteId: acaoPendente.id,
          solicitantePendenteId: acaoPendente.solicitanteId,
          tipoSolicitacaoPendente: acaoPendente.tipoSolicitacao,
          tipoAcaoPendente: acaoPendente.tipoSolicitacao,
        };
      }
      return e;
    });

    return [...decoratedEvents, ...transientEvents];
  }, [events, aprovacoesPendentes]);

  const getEventsForDay = (day: Date) => combinedEvents.filter((event) => isSameDay(new Date(event.inicio), day));

  const agendaEvents = useMemo(() => {
    return [...combinedEvents]
      .filter((e) => new Date(e.inicio) >= startOfGrid)
      .sort((a, b) => new Date(a.inicio).getTime() - new Date(b.inicio).getTime());
  }, [combinedEvents, startOfGrid]);

  const getOrgName = (orgId: string, igrejaVinculoId?: string | null) => {
    const org = organizations.find((o) => o.id === orgId);
    const orgName = org ? org.nome : 'Organização Paroquial';
    if (igrejaVinculoId) {
      const church = organizations.find((o) => o.id === igrejaVinculoId);
      if (church) {
        return `${orgName} - ${church.nome}`;
      }
    }
    return orgName;
  };

  const getLocalName = (localId: string | null) => {
    if (!localId) return null;
    const local = locais.find((l) => l.id === localId);
    return local ? local.nome : null;
  };

  return {
    organizations,
    filteredOrganizations,
    locais,
    projetos,
    ativos,
    anosParoquiais,
    solemnities,
    liturgicalInfo,
    combinedEvents,
    agendaEvents,
    aprovacoesPendentes,
    isLoading,
    refetchCalendario,
    getEventsForDay,
    getOrgName,
    getLocalName,
  };
};

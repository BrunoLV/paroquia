import {useCallback, useEffect, useMemo, useState} from 'react';
import {useQueryClient} from '@tanstack/react-query';
import apiClient from '@/api/client';
import {getOrganizacoes} from '@/api/organizacoes';
import {useAuth} from '@/auth/AuthContext';
import {useNotification} from '@/context/NotificationContext';
import {extractErrorMessage} from '@/utils/errorUtils';
import {Evento, Local, Organizacao, Projeto} from '@/api/types';

const isAdminUser = (roles?: string[]): boolean =>
  !!roles && (roles.includes('ADMIN') || roles.includes('ROLE_ADMIN'));

/**
 * Estado e ações do modal "Novo Evento" do Dashboard: carrega os metadados que o formulário
 * precisa (organizações permitidas, locais, projetos ativos), a lista de próximos eventos exibida
 * na tela, e trata a criação do evento (com recorrência opcional e `Idempotency-Key`),
 * invalidando o cache `['dashboard']` no sucesso.
 *
 * É um hook imperativo (Axios direto) porque estes recursos já são servidos por endpoints
 * distintos sem um hook de query dedicado; concentrá-los aqui mantém `Dashboard.tsx` como um
 * container fino de composição.
 */
export const useDashboardEventoModal = () => {
  const {user} = useAuth();
  const {showError} = useNotification();
  const queryClient = useQueryClient();

  const [events, setEvents] = useState<Evento[]>([]);
  const [organizations, setOrganizations] = useState<Organizacao[]>([]);
  const [locais, setLocais] = useState<Local[]>([]);
  const [projetos, setProjetos] = useState<Projeto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchEvents = useCallback(async () => {
    const now = new Date();
    const nextMonth = new Date();
    nextMonth.setDate(now.getDate() + 30);
    const params: Record<string, unknown> = {
      dataInicio: now.toISOString(),
      dataFim: nextMonth.toISOString(),
      size: 5,
      sort: 'inicioUtc,asc',
    };
    if (!isAdminUser(user?.roles)) {
      const userOrgIds = user?.organizations.map((org) => org.id) ?? [];
      if (userOrgIds.length === 0) {
        setEvents([]);
        return;
      }
      params.organizacaoIds = userOrgIds;
    }
    const res = await apiClient.get<{content?: Evento[]} | Evento[]>('/eventos', {
      params,
      paramsSerializer: {indexes: null},
    });
    const data = res.data;
    if (data && !Array.isArray(data) && Array.isArray(data.content)) setEvents(data.content);
    else if (Array.isArray(data)) setEvents(data);
  }, [user]);

  const fetchData = useCallback(async () => {
    setIsLoading(true);
    setError('');
    try {
      const [orgsData, locaisRes, projetosRes] = await Promise.all([
        getOrganizacoes().catch(() => []),
        apiClient.get<Local[]>('/locais').catch(() => ({data: []})),
        apiClient.get<Projeto[]>('/projetos').catch(() => ({data: []})),
      ]);
      if (Array.isArray(orgsData)) setOrganizations(orgsData);
      if (Array.isArray(locaisRes.data)) setLocais(locaisRes.data);
      if (Array.isArray(projetosRes.data)) setProjetos(projetosRes.data);
      await fetchEvents();
    } catch (err) {
      setError('Não foi possível carregar os dados do dashboard.');
    } finally {
      setIsLoading(false);
    }
  }, [fetchEvents]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleCreateEvent = useCallback(async (data: Record<string, unknown>) => {
    setIsSubmitting(true);
    try {
      const {recurrence, ...eventBaseData} = data;
      const requestData = {
        ...eventBaseData,
        projetoId: data.projetoId || null,
        localId: data.localId || null,
        participantes: data.participantes || [],
      };
      const idempotencyKey =
        typeof crypto !== 'undefined' && crypto.randomUUID
          ? crypto.randomUUID()
          : Math.random().toString(36).substring(2) + Date.now().toString(36);
      const res = await apiClient.post('/eventos', requestData, {headers: {'Idempotency-Key': idempotencyKey}});
      if (recurrence && res.data?.id) {
        await apiClient.put(`/eventos/${res.data.id}/recorrencia`, recurrence);
      }
      setIsModalOpen(false);
      queryClient.invalidateQueries({queryKey: ['dashboard']});
      fetchData();
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao criar evento.'));
    } finally {
      setIsSubmitting(false);
    }
  }, [queryClient, fetchData, showError]);

  const allowedOrgs = useMemo(() => {
    if (!user) return [];
    if (isAdminUser(user.roles)) return organizations;
    const userOrgIds = user.organizations.map((org) => org.id);
    return organizations.filter((org) => userOrgIds.includes(org.id));
  }, [user, organizations]);

  return {
    isLoading,
    error,
    events,
    locais,
    projetos,
    allowedOrgs,
    isModalOpen,
    isSubmitting,
    openModal: () => setIsModalOpen(true),
    closeModal: () => setIsModalOpen(false),
    handleCreateEvent,
  };
};

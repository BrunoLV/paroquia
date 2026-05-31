import React, { useState, useEffect } from 'react';
import {
  Grid,
  Typography,
  Box,
  Stack,
  CircularProgress,
  Alert,
} from '@mui/material';
import SummaryCards from '../components/dashboard/SummaryCards';
import UpcomingEvents from '../components/dashboard/UpcomingEvents';
import QuickActions from '../components/dashboard/QuickActions';
import PendingApprovals from '../components/dashboard/PendingApprovals';
import apiClient from '../api/client';
import EventRegistrationModal from '../components/EventRegistrationModal';
import { useAuth } from '../auth/AuthContext';
import { useNotification } from '../context/NotificationContext';
import { Evento, Organizacao, Local, Projeto } from '../api/types';

const Dashboard = () => {
  const { user } = useAuth();
  const { showError } = useNotification();
  const [events, setEvents] = useState<Evento[]>([]);
  const [organizations, setOrganizations] = useState<Organizacao[]>([]);
  const [locais, setLocais] = useState<Local[]>([]);
  const [projetos, setProjetos] = useState<Projeto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  
  // Modal state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchData = async () => {
    setIsLoading(true);
    setError('');
    try {
      // Fetch organizations list independently
      const fetchOrgs = async () => {
        try {
          const orgsRes = await apiClient.get<Organizacao[]>('/organizacoes');
          if (orgsRes.data && Array.isArray(orgsRes.data)) {
            setOrganizations(orgsRes.data);
          }
        } catch (err) {
          console.error('Error fetching organizations:', err);
        }
      };

      // Fetch locales list
      const fetchLocais = async () => {
        try {
          const res = await apiClient.get<Local[]>('/locais');
          if (res.data && Array.isArray(res.data)) {
            setLocais(res.data);
          }
        } catch (err) {
          console.error('Error fetching locales:', err);
        }
      };

      // Fetch projects list
      const fetchProjetos = async () => {
        try {
          const res = await apiClient.get<Projeto[]>('/projetos');
          if (res.data && Array.isArray(res.data)) {
            setProjetos(res.data);
          }
        } catch (err) {
          console.error('Error fetching projects:', err);
        }
      };

      // Fetch upcoming events (next 30 days)
      const fetchEvents = async () => {
        const now = new Date();
        const nextMonth = new Date();
        nextMonth.setDate(now.getDate() + 30);

        const params: any = {
          dataInicio: now.toISOString(),
          dataFim: nextMonth.toISOString(),
          size: 5,
          sort: 'inicioUtc,asc' // Corrected field name to match backend Entity property
        };

        if (user && !user.roles.includes('ADMIN')) {
          const userOrgIds = user.organizations.map(org => org.id);
          if (userOrgIds.length > 0) {
            params.organizacaoIds = userOrgIds;
          } else {
            setEvents([]);
            return;
          }
        }

        const eventsRes = await apiClient.get<any>('/eventos', {
          params,
          paramsSerializer: {
            indexes: null
          }
        });

        if (eventsRes.data && Array.isArray(eventsRes.data.content)) {
          setEvents(eventsRes.data.content);
        } else if (Array.isArray(eventsRes.data)) {
          setEvents(eventsRes.data);
        }
      };

      await Promise.all([fetchOrgs(), fetchLocais(), fetchProjetos(), fetchEvents()]);
    } catch (err: any) {
      console.error('Error fetching dashboard data:', err);
      setError('Não foi possível carregar os dados do dashboard.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  const handleCreateEvent = async (data: any) => {
    setIsSubmitting(true);
    try {
      const { recurrence, ...eventBaseData } = data;
      const requestData = {
        ...eventBaseData,
        projetoId: data.projetoId || null,
        localId: data.localId || null,
        participantes: data.participantes || [],
      };

      const idempotencyKey = typeof crypto !== 'undefined' && crypto.randomUUID
          ? crypto.randomUUID()
          : Math.random().toString(36).substring(2) + Date.now().toString(36);

      const res = await apiClient.post('/eventos', requestData, {
        headers: { 'Idempotency-Key': idempotencyKey }
      });

      // If recurrence is enabled, call the recurrence endpoint
      if (recurrence && res.data?.id) {
        await apiClient.put(`/eventos/${res.data.id}/recorrencia`, recurrence);
      }

      setIsModalOpen(false);
      fetchData();
    } catch (err: any) {
      showError(err.response?.data?.message || 'Erro ao criar evento.');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading && events.length === 0) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 4, fontWeight: 700, color: '#37474f' }}>
        Dashboard
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

      <Grid container spacing={3}>
        <Grid size={{ xs: 12 }}>
          <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
            Visão Geral
          </Typography>
          <SummaryCards metrics={{ monthlyEvents: events.length, pendingApprovals: 0 }} />
        </Grid>

        <Grid size={{ xs: 12, md: 8 }}>
          <UpcomingEvents events={events} isLoading={isLoading} />
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Stack spacing={3}>
            <QuickActions onNewEvent={() => setIsModalOpen(true)} />
            <PendingApprovals />
          </Stack>
        </Grid>
      </Grid>

      <EventRegistrationModal
        open={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleCreateEvent}
        isSubmitting={isSubmitting}
        organizations={organizations.filter(org => org.ativo)}
        locais={locais.filter(l => l.ativo)}
        projetos={projetos.filter(p => p.status === 'ATIVO')}
      />
    </Box>
  );
};

export default Dashboard;

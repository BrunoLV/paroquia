import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Avatar,
  Collapse,
  Button,
  Grid,
  Skeleton,
  Alert,
  Container,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
  AccountTree as AccountTreeIcon,
  People as PeopleIcon,
  Church as ChurchIcon,
  SupervisorAccount as SupervisorAccountIcon,
} from '@mui/icons-material';
import { getOrganograma } from '../api/organizacoes';
import { OrganogramaNode } from '../api/types';

const Organograma: React.FC = () => {
  const { data: organograma, isLoading, isError } = useQuery({
    queryKey: ['organograma'],
    queryFn: getOrganograma,
  });

  const [expandedMap, setExpandedMap] = useState<Record<string, boolean>>({
    'grupo-pastorais': true,
  });

  const toggleExpand = (id: string) => {
    setExpandedMap(prev => ({
      ...prev,
      [id]: !prev[id],
    }));
  };

  const getSubtypeLabel = (subtype: string) => {
    switch (subtype?.toUpperCase()) {
      case 'CLERO':
        return 'Clero Paroquial';
      case 'CONSELHO':
        return 'Conselho Pastoral Paroquial';
      case 'PASTORAL':
        return 'Pastoral';
      case 'MOVIMENTO':
        return 'Movimento';
      case 'LAICATO':
        return 'Laicato';
      default:
        return subtype || '';
    }
  };

  const getRoleBadgeColor = (role: string) => {
    switch (role?.toUpperCase()) {
      case 'PAROCO':
        return '#d32f2f'; // Red
      case 'VIGARIO':
      case 'PADRE':
        return '#f57c00'; // Orange
      case 'COORDENADOR':
        return '#1976d2'; // Blue
      case 'VICE_COORDENADOR':
      case 'VICE-COORDENADOR':
        return '#388e3c'; // Green
      case 'SECRETARIO':
        return '#7b1fa2'; // Purple
      default:
        return '#757575'; // Grey
    }
  };

  const renderMemberNode = (member: OrganogramaNode) => {
    const roleColor = getRoleBadgeColor(member.subtype);
    const initials = member.label
      .split(' ')
      .slice(0, 2)
      .map(n => n[0])
      .join('')
      .toUpperCase();

    return (
      <Card
        key={member.id}
        sx={{
          mb: 1.5,
          width: '100%',
          maxWidth: 280,
          mx: 'auto',
          borderLeft: `5px solid ${roleColor}`,
          borderRadius: 2,
          boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
          transition: 'all 0.2s ease-in-out',
          '&:hover': {
            transform: 'translateY(-2px)',
            boxShadow: '0 4px 12px rgba(0,0,0,0.12)',
          },
        }}
      >
        <CardContent sx={{ display: 'flex', alignItems: 'center', p: '12px !important', gap: 1.5 }}>
          <Avatar sx={{ width: 36, height: 36, bgcolor: roleColor, fontSize: '0.9rem', fontWeight: 'bold' }}>
            {initials}
          </Avatar>
          <Box sx={{ minWidth: 0 }}>
            <Typography variant="subtitle2" noWrap sx={{ fontWeight: 600 }}>
              {member.label}
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', textTransform: 'capitalize' }}>
              {member.subtype?.toLowerCase().replace('-', ' ')}
            </Typography>
          </Box>
        </CardContent>
      </Card>
    );
  };

  const renderOrgNode = (org: OrganogramaNode) => {
    const isExpanded = expandedMap[org.id] ?? false;
    const members = org.children || [];

    // Separar coordenadores/vice para exibir em destaque na capa do card
    const coord = org.responsavelNome;
    const vice = org.viceResponsavelNome;

    // Filtrar os membros da lista para que não fiquem repetidos abaixo
    const ordinaryMembers = members.filter(
      m =>
        !equalsIgnoreCase(m.label, coord) &&
        !equalsIgnoreCase(m.label, vice)
    );

    return (
      <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={org.id}>
        <Card
          sx={{
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            borderRadius: 3,
            boxShadow: '0 4px 14px rgba(0,0,0,0.08)',
            border: '1px solid #eef2f6',
            background: 'linear-gradient(to bottom, #ffffff, #fafafa)',
          }}
        >
          <Box
            sx={{
              p: 2,
              bgcolor: '#f5f7fb',
              borderBottom: '1px solid #eef2f6',
              borderTopLeftRadius: 12,
              borderTopRightRadius: 12,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
            }}
          >
            <Box>
              <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#1a237e' }}>
                {org.label}
              </Typography>
              <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 500 }}>
                {getSubtypeLabel(org.subtype)}
              </Typography>
            </Box>
            <ChurchIcon sx={{ color: '#1a237e', opacity: 0.8 }} />
          </Box>

          <CardContent sx={{ flexGrow: 1, p: 2, display: 'flex', flexDirection: 'column', gap: 1.5 }}>
            {/* Lideranças */}
            {coord && (
              <Box>
                <Typography variant="caption" sx={{ fontWeight: 600, color: 'text.secondary' }}>
                  {org.subtype === 'CLERO' ? 'Pároco' : 'Coordenador(a)'}
                </Typography>
                <Typography variant="body2" sx={{ fontWeight: 600, color: '#333' }}>
                  {coord}
                </Typography>
              </Box>
            )}

            {vice && (
              <Box>
                <Typography variant="caption" sx={{ fontWeight: 600, color: 'text.secondary' }}>
                  {org.subtype === 'CLERO' ? 'Vigário / Padres' : 'Vice-Coordenador(a)'}
                </Typography>
                <Typography variant="body2" sx={{ fontWeight: 600, color: '#555' }}>
                  {vice}
                </Typography>
              </Box>
            )}

            {/* Ações de expansão de membros comuns */}
            {ordinaryMembers.length > 0 && (
              <Box sx={{ mt: 'auto', pt: 1 }}>
                <Button
                  size="small"
                  fullWidth
                  onClick={() => toggleExpand(org.id)}
                  endIcon={isExpanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                  sx={{
                    justifyContent: 'space-between',
                    color: '#1a237e',
                    fontWeight: 600,
                    textTransform: 'none',
                  }}
                >
                  {isExpanded ? 'Ocultar Membros' : `Ver Membros (${ordinaryMembers.length})`}
                </Button>

                <Collapse in={isExpanded} timeout="auto" unmountOnExit>
                  <Box sx={{ mt: 1.5, pt: 1.5, borderTop: '1px dashed #e2e8f0', display: 'flex', flexDirection: 'column' }}>
                    {ordinaryMembers.map(renderMemberNode)}
                  </Box>
                </Collapse>
              </Box>
            )}
          </CardContent>
        </Card>
      </Grid>
    );
  };

  if (isLoading) {
    return (
      <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
        <Box sx={{ mb: 4 }}>
          <Skeleton width={300} height={40} />
          <Skeleton width={450} height={20} />
        </Box>
        <Grid container spacing={3}>
          {[1, 2, 3].map(i => (
            <Grid size={{ xs: 12 }} key={i}>
              <Skeleton variant="rectangular" height={150} sx={{ borderRadius: 3 }} />
            </Grid>
          ))}
        </Grid>
      </Container>
    );
  }

  if (isError || !organograma) {
    return (
      <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
        <Alert severity="error">
          Erro ao carregar a estrutura do organograma. Por favor, tente novamente mais tarde.
        </Alert>
      </Container>
    );
  }

  // Separar clero, conselho e pastorais
  const cleroNode = organograma.children.find(c => c.subtype === 'CLERO');
  const conselhoNode = organograma.children.find(c => c.subtype === 'CONSELHO');
  const pastoraisGrupoNode = organograma.children.find(c => c.id === 'grupo-pastorais');

  return (
    <Box sx={{ mt: 3, mb: 4, px: { xs: 2, md: 4 } }}>
      {/* Header */}
      <Box sx={{ mb: 4, display: 'flex', alignItems: 'center', gap: 2 }}>
        <Box sx={{ p: 1.5, bgcolor: 'rgba(26, 35, 126, 0.08)', borderRadius: 3, display: 'flex' }}>
          <AccountTreeIcon sx={{ fontSize: 32, color: '#1a237e' }} />
        </Box>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 800, color: '#1a237e', mb: 0.5 }}>
            Estrutura Organizacional
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
            Visualização completa da hierarquia e lideranças da paróquia.
          </Typography>
        </Box>
      </Box>

      {/* Root Node (A Paróquia) */}
      <Box sx={{ display: 'flex', justifyContent: 'center', mb: 5 }}>
        <Card
          sx={{
            minWidth: 320,
            maxWidth: 400,
            borderRadius: 4,
            background: 'linear-gradient(135deg, #1a237e 0%, #311b92 100%)',
            color: 'white',
            boxShadow: '0 8px 24px rgba(26, 35, 126, 0.25)',
            position: 'relative',
            overflow: 'hidden',
          }}
        >
          <Box
            sx={{
              position: 'absolute',
              top: -20,
              right: -20,
              width: 100,
              height: 100,
              borderRadius: '50%',
              bgcolor: 'rgba(255,255,255,0.06)',
            }}
          />
          <CardContent sx={{ p: 3, textAlign: 'center' }}>
            <ChurchIcon sx={{ fontSize: 44, mb: 1, color: 'rgba(255,255,255,0.9)' }} />
            <Typography variant="h5" sx={{ fontWeight: 800, letterSpacing: '-0.5px' }}>
              {organograma.label}
            </Typography>
            <Typography variant="caption" sx={{ opacity: 0.7, fontWeight: 500, letterSpacing: '0.5px', textTransform: 'uppercase' }}>
              Administração Geral
            </Typography>
          </CardContent>
        </Card>
      </Box>

      {/* Nível 1 & Nível 2 (Clero e Conselho) */}
      <Grid container spacing={4} sx={{ mb: 6 }}>
        {/* Clero Paroquial */}
        {cleroNode && (
          <Grid size={{ xs: 12, md: 6 }}>
            <Card
              sx={{
                borderRadius: 4,
                boxShadow: '0 6px 20px rgba(0,0,0,0.05)',
                border: '1px solid #e2e8f0',
                height: '100%',
              }}
            >
              <Box
                sx={{
                  p: 2.5,
                  bgcolor: '#fff5f5',
                  borderBottom: '1px solid #ffe3e3',
                  display: 'flex',
                  alignItems: 'center',
                  gap: 2,
                  borderTopLeftRadius: 16,
                  borderTopRightRadius: 16,
                }}
              >
                <Avatar sx={{ bgcolor: '#e53935' }}>
                  <ChurchIcon />
                </Avatar>
                <Box>
                  <Typography variant="h6" sx={{ fontWeight: 700, color: '#c62828' }}>
                    {cleroNode.label}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Liderança Espiritual e Eclesiástica
                  </Typography>
                </Box>
              </Box>
              <CardContent sx={{ p: 3 }}>
                <Grid container spacing={2}>
                  {cleroNode.children?.map(m => (
                    <Grid size={{ xs: 12, sm: 6 }} key={m.id}>
                      {renderMemberNode(m)}
                    </Grid>
                  ))}
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        )}

        {/* Conselho Pastoral Paroquial */}
        {conselhoNode && (
          <Grid size={{ xs: 12, md: 6 }}>
            <Card
              sx={{
                borderRadius: 4,
                boxShadow: '0 6px 20px rgba(0,0,0,0.05)',
                border: '1px solid #e2e8f0',
                height: '100%',
              }}
            >
              <Box
                sx={{
                  p: 2.5,
                  bgcolor: '#f0f4c3',
                  borderBottom: '1px solid #e6ee9c',
                  display: 'flex',
                  alignItems: 'center',
                  gap: 2,
                  borderTopLeftRadius: 16,
                  borderTopRightRadius: 16,
                }}
              >
                <Avatar sx={{ bgcolor: '#afb42b' }}>
                  <SupervisorAccountIcon />
                </Avatar>
                <Box>
                  <Typography variant="h6" sx={{ fontWeight: 700, color: '#827717' }}>
                    {conselhoNode.label}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Conselho Administrativo e Pastoral
                  </Typography>
                </Box>
              </Box>
              <CardContent sx={{ p: 3 }}>
                <Grid container spacing={2}>
                  {conselhoNode.children?.map(m => (
                    <Grid size={{ xs: 12, sm: 6 }} key={m.id}>
                      {renderMemberNode(m)}
                    </Grid>
                  ))}
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        )}
      </Grid>

      {/* Nível 3: Pastorais e Movimentos */}
      {pastoraisGrupoNode && (
        <Box>
          <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <PeopleIcon sx={{ color: '#1a237e' }} />
            <Typography variant="h5" sx={{ fontWeight: 800, color: '#1a237e' }}>
              {pastoraisGrupoNode.label}
            </Typography>
          </Box>

          <Grid container spacing={3}>
            {pastoraisGrupoNode.children?.map(renderOrgNode)}
          </Grid>
        </Box>
      )}
    </Box>
  );
};

// Helper case-insensitive string compare
const equalsIgnoreCase = (a: string | null | undefined, b: string | null | undefined): boolean => {
  if (a === null || a === undefined || b === null || b === undefined) {
    return false;
  }
  return a.toLowerCase() === b.toLowerCase();
};

export default Organograma;

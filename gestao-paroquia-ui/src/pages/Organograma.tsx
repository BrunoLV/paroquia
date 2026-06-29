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
  ToggleButton,
  ToggleButtonGroup,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
  AccountTree as AccountTreeIcon,
  People as PeopleIcon,
  Church as ChurchIcon,
  SupervisorAccount as SupervisorAccountIcon,
  GridView as GridViewIcon,
} from '@mui/icons-material';
import { getOrganograma } from '../api/organizacoes';
import { OrganogramaNode } from '../api/types';

const treeStyles = {
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  overflowX: 'auto',
  py: 4,
  width: '100%',
  '& ul': {
    paddingTop: '24px',
    position: 'relative',
    transition: 'all 0.5s',
    display: 'flex',
    justifyContent: 'center',
    gap: '32px',
    listStyleType: 'none',
    paddingLeft: 0,
    margin: 0,
  },
  '& li': {
    textAlign: 'center',
    listStyleType: 'none',
    position: 'relative',
    paddingTop: '24px',
    transition: 'all 0.5s',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
  },
  /* Line connector from child to parent */
  '& li::before, & li::after': {
    content: '""',
    position: 'absolute',
    top: 0,
    right: '50%',
    borderTop: '2px solid #1a237e',
    width: '50%',
    height: '24px',
  },
  '& li::after': {
    right: 'auto',
    left: '50%',
    borderLeft: '2px solid #1a237e',
  },
  /* Remove connectors for single child */
  '& li:only-child::after, & li:only-child::before': {
    display: 'none',
  },
  '& li:only-child': {
    paddingTop: 0,
  },
  /* Remove left/right connector lines from start/end nodes */
  '& li:first-of-type::before, & li:last-of-type::after': {
    border: '0 none',
  },
  /* Border radius for corner connectors */
  '& li:last-of-type::before': {
    borderRight: '2px solid #1a237e',
    borderRadius: '0 8px 0 0',
  },
  '& li:first-of-type::after': {
    borderRadius: '8px 0 0 0',
  },
  /* Downward connector line from parent */
  '& ul ul::before': {
    content: '""',
    position: 'absolute',
    top: 0,
    left: '50%',
    borderLeft: '2px solid #1a237e',
    width: 0,
    height: '24px',
    transform: 'translateX(-50%)',
  },
};

const Organograma: React.FC = () => {
  const { data: organograma, isLoading, isError } = useQuery({
    queryKey: ['organograma'],
    queryFn: getOrganograma,
  });

  const [viewMode, setViewMode] = useState<'tree' | 'grid'>('tree');
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
          mb: 1,
          width: '100%',
          maxWidth: 240,
          mx: 'auto',
          borderLeft: `4px solid ${roleColor}`,
          borderRadius: 1.5,
          boxShadow: '0 1px 4px rgba(0,0,0,0.05)',
          transition: 'all 0.2s ease-in-out',
          '&:hover': {
            transform: 'translateY(-1px)',
            boxShadow: '0 3px 8px rgba(0,0,0,0.1)',
          },
        }}
      >
        <CardContent sx={{ display: 'flex', alignItems: 'center', p: '8px 12px !important', gap: 1 }}>
          <Avatar sx={{ width: 28, height: 28, bgcolor: roleColor, fontSize: '0.75rem', fontWeight: 'bold' }}>
            {initials}
          </Avatar>
          <Box sx={{ minWidth: 0, textAlign: 'left' }}>
            <Typography variant="body2" noWrap sx={{ fontWeight: 600, fontSize: '0.8rem' }}>
              {member.label}
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', textTransform: 'capitalize', fontSize: '0.7rem' }}>
              {member.subtype?.toLowerCase().replace('-', ' ')}
            </Typography>
          </Box>
        </CardContent>
      </Card>
    );
  };

  const renderOrgNode = (org: OrganogramaNode, isInsideTree: boolean = false) => {
    if (!org) return null;
    const isExpanded = expandedMap[org.id] ?? false;
    const members = org.children || [];

    const coord = org.responsavelNome;
    const vice = org.viceResponsavelNome;

    const ordinaryMembers = members.filter(
      m =>
        m &&
        !equalsIgnoreCase(m.label, coord) &&
        !equalsIgnoreCase(m.label, vice)
    );

    const cardContent = (
      <Card
        sx={{
          height: '100%',
          width: isInsideTree ? 260 : '100%',
          display: 'flex',
          flexDirection: 'column',
          borderRadius: 3,
          boxShadow: '0 4px 14px rgba(0,0,0,0.06)',
          border: '1px solid #eef2f6',
          background: 'linear-gradient(to bottom, #ffffff, #fafafa)',
          transition: 'all 0.25s ease-in-out',
          '&:hover': {
            transform: 'translateY(-2px)',
            boxShadow: '0 6px 18px rgba(26, 35, 126, 0.1)',
          },
        }}
      >
        <Box
          sx={{
            p: 1.5,
            bgcolor: '#f5f7fb',
            borderBottom: '1px solid #eef2f6',
            borderTopLeftRadius: 12,
            borderTopRightRadius: 12,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          <Box sx={{ textAlign: 'left' }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#1a237e' }}>
              {org.label}
            </Typography>
            {getSubtypeLabel(org.subtype) !== org.label && (
              <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 500 }}>
                {getSubtypeLabel(org.subtype)}
              </Typography>
            )}
          </Box>
          <ChurchIcon sx={{ color: '#1a237e', opacity: 0.8, fontSize: 20 }} />
        </Box>

        <CardContent sx={{ flexGrow: 1, p: 1.5, display: 'flex', flexDirection: 'column', gap: 1, textAlign: 'left' }}>
          {coord && (
            <Box>
              <Typography variant="caption" sx={{ fontWeight: 600, color: 'text.secondary', display: 'block', fontSize: '0.7rem' }}>
                {org.subtype === 'CLERO' ? 'Pároco' : 'Coordenador(a)'}
              </Typography>
              <Typography variant="body2" sx={{ fontWeight: 600, color: '#333', fontSize: '0.8rem' }}>
                {coord}
              </Typography>
            </Box>
          )}

          {vice && (
            <Box>
              <Typography variant="caption" sx={{ fontWeight: 600, color: 'text.secondary', display: 'block', fontSize: '0.7rem' }}>
                {org.subtype === 'CLERO' ? 'Vigário / Padres' : 'Vice-Coordenador(a)'}
              </Typography>
              <Typography variant="body2" sx={{ fontWeight: 600, color: '#555', fontSize: '0.8rem' }}>
                {vice}
              </Typography>
            </Box>
          )}

          {ordinaryMembers.length > 0 && (
            <Box sx={{ mt: 'auto', pt: 0.5 }}>
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
                  fontSize: '0.75rem',
                  p: 0,
                }}
              >
                {isExpanded ? 'Ocultar Membros' : `Ver Membros (${ordinaryMembers.length})`}
              </Button>

              <Collapse in={isExpanded} timeout="auto" unmountOnExit>
                <Box sx={{ mt: 1, pt: 1, borderTop: '1px dashed #e2e8f0', display: 'flex', flexDirection: 'column' }}>
                  {ordinaryMembers.map(renderMemberNode)}
                </Box>
              </Collapse>
            </Box>
          )}
        </CardContent>
      </Card>
    );

    return isInsideTree ? cardContent : (
      <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={org.id}>
        {cardContent}
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

  const children = organograma?.children || [];
  const cleroNode = children.find(c => c.subtype === 'CLERO');
  const conselhoNode = children.find(c => c.subtype === 'CONSELHO');
  const pastoraisGrupoNode = children.find(c => c.id === 'grupo-pastorais');

  return (
    <Box sx={{ mt: 3, mb: 4, px: { xs: 2, md: 4 } }}>
      {/* Header with Switch */}
      <Box sx={{ mb: 4, display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', gap: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
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

        <ToggleButtonGroup
          value={viewMode}
          exclusive
          onChange={(_, nextValue) => nextValue && setViewMode(nextValue)}
          size="small"
          sx={{ bgcolor: 'white', boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }}
        >
          <ToggleButton value="tree" aria-label="Visualização em Árvore">
            <AccountTreeIcon sx={{ mr: 1, fontSize: 18 }} />
            Árvore
          </ToggleButton>
          <ToggleButton value="grid" aria-label="Visualização em Grade">
            <GridViewIcon sx={{ mr: 1, fontSize: 18 }} />
            Grade
          </ToggleButton>
        </ToggleButtonGroup>
      </Box>

      {viewMode === 'tree' ? (
        /* Connected Interactive Tree View */
        <Box sx={{ overflowX: 'auto', width: '100%', pb: 4, display: 'flex', justifyContent: 'center' }}>
          <Box sx={treeStyles}>
            <ul>
              <li>
                {/* Root Node (A Paróquia) */}
                <Card
                  sx={{
                    minWidth: 260,
                    borderRadius: 4,
                    background: 'linear-gradient(135deg, #1a237e 0%, #311b92 100%)',
                    color: 'white',
                    boxShadow: '0 6px 20px rgba(26, 35, 126, 0.2)',
                    position: 'relative',
                    overflow: 'hidden',
                  }}
                >
                  <Box
                    sx={{
                      position: 'absolute',
                      top: -15,
                      right: -15,
                      width: 70,
                      height: 70,
                      borderRadius: '50%',
                      bgcolor: 'rgba(255,255,255,0.05)',
                    }}
                  />
                  <CardContent sx={{ p: 2, textAlign: 'center' }}>
                    <ChurchIcon sx={{ fontSize: 32, mb: 0.5, color: 'rgba(255,255,255,0.9)' }} />
                    <Typography variant="subtitle1" sx={{ fontWeight: 800 }}>
                      {organograma.label}
                    </Typography>
                    <Typography variant="caption" sx={{ opacity: 0.7, fontWeight: 500, letterSpacing: '0.5px', textTransform: 'uppercase', fontSize: '0.65rem' }}>
                      Administração Geral
                    </Typography>
                  </CardContent>
                </Card>

                {/* Sub-groups */}
                <ul>
                  {/* Clero Node */}
                  {cleroNode && (
                    <li>
                      {renderOrgNode(cleroNode, true)}
                    </li>
                  )}

                  {/* Conselho Node */}
                  {conselhoNode && (
                    <li>
                      {renderOrgNode(conselhoNode, true)}
                    </li>
                  )}

                  {/* Pastorais Group Node */}
                  {pastoraisGrupoNode && (
                    <li>
                      <Card
                        sx={{
                          width: 250,
                          borderRadius: 3,
                          border: '2px dashed #1a237e',
                          bgcolor: 'rgba(26, 35, 126, 0.02)',
                        }}
                      >
                        <CardContent sx={{ p: 2 }}>
                          <PeopleIcon sx={{ color: '#1a237e', fontSize: 28, mb: 0.5 }} />
                          <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#1a237e' }}>
                            {pastoraisGrupoNode.label}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            Pastorais e Movimentos Locais
                          </Typography>
                        </CardContent>
                      </Card>

                      {/* Pastorais Leaf Nodes */}
                      <ul>
                        {pastoraisGrupoNode.children?.map(org => (
                          <li key={org.id}>
                            {renderOrgNode(org, true)}
                          </li>
                        ))}
                      </ul>
                    </li>
                  )}
                </ul>
              </li>
            </ul>
          </Box>
        </Box>
      ) : (
        /* Grid View Layout */
        <>
          {/* Root Card (A Paróquia) */}
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
                {pastoraisGrupoNode.children?.map(org => renderOrgNode(org, false))}
              </Grid>
            </Box>
          )}
        </>
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

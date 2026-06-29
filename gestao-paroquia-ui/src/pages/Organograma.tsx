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
  px: 4,
  width: '100%',
  '& ul': {
    display: 'flex',
    justifyContent: 'center',
    position: 'relative',
    paddingTop: '24px',
    listStyleType: 'none',
    paddingLeft: 0,
    margin: 0,
    gap: 0,
  },
  '& li': {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    position: 'relative',
    padding: '24px 16px 0 16px',
  },
  /* Line connector from child to parent horizontal bar */
  '& li::before': {
    content: '""',
    position: 'absolute',
    top: 0,
    left: '50%',
    borderLeft: '2px solid #1a237e',
    height: '24px',
    width: 0,
    transform: 'translateX(-50%)',
  },
  /* Horizontal bar connecting sibling nodes */
  '& li::after': {
    content: '""',
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    borderTop: '2px solid #1a237e',
  },
  /* Adjust horizontal lines for first and last siblings */
  '& li:first-of-type::after': {
    left: '50%',
  },
  '& li:last-of-type::after': {
    right: '50%',
  },
  /* Remove connectors for single child */
  '& li:only-child::after, & li:only-child::before': {
    display: 'none',
  },
  '& li:only-child': {
    paddingTop: 0,
  },
};

const Organograma: React.FC = () => {
  const { data: organograma, isLoading, isError } = useQuery({
    queryKey: ['organograma'],
    queryFn: getOrganograma,
  });

  const [viewMode, setViewMode] = useState<'tree' | 'grid'>('tree');
  const [expandedMap, setExpandedMap] = useState<Record<string, boolean>>({});

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
      case 'CONSELHO_ECONOMICO':
        return 'Conselho Econômico Paroquial';
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

  const renderMemberNode = (member: OrganogramaNode, orgType?: string) => {
    const isEconomic = orgType === 'CONSELHO_ECONOMICO';
    const roleColor = isEconomic ? '#2e7d32' : getRoleBadgeColor(member.subtype);
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
        m.type === 'MEMBRO' &&
        !equalsIgnoreCase(m.label, coord) &&
        !equalsIgnoreCase(m.label, vice)
    );

    const cardContent = (
      <Card
        sx={{
          height: '100%',
          width: 260,
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
                  {ordinaryMembers.map(m => renderMemberNode(m, org.subtype))}
                </Box>
              </Collapse>
            </Box>
          )}
        </CardContent>
      </Card>
    );

    return isInsideTree ? cardContent : (
      <Grid key={org.id} sx={{ display: 'flex', justifyContent: 'center' }}>
        {cardContent}
      </Grid>
    );
  };

  if (isLoading) {
    return (
      <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Skeleton variant="rectangular" height={100} sx={{ borderRadius: 3 }} />
          <Grid container spacing={3}>
            <Grid size={{ xs: 12, md: 4 }}><Skeleton variant="rectangular" height={300} sx={{ borderRadius: 3 }} /></Grid>
            <Grid size={{ xs: 12, md: 4 }}><Skeleton variant="rectangular" height={300} sx={{ borderRadius: 3 }} /></Grid>
            <Grid size={{ xs: 12, md: 4 }}><Skeleton variant="rectangular" height={300} sx={{ borderRadius: 3 }} /></Grid>
          </Grid>
        </Box>
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
  let cleroNode: OrganogramaNode | undefined = undefined;
  let conselhoNode: OrganogramaNode | undefined = undefined;
  let conselhoEconomicoNode: OrganogramaNode | undefined = undefined;
  let pastorais: OrganogramaNode[] = [];

  if (organograma.subtype === 'CLERO') {
    cleroNode = organograma;
    conselhoNode = children.find(c => c.subtype === 'CONSELHO');
    conselhoEconomicoNode = children.find(c => c.subtype === 'CONSELHO_ECONOMICO');
  } else {
    cleroNode = children.find(c => c.subtype === 'CLERO');
    conselhoNode = children.find(c => c.subtype === 'CONSELHO');
    conselhoEconomicoNode = children.find(c => c.subtype === 'CONSELHO_ECONOMICO');
  }

  if (conselhoNode) {
    pastorais = conselhoNode.children || [];
  }

  return (
    <Box sx={{ mt: 3, mb: 4, px: { xs: 2, md: 4 } }}>
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
        <Box sx={{ overflowX: 'auto', width: '100%', pb: 4, display: 'flex', justifyContent: 'flex-start', minWidth: 'min-content' }}>
          <Box sx={treeStyles}>
            <ul>
              <li>
                <Box sx={{ position: 'relative', pb: '24px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                  <Card
                    sx={{
                      width: 280,
                      borderRadius: 4,
                      background: 'linear-gradient(135deg, #1a237e 0%, #311b92 100%)',
                      color: 'white',
                      boxShadow: '0 6px 20px rgba(26, 35, 126, 0.2)',
                      position: 'relative',
                      overflow: 'hidden',
                    }}
                  >
                    <CardContent sx={{ p: 2.5, textAlign: 'center' }}>
                      <ChurchIcon sx={{ fontSize: 36, mb: 0.5, color: 'rgba(255,255,255,0.9)' }} />
                      <Typography variant="subtitle1" sx={{ fontWeight: 800 }}>
                        {cleroNode?.label || 'Clero Paroquial'}
                      </Typography>
                      <Typography variant="caption" sx={{ opacity: 0.7, fontWeight: 500, letterSpacing: '0.5px', textTransform: 'uppercase', fontSize: '0.65rem', display: 'block', mb: 1 }}>
                        Administração Geral
                      </Typography>
                      {cleroNode?.responsavelNome && (
                        <Box sx={{ mt: 1, pt: 1, borderTop: '1px solid rgba(255,255,255,0.1)' }}>
                          <Typography variant="body2" sx={{ fontWeight: 600, fontSize: '0.8rem' }}>
                            Pároco: {cleroNode.responsavelNome}
                          </Typography>
                          {cleroNode.viceResponsavelNome && (
                            <Typography variant="caption" sx={{ opacity: 0.8, fontSize: '0.7rem', display: 'block' }}>
                              Vigário: {cleroNode.viceResponsavelNome}
                            </Typography>
                          )}
                        </Box>
                      )}
                    </CardContent>
                  </Card>
                  {((conselhoNode) || (conselhoEconomicoNode)) && (
                    <Box
                      sx={{
                        position: 'absolute',
                        bottom: 0,
                        left: '50%',
                        transform: 'translateX(-50%)',
                        borderLeft: '2px solid #1a237e',
                        height: '24px',
                        width: 0,
                      }}
                    />
                  )}
                </Box>

                {((conselhoNode) || (conselhoEconomicoNode)) && (
                  <ul>
                    {conselhoNode && (
                      <li key={conselhoNode.id}>
                        {renderOrgNode(conselhoNode, true)}
                        
                        {pastorais.length > 0 && (
                          <ul>
                            {pastorais.map(pastoral => (
                              <li key={pastoral.id}>
                                {renderOrgNode(pastoral, true)}
                              </li>
                            ))}
                          </ul>
                        )}
                      </li>
                    )}

                    {conselhoEconomicoNode && (
                      <li key={conselhoEconomicoNode.id}>
                        {renderOrgNode(conselhoEconomicoNode, true)}
                      </li>
                    )}
                  </ul>
                )}
              </li>
            </ul>
          </Box>
        </Box>
      ) : (
        <>
          <Grid container spacing={4} sx={{ mb: 6, justifyContent: 'center' }}>
            {cleroNode && (
              <Grid size={{ xs: 12 }} sx={{ display: 'flex', justifyContent: 'center' }}>
                <Card
                  sx={{
                    borderRadius: 4,
                    boxShadow: '0 6px 20px rgba(0,0,0,0.05)',
                    border: '1px solid #e2e8f0',
                    width: '100%',
                    maxWidth: 550,
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
                    <Box sx={{ textAlign: 'left' }}>
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
                        <Grid size={{ xs: 12, sm: 6 }} key={m.id} sx={{ display: 'flex', justifyContent: 'center' }}>
                          {renderMemberNode(m, 'CLERO')}
                        </Grid>
                      ))}
                    </Grid>
                  </CardContent>
                </Card>
              </Grid>
            )}

            {conselhoNode && (
              <Grid size={{ xs: 12, md: 6 }} sx={{ display: 'flex', justifyContent: 'center' }}>
                <Card
                  sx={{
                    borderRadius: 4,
                    boxShadow: '0 6px 20px rgba(0,0,0,0.05)',
                    border: '1px solid #e2e8f0',
                    width: '100%',
                    maxWidth: 550,
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
                    <Box sx={{ textAlign: 'left' }}>
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
                        <Grid size={{ xs: 12, sm: 6 }} key={m.id} sx={{ display: 'flex', justifyContent: 'center' }}>
                          {renderMemberNode(m, 'CONSELHO')}
                        </Grid>
                      ))}
                    </Grid>
                  </CardContent>
                </Card>
              </Grid>
            )}

            {conselhoEconomicoNode && (
              <Grid size={{ xs: 12, md: 6 }} sx={{ display: 'flex', justifyContent: 'center' }}>
                <Card
                  sx={{
                    borderRadius: 4,
                    boxShadow: '0 6px 20px rgba(0,0,0,0.05)',
                    border: '1px solid #e2e8f0',
                    width: '100%',
                    maxWidth: 550,
                  }}
                >
                  <Box
                    sx={{
                      p: 2.5,
                      bgcolor: '#e8f5e9',
                      borderBottom: '1px solid #c8e6c9',
                      display: 'flex',
                      alignItems: 'center',
                      gap: 2,
                      borderTopLeftRadius: 16,
                      borderTopRightRadius: 16,
                    }}
                  >
                    <Avatar sx={{ bgcolor: '#2e7d32' }}>
                      <SupervisorAccountIcon />
                    </Avatar>
                    <Box sx={{ textAlign: 'left' }}>
                      <Typography variant="h6" sx={{ fontWeight: 700, color: '#1b5e20' }}>
                        {conselhoEconomicoNode.label}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        Conselho Econômico e de Assuntos Administrativos
                      </Typography>
                    </Box>
                  </Box>
                  <CardContent sx={{ p: 3 }}>
                    <Grid container spacing={2}>
                      {conselhoEconomicoNode.children?.map(m => (
                        <Grid size={{ xs: 12, sm: 6 }} key={m.id} sx={{ display: 'flex', justifyContent: 'center' }}>
                          {renderMemberNode(m, 'CONSELHO_ECONOMICO')}
                        </Grid>
                      ))}
                    </Grid>
                  </CardContent>
                </Card>
              </Grid>
            )}
          </Grid>

          {pastorais.length > 0 && (
            <Box>
              <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <PeopleIcon sx={{ color: '#1a237e' }} />
                <Typography variant="h5" sx={{ fontWeight: 800, color: '#1a237e' }}>
                  Pastorais e Movimentos
                </Typography>
              </Box>

              <Grid container spacing={3} sx={{ justifyContent: 'center' }}>
                {pastorais.map(org => renderOrgNode(org, false))}
              </Grid>
            </Box>
          )}
        </>
      )}
    </Box>
  );
};

const equalsIgnoreCase = (a: string | null | undefined, b: string | null | undefined): boolean => {
  if (a === null || a === undefined || b === null || b === undefined) {
    return false;
  }
  return a.toLowerCase() === b.toLowerCase();
};

export default Organograma;

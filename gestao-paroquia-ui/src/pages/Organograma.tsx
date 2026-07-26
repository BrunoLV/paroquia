import React, { useState, useRef } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Avatar,
  Collapse,
  Button,
  ButtonGroup,
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
  ZoomIn as ZoomInIcon,
  ZoomOut as ZoomOutIcon,
} from '@mui/icons-material';
import { getOrganograma } from '../api/organizacoes';
import { OrganogramaNode } from '../api/types';
import { useOrganogramaExport, ExportFormat } from '../hooks/useOrganogramaExport';
import { OrganogramaExportButton } from '../components/organograma/OrganogramaExportButton';

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
  const [zoom, setZoom] = useState<number>(1.0);

  const chartRef = useRef<HTMLDivElement>(null);
  const { isExporting, exportOrganograma, generateFilename } = useOrganogramaExport();

  const handleExport = async (format: ExportFormat) => {
    const orgName = organograma?.label || 'paroquia';
    const filename = generateFilename(orgName, format);
    await exportOrganograma(chartRef, format, filename);
  };

  const handleZoomIn = () => setZoom(prev => Math.min(prev + 0.1, 1.5));
  const handleZoomOut = () => setZoom(prev => Math.max(prev - 0.1, 0.5));
  const handleZoomReset = () => setZoom(1.0);

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
          borderRadius: 0,
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

  const getOrgHeaderStyle = (subtype: string) => {
    switch (subtype?.toUpperCase()) {
      case 'CLERO':
        return {
          bgcolor: '#fff5f5',
          borderColor: '#ffe3e3',
          textColor: '#c62828',
          iconColor: '#e53935',
          icon: ChurchIcon,
        };
      case 'CONSELHO':
        return {
          bgcolor: '#f0f4c3',
          borderColor: '#e6ee9c',
          textColor: '#827717',
          iconColor: '#afb42b',
          icon: SupervisorAccountIcon,
        };
      case 'CONSELHO_ECONOMICO':
        return {
          bgcolor: '#e8f5e9',
          borderColor: '#c8e6c9',
          textColor: '#1b5e20',
          iconColor: '#2e7d32',
          icon: SupervisorAccountIcon,
        };
      default:
        return {
          bgcolor: '#f5f7fb',
          borderColor: '#eef2f6',
          textColor: '#1a237e',
          iconColor: '#1a237e',
          icon: ChurchIcon,
        };
    }
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

    const headerStyle = getOrgHeaderStyle(org.subtype);
    const HeaderIcon = headerStyle.icon;

    const cardContent = (
      <Card
        sx={{
          height: '100%',
          width: isInsideTree ? 260 : '100%',
          maxWidth: isInsideTree ? 260 : 550,
          display: 'flex',
          flexDirection: 'column',
          borderRadius: 0,
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
            bgcolor: headerStyle.bgcolor,
            borderBottom: `1px solid ${headerStyle.borderColor}`,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          <Box sx={{ textAlign: 'left' }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 700, color: headerStyle.textColor }}>
              {org.label}
            </Typography>
            {getSubtypeLabel(org.subtype) !== org.label && (
              <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 500 }}>
                {getSubtypeLabel(org.subtype)}
              </Typography>
            )}
          </Box>
          <HeaderIcon sx={{ color: headerStyle.iconColor, opacity: 0.8, fontSize: 20 }} />
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
                  color: headerStyle.textColor,
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

    return cardContent;
  };

  if (isLoading) {
    return (
      <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Skeleton variant="rectangular" height={100} sx={{ borderRadius: 0 }} />
          <Grid container spacing={3}>
            <Grid size={{ xs: 12, md: 4 }}><Skeleton variant="rectangular" height={300} sx={{ borderRadius: 0 }} /></Grid>
            <Grid size={{ xs: 12, md: 4 }}><Skeleton variant="rectangular" height={300} sx={{ borderRadius: 0 }} /></Grid>
            <Grid size={{ xs: 12, md: 4 }}><Skeleton variant="rectangular" height={300} sx={{ borderRadius: 0 }} /></Grid>
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

  const collectNodesBySubtypes = (node: OrganogramaNode, subtypes: string[]): OrganogramaNode[] => {
    if (!node) return [];
    let results: OrganogramaNode[] = [];
    if (subtypes.includes(node.subtype?.toUpperCase())) {
      results.push(node);
    }
    if (node.children) {
      node.children.forEach(child => {
        results = results.concat(collectNodesBySubtypes(child, subtypes));
      });
    }
    return results;
  };

  const cleroOrgs = collectNodesBySubtypes(organograma, ['CLERO']);
  const matrizesOrgs = collectNodesBySubtypes(organograma, ['MATRIZ', 'COMUNIDADE']);
  const conselhoOrgs = collectNodesBySubtypes(organograma, ['CONSELHO', 'CONSELHO_ECONOMICO']);
  const pastoralOrgs = collectNodesBySubtypes(organograma, ['PASTORAL', 'MOVIMENTO', 'LAICATO', 'OUTRO']);

  let cleroRootNode = organograma;
  if (organograma.subtype !== 'CLERO') {
    if (cleroOrgs.length > 0) {
      cleroRootNode = cleroOrgs[0];
    }
  }

  const renderTreeNode = (node: OrganogramaNode, isRoot: boolean = false) => {
    if (!node) return null;
    const childrenOrgs = (node.children || []).filter(c => c && c.type === 'ORGANIZACAO');
    const hasChildren = childrenOrgs.length > 0;

    return (
      <li key={node.id}>
        {isRoot ? (
          <Box sx={{ position: 'relative', pb: hasChildren ? '24px' : 0, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <Card
              sx={{
                width: 260,
                borderRadius: 0,
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
              <CardContent sx={{ p: 2.5, textAlign: 'center' }}>
                <ChurchIcon sx={{ fontSize: 36, mb: 0.5, color: 'rgba(255,255,255,0.9)' }} />
                <Typography variant="subtitle1" sx={{ fontWeight: 800 }}>
                  {node.label || 'Clero Paroquial'}
                </Typography>
                <Typography variant="caption" sx={{ opacity: 0.7, fontWeight: 500, letterSpacing: '0.5px', textTransform: 'uppercase', fontSize: '0.65rem', display: 'block', mb: 1 }}>
                  Administração Geral
                </Typography>
                {node.responsavelNome && (
                  <Box sx={{ mt: 1, pt: 1, borderTop: '1px solid rgba(255,255,255,0.1)' }}>
                    <Typography variant="body2" sx={{ fontWeight: 600, fontSize: '0.8rem' }}>
                      Pároco: {node.responsavelNome}
                    </Typography>
                    {node.viceResponsavelNome && (
                      <Typography variant="caption" sx={{ opacity: 0.8, fontSize: '0.7rem', display: 'block' }}>
                        Vigário: {node.viceResponsavelNome}
                      </Typography>
                    )}
                  </Box>
                )}
              </CardContent>
            </Card>
            {hasChildren && (
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
        ) : (
          hasChildren ? (
            <Box sx={{ position: 'relative', pb: '24px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              {renderOrgNode(node, true)}
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
            </Box>
          ) : (
            renderOrgNode(node, true)
          )
        )}

        {hasChildren && (
          <ul>
            {childrenOrgs.map(child => renderTreeNode(child, false))}
          </ul>
        )}
      </li>
    );
  };

  return (
    <Box sx={{ mt: 3, mb: 4, px: { xs: 2, md: 4 } }}>
      <Box sx={{ mb: 4, display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', gap: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Box sx={{ p: 1.5, bgcolor: 'rgba(26, 35, 126, 0.08)', borderRadius: 0, display: 'flex' }}>
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

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
          <OrganogramaExportButton onExport={handleExport} isExporting={isExporting} />

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
      </Box>

      {/* Container de captura para exportação de imagem */}
      <Box ref={chartRef} sx={{ bgcolor: '#ffffff', p: 1 }}>
        {viewMode === 'tree' ? (
          /* Connected Interactive Tree View */
          <Box>
            {/* Controle de Zoom */}
            <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
              <ButtonGroup
                size="small"
                variant="outlined"
                sx={{
                  bgcolor: 'white',
                  border: '1px solid #e2e8f0',
                  borderRadius: 0,
                  boxShadow: '0 2px 6px rgba(0,0,0,0.04)',
                }}
              >
                <Button
                  onClick={handleZoomOut}
                  disabled={zoom <= 0.5}
                  title="Diminuir Zoom"
                  sx={{
                    color: '#1a237e',
                    borderColor: '#e2e8f0',
                    borderRadius: 0,
                    '&:hover': { bgcolor: '#f8fafc', borderColor: '#cbd5e1' },
                  }}
                >
                  <ZoomOutIcon fontSize="small" />
                </Button>
                <Button
                  onClick={handleZoomReset}
                  title="Resetar Zoom"
                  sx={{
                    color: '#1a237e',
                    borderColor: '#e2e8f0',
                    fontWeight: 700,
                    fontSize: '0.75rem',
                    px: 2,
                    '&:hover': { bgcolor: '#f8fafc', borderColor: '#cbd5e1' },
                  }}
                >
                  {Math.round(zoom * 100)}%
                </Button>
                <Button
                  onClick={handleZoomIn}
                  disabled={zoom >= 1.5}
                  title="Aumentar Zoom"
                  sx={{
                    color: '#1a237e',
                    borderColor: '#e2e8f0',
                    borderRadius: 0,
                    '&:hover': { bgcolor: '#f8fafc', borderColor: '#cbd5e1' },
                  }}
                >
                  <ZoomInIcon fontSize="small" />
                </Button>
              </ButtonGroup>
            </Box>

            <Box sx={{ overflowX: 'auto', width: '100%', pb: 4 }}>
              <Box sx={{ ...treeStyles, width: 'max-content', minWidth: '100%', zoom: zoom }}>
                <ul>
                  {renderTreeNode(cleroRootNode, true)}
                </ul>
              </Box>
            </Box>
          </Box>
        ) : (
          /* Grid View Layout organized by hierarchy groups */
          <>
            {/* Nível 1: Clero */}
            {cleroOrgs.length > 0 && (
              <Box sx={{ mb: 6 }}>
                <Typography variant="h5" sx={{ fontWeight: 800, color: '#1a237e', mb: 3, display: 'flex', alignItems: 'center', gap: 1 }}>
                  <ChurchIcon /> Clero Paroquial
                </Typography>
                <Grid container spacing={3} sx={{ justifyContent: 'center' }}>
                  {cleroOrgs.map(org => (
                    <Grid size={{ xs: 12, sm: 6, md: 4 }} key={org.id} sx={{ display: 'flex', justifyContent: 'center' }}>
                      {renderOrgNode(org, false)}
                    </Grid>
                  ))}
                </Grid>
              </Box>
            )}

            {/* Nível 2: Matrizes e Comunidades */}
            {matrizesOrgs.length > 0 && (
              <Box sx={{ mb: 6 }}>
                <Typography variant="h5" sx={{ fontWeight: 800, color: '#1a237e', mb: 3, display: 'flex', alignItems: 'center', gap: 1 }}>
                  <ChurchIcon /> Matriz e Comunidades
                </Typography>
                <Grid container spacing={3} sx={{ justifyContent: 'center' }}>
                  {matrizesOrgs.map(org => (
                    <Grid size={{ xs: 12, sm: 6, md: 4 }} key={org.id} sx={{ display: 'flex', justifyContent: 'center' }}>
                      {renderOrgNode(org, false)}
                    </Grid>
                  ))}
                </Grid>
              </Box>
            )}

            {/* Nível 3: Conselhos */}
            {conselhoOrgs.length > 0 && (
              <Box sx={{ mb: 6 }}>
                <Typography variant="h5" sx={{ fontWeight: 800, color: '#1a237e', mb: 3, display: 'flex', alignItems: 'center', gap: 1 }}>
                  <SupervisorAccountIcon /> Conselhos Paroquiais
                </Typography>
                <Grid container spacing={3} sx={{ justifyContent: 'center' }}>
                  {conselhoOrgs.map(org => (
                    <Grid size={{ xs: 12, sm: 6, md: 4 }} key={org.id} sx={{ display: 'flex', justifyContent: 'center' }}>
                      {renderOrgNode(org, false)}
                    </Grid>
                  ))}
                </Grid>
              </Box>
            )}

            {/* Nível 4: Pastorais, Movimentos e outros */}
            {pastoralOrgs.length > 0 && (
              <Box>
                <Typography variant="h5" sx={{ fontWeight: 800, color: '#1a237e', mb: 3, display: 'flex', alignItems: 'center', gap: 1 }}>
                  <PeopleIcon /> Pastorais e Movimentos
                </Typography>
                <Grid container spacing={3} sx={{ justifyContent: 'center' }}>
                  {pastoralOrgs.map(org => (
                    <Grid size={{ xs: 12, sm: 6, md: 4 }} key={org.id} sx={{ display: 'flex', justifyContent: 'center' }}>
                      {renderOrgNode(org, false)}
                    </Grid>
                  ))}
                </Grid>
              </Box>
            )}
          </>
        )}
      </Box>
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

import React, {useRef, useState} from 'react';
import {useQuery} from '@tanstack/react-query';
import {
    Alert,
    Avatar,
    Box,
    Button,
    ButtonGroup,
    Card,
    CardContent,
    Collapse,
    Container,
    Grid,
    Skeleton,
    ToggleButton,
    ToggleButtonGroup,
    Typography,
} from '@mui/material';
import {
    AccountTree as AccountTreeIcon,
    Church as ChurchIcon,
    ExpandLess as ExpandLessIcon,
    ExpandMore as ExpandMoreIcon,
    GridView as GridViewIcon,
    People as PeopleIcon,
    SupervisorAccount as SupervisorAccountIcon,
    ZoomIn as ZoomInIcon,
    ZoomOut as ZoomOutIcon,
} from '@mui/icons-material';
import OrganizationChart from '@dabeng/react-orgchart';
import '@dabeng/react-orgchart/dist/ChartContainer.css';
import {getOrganograma} from '../api/organizacoes';
import {OrganogramaNode} from '../api/types';
import {ExportFormat, useOrganogramaExport} from '../hooks/useOrganogramaExport';
import {OrganogramaExportButton} from '../components/organograma/OrganogramaExportButton';
import PageHeader from '../components/PageHeader';

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

  const renderMembroNode = (member: OrganogramaNode, orgType?: string) => {
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
          textColor: 'primary.main',
          iconColor: 'primary.main',
          icon: ChurchIcon,
        };
    }
  };

  const renderOrgNode = (org: OrganogramaNode, isInsideTree: boolean = false) => {
    if (!org) return null;
    const isExpanded = expandedMap[org.id] ?? false;
    const membros = org.orgChildren || org.children || [];

    const coord = org.responsavelNome;
    const vice = org.viceResponsavelNome;

    // O backend já retorna o nome do responsável/vice como campos próprios do nó da organização
    // (destacados no cabeçalho do card), então eles são excluídos da lista de "membros comuns"
    // por nome para não aparecerem duplicados dentro da seção expansível de membros.
    const membrosOrdinarios = membros.filter(
      (m: OrganogramaNode) =>
        m &&
        m.type === 'MEMBRO' &&
        !equalsIgnoreCase(m.label, coord) &&
        !equalsIgnoreCase(m.label, vice)
    );


    const headerStyle = getOrgHeaderStyle(org.subtype);
    const HeaderIcon = headerStyle.icon;


    return (
      <Card
        sx={{
          height: '100%',
          width: isInsideTree ? 260 : '100%',
          maxWidth: isInsideTree ? 260 : 550,
          display: 'flex',
          flexDirection: 'column',
          
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

          {membrosOrdinarios.length > 0 && (
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
                {isExpanded ? 'Ocultar Membros' : `Ver Membros (${membrosOrdinarios.length})`}
              </Button>

              <Collapse in={isExpanded} timeout="auto" unmountOnExit>
                <Box sx={{ mt: 1, pt: 1, borderTop: '1px dashed #e2e8f0', display: 'flex', flexDirection: 'column' }}>
                  {membrosOrdinarios.map((m: OrganogramaNode) => renderMembroNode(m, org.subtype))}

                </Box>
              </Collapse>
            </Box>
          )}
        </CardContent>
      </Card>
    );
  };

  /**
   * A árvore retornada pela API (`OrganogramaNode`) mistura, em `children`, tanto sub-organizações
   * (type === 'ORGANIZACAO', ex.: uma pastoral dentro de uma comunidade) quanto membros
   * individuais (type === 'MEMBRO'). A biblioteca `@dabeng/react-orgchart` só sabe desenhar
   * ramos entre nós do tipo organização, então esta função separa os dois: `orgChildren`
   * preserva a lista original completa (usada por `renderOrgNode` para achar membros/coordenador)
   * enquanto `children` (o campo que o orgchart de fato percorre para desenhar a árvore) é
   * filtrado para conter apenas sub-organizações, recursivamente.
   */
  const transformToOrgchartData = (node: OrganogramaNode): any => {
    if (!node) return null;
    const childrenOrgs = (node.children || []).filter(c => c && c.type === 'ORGANIZACAO');
    return {
      ...node,
      name: node.label,
      title: node.subtype,
      orgChildren: node.children,
      children: childrenOrgs.map(c => transformToOrgchartData(c)),
    };
  };


  const OrgchartNodeTemplate = ({ nodeData }: { nodeData: any }) => {
    return renderOrgNode(nodeData, true);
  };

  if (isLoading) {
    return (
      <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Skeleton variant="rectangular" height={100} />
          <Grid container spacing={3}>
            <Grid size={{ xs: 12, md: 4 }}><Skeleton variant="rectangular" height={300} /></Grid>
            <Grid size={{ xs: 12, md: 4 }}><Skeleton variant="rectangular" height={300} /></Grid>
            <Grid size={{ xs: 12, md: 4 }}><Skeleton variant="rectangular" height={300} /></Grid>
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

  // Achata recursivamente a árvore hierárquica (que reflete a estrutura organizacional real:
  // clero > matriz/comunidades > conselhos > pastorais/movimentos) em listas planas por
  // categoria, usadas para montar tanto a raiz da visão em árvore quanto os grupos da visão em
  // grade — a API entrega uma única árvore aninhada, não listas já separadas por tipo.
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

  // A visão em árvore usa o Clero como raiz visual (pároco no topo da hierarquia pastoral), não
  // o nó raiz literal devolvido pela API (que normalmente é a Paróquia/Matriz). Se a raiz da API
  // já for o Clero, usa-se ela; senão, busca-se o primeiro nó de subtipo CLERO na árvore para
  // servir de ponto de partida do `OrganizationChart`.
  let cleroRootNode = organograma;
  if (organograma.subtype !== 'CLERO') {
    if (cleroOrgs.length > 0) {
      cleroRootNode = cleroOrgs[0];
    }
  }

  return (
    <Box sx={{ mt: 3, mb: 4, px: { xs: 2, md: 4 } }}>
      <PageHeader
        icon={<AccountTreeIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Estrutura Organizacional"
        subtitle="Visualização completa da hierarquia e lideranças da paróquia."
        actions={
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
        }
      />

      {/* Container de captura para exportação de imagem */}
      <Box ref={chartRef} sx={{ bgcolor: '#ffffff', p: 1 }}>
        {viewMode === 'tree' ? (
          /* Connected Interactive Tree View powered by @dabeng/react-orgchart */
          <Box>
            {/* Controle de Zoom */}
            <Box className="no-export" sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
              <ButtonGroup
                size="small"
                variant="outlined"
                sx={{
                  bgcolor: 'white',
                  border: '1px solid #e2e8f0',
                  
                  boxShadow: '0 2px 6px rgba(0,0,0,0.04)',
                }}
              >
                <Button
                  onClick={handleZoomOut}
                  disabled={zoom <= 0.5}
                  title="Diminuir Zoom"
                  sx={{
                    color: 'primary.main',
                    borderColor: '#e2e8f0',
                    
                    '&:hover': { bgcolor: '#f8fafc', borderColor: '#cbd5e1' },
                  }}
                >
                  <ZoomOutIcon fontSize="small" />
                </Button>
                <Button
                  onClick={handleZoomReset}
                  title="Resetar Zoom"
                  sx={{
                    color: 'primary.main',
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
                    color: 'primary.main',
                    borderColor: '#e2e8f0',
                    
                    '&:hover': { bgcolor: '#f8fafc', borderColor: '#cbd5e1' },
                  }}
                >
                  <ZoomInIcon fontSize="small" />
                </Button>
              </ButtonGroup>
            </Box>

            <Box sx={{ overflowX: 'auto', width: '100%', pb: 4 }}>
              <Box sx={{ width: 'max-content', minWidth: '100%', zoom: zoom }}>
                <OrganizationChart
                  datasource={transformToOrgchartData(cleroRootNode)}
                  NodeTemplate={OrgchartNodeTemplate}
                  pan={true}
                  zoom={true}
                  chartClass="paroquia-orgchart"
                />
              </Box>
            </Box>
          </Box>
        ) : (
          /* Grid View Layout organized by hierarchy groups */
          <>
            {/* Nível 1: Clero */}
            {cleroOrgs.length > 0 && (
              <Box sx={{ mb: 6 }}>
                <Typography variant="h5" sx={{ fontWeight: 800, color: 'primary.main', mb: 3, display: 'flex', alignItems: 'center', gap: 1 }}>
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
                <Typography variant="h5" sx={{ fontWeight: 800, color: 'primary.main', mb: 3, display: 'flex', alignItems: 'center', gap: 1 }}>
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
                <Typography variant="h5" sx={{ fontWeight: 800, color: 'primary.main', mb: 3, display: 'flex', alignItems: 'center', gap: 1 }}>
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
                <Typography variant="h5" sx={{ fontWeight: 800, color: 'primary.main', mb: 3, display: 'flex', alignItems: 'center', gap: 1 }}>
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

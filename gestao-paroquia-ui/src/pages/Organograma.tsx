import React, {useRef, useState} from 'react';
import {useQuery} from '@tanstack/react-query';
import {
    Alert,
    alpha,
    Avatar,
    Box,
    Card,
    CardContent,
    Collapse,
    Container,
    Grid,
    Skeleton,
    Stack,
    ToggleButton,
    ToggleButtonGroup,
    Typography,
    useTheme,
} from '@mui/material';
import AccountTreeIcon from '@mui/icons-material/AccountTree';
import ChurchIcon from '@mui/icons-material/Church';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import GridViewIcon from '@mui/icons-material/GridView';
import PeopleIcon from '@mui/icons-material/People';
import SupervisorAccountIcon from '@mui/icons-material/SupervisorAccount';
import ZoomInIcon from '@mui/icons-material/ZoomIn';
import ZoomOutIcon from '@mui/icons-material/ZoomOut';
import OrganizationChart from '@dabeng/react-orgchart';
import '@dabeng/react-orgchart/dist/ChartContainer.css';
import {getOrganograma} from '@/api/organizacoes';
import {OrganogramaNode} from '@/api/types';
import {ExportFormat, useOrganogramaExport} from '@/hooks/useOrganogramaExport';
import {OrganogramaExportButton} from '@/components/organograma/OrganogramaExportButton';
import PageHeader from '@/components/PageHeader';
import ActionButton from '@/components/ActionButton';
import IconActionButton from '@/components/IconActionButton';

const Organograma: React.FC = () => {
  const theme = useTheme();
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
      case 'MATRIZ':
        return 'Matriz Paroquial';
      case 'COMUNIDADE':
        return 'Comunidade Territorial';
      case 'CONSELHO':
        return 'Conselho Pastoral Paroquial';
      case 'CONSELHO_ECONOMICO':
        return 'Conselho Econômico Paroquial';
      case 'PASTORAL':
        return 'Pastoral';
      case 'MOVIMENTO':
        return 'Movimento';
      case 'MINISTERIO':
        return 'Ministério';
      case 'LAICATO':
        return 'Laicato';
      default:
        return subtype || '';
    }
  };

  // Reaproveita as cores semânticas do tema para o papel do membro no organograma
  // (não há cor "roxa" dedicada no tema — Secretário usa o dourado do secondary).
  const getRoleBadgeColor = (role: string) => {
    switch (role?.toUpperCase()) {
      case 'PAROCO':
        return theme.palette.error.main;
      case 'VIGARIO':
      case 'PADRE':
        return theme.palette.warning.main;
      case 'COORDENADOR':
        return theme.palette.info.main;
      case 'VICE_COORDENADOR':
      case 'VICE-COORDENADOR':
        return theme.palette.success.main;
      case 'SECRETARIO':
        return theme.palette.secondary.main;
      default:
        return theme.palette.grey[600];
    }
  };

  const renderMembroNode = (member: OrganogramaNode, orgType?: string) => {
    const isEconomic = orgType === 'CONSELHO_ECONOMICO';
    const roleColor = isEconomic ? theme.palette.success.main : getRoleBadgeColor(member.subtype);
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
          bgcolor: alpha(theme.palette.error.main, 0.06),
          borderColor: alpha(theme.palette.error.main, 0.15),
          textColor: theme.palette.error.dark,
          iconColor: theme.palette.error.main,
          icon: ChurchIcon,
        };
      case 'CONSELHO':
        return {
          bgcolor: alpha(theme.palette.secondary.main, 0.12),
          borderColor: alpha(theme.palette.secondary.main, 0.25),
          textColor: theme.palette.secondary.dark,
          iconColor: theme.palette.secondary.dark,
          icon: SupervisorAccountIcon,
        };
      case 'CONSELHO_ECONOMICO':
        return {
          bgcolor: alpha(theme.palette.success.main, 0.10),
          borderColor: alpha(theme.palette.success.main, 0.22),
          textColor: theme.palette.success.dark,
          iconColor: theme.palette.success.main,
          icon: SupervisorAccountIcon,
        };
      default:
        return {
          bgcolor: theme.custom.subtlePanelBg,
          borderColor: theme.custom.borderSubtle,
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
          minHeight: isInsideTree ? 190 : undefined,
          width: isInsideTree ? 260 : '100%',
          maxWidth: isInsideTree ? 260 : 550,
          display: 'flex',
          flexDirection: 'column',
          border: '1px solid',
          borderColor: 'divider',
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
              <Typography variant="body2" sx={{ fontWeight: 600, color: 'text.primary', fontSize: '0.8rem' }}>
                {coord}
              </Typography>
            </Box>
          )}

          {vice && (
            <Box>
              <Typography variant="caption" sx={{ fontWeight: 600, color: 'text.secondary', display: 'block', fontSize: '0.7rem' }}>
                {org.subtype === 'CLERO' ? 'Vigário / Padres' : 'Vice-Coordenador(a)'}
              </Typography>
              <Typography variant="body2" sx={{ fontWeight: 600, color: 'text.secondary', fontSize: '0.8rem' }}>
                {vice}
              </Typography>
            </Box>
          )}

          {membrosOrdinarios.length > 0 && (
            <Box sx={{ mt: 'auto', pt: 0.5 }}>
              <ActionButton
                title={isExpanded ? 'Ocultar Membros' : 'Ver Membros'}
                icon={isExpanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                size="small"
                variant="text"
                onClick={() => toggleExpand(org.id)}
                sx={{ color: headerStyle.textColor }}
              />

              <Collapse in={isExpanded} timeout="auto" unmountOnExit>
                <Box sx={{ mt: 1, pt: 1, borderTop: '1px dashed', borderColor: 'divider', display: 'flex', flexDirection: 'column' }}>
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
  const transformToOrgchartData = (node: OrganogramaNode): Record<string, unknown> | null => {
    if (!node) return null;
    const childrenOrgs = (node.children || []).filter(c => c && c.type !== 'MEMBRO');
    return {
      ...node,
      name: node.label,
      title: node.subtype,
      orgChildren: node.children,
      children: childrenOrgs.map(c => transformToOrgchartData(c)),
    };
  };


  const OrgchartNodeTemplate = ({ nodeData }: { nodeData: OrganogramaNode }) => {
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
  const pastoralOrgs = collectNodesBySubtypes(organograma, ['PASTORAL', 'MOVIMENTO', 'MINISTERIO', 'LAICATO', 'OUTRO']);

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
            {viewMode === 'tree' && (
              <OrganogramaExportButton onExport={handleExport} isExporting={isExporting} />
            )}

            <ToggleButtonGroup
              value={viewMode}
              exclusive
              onChange={(_, nextValue) => nextValue && setViewMode(nextValue)}
              size="small"
              sx={{ bgcolor: 'background.paper', border: '1px solid', borderColor: 'divider' }}
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

      <Box sx={{ p: 1 }}>
        {viewMode === 'tree' ? (
          /* Connected Interactive Tree View powered by @dabeng/react-orgchart */
          <Box>
            {/* Controle de Zoom */}
            <Box className="no-export" sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
              <Stack
                direction="row"
                spacing={0.5}
                sx={{
                  alignItems: 'center',
                  bgcolor: 'background.paper',
                  border: '1px solid',
                  borderColor: 'divider',
                  borderRadius: 1,
                  p: 0.5,
                }}
              >
                <IconActionButton
                  title="Diminuir Zoom"
                  icon={<ZoomOutIcon fontSize="small" />}
                  onClick={handleZoomOut}
                  disabled={zoom <= 0.5}
                  size="small"
                />
                <ActionButton
                  title="Resetar Zoom"
                  icon={<></>}
                  label={`${Math.round(zoom * 100)}%`}
                  variant="text"
                  onClick={handleZoomReset}
                  sx={{ color: 'primary.main', fontWeight: 700, fontSize: '0.75rem', px: 1, minWidth: 52 }}
                />
                <IconActionButton
                  title="Aumentar Zoom"
                  icon={<ZoomInIcon fontSize="small" />}
                  onClick={handleZoomIn}
                  disabled={zoom >= 1.5}
                  size="small"
                />
              </Stack>
            </Box>

            <Box sx={{ overflowX: 'auto', width: '100%', pb: 4 }}>
              <Box ref={chartRef} sx={{ width: 'max-content', minWidth: '100%', zoom: zoom, bgcolor: 'background.paper', p: 2 }}>
                <OrganizationChart
                  datasource={transformToOrgchartData(cleroRootNode)}
                  NodeTemplate={OrgchartNodeTemplate}
                  pan={true}
                  zoom={false}
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

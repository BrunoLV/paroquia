import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Container,
  Typography,
  Box,
  Tabs,
  Tab,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  Chip,
  CircularProgress,
  TablePagination,
  Alert,
  Drawer,
  IconButton,
  Divider,
} from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import { getApprovals } from '../api/approvals';
import { AprovacaoResponse } from '../api/types';

const Approvals = () => {
  const [tabValue, setTabValue] = useState(0);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [selectedApproval, setSelectedApproval] = useState<AprovacaoResponse | null>(null);

  const statuses: ('PENDENTE' | 'APROVADA' | 'REPROVADA' | 'FALHA_EXECUCAO')[] = [
    'PENDENTE',
    'APROVADA',
    'REPROVADA',
    'FALHA_EXECUCAO',
  ];

  const currentStatus = statuses[tabValue];

  const { data, isLoading, isError } = useQuery({
    queryKey: ['approvals', currentStatus, page, size],
    queryFn: () => getApprovals(currentStatus, page, size),
  });

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
    setPage(0); // Reset page on filter change
  };

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSize(parseInt(event.target.value, 10));
    setPage(0);
  };

  const getTipoLabel = (tipo: string) => {
    switch (tipo) {
      case 'CRIACAO_EVENTO':
        return 'Criação de Evento';
      case 'EDICAO_EVENTO':
      case 'ALTERACAO_HORARIO':
        return 'Edição de Evento';
      case 'CANCELAMENTO':
        return 'Cancelamento de Evento';
      default:
        return tipo;
    }
  };

  const getStatusChip = (status: string) => {
    switch (status) {
      case 'PENDENTE':
        return <Chip label="Pendente" color="warning" size="small" />;
      case 'APROVADA':
        return <Chip label="Aprovada" color="success" size="small" />;
      case 'REPROVADA':
        return <Chip label="Rejeitada" color="error" size="small" />;
      case 'FALHA_EXECUCAO':
        return <Chip label="Falha" color="error" size="small" variant="outlined" />;
      default:
        return <Chip label={status} size="small" />;
    }
  };

  const parsePayload = (jsonStr: string | null) => {
    if (!jsonStr) return null;
    try {
      return JSON.parse(jsonStr);
    } catch {
      return null;
    }
  };

  const getEventTitle = (approval: AprovacaoResponse) => {
    const payload = parsePayload(approval.actionPayloadJson);
    return payload?.titulo || 'Sem título / Cancelamento';
  };

  return (
    <Container maxWidth="xl">
      <Box sx={{ my: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom sx={{ fontWeight: 600, color: '#1a2035' }}>
          Solicitações de Aprovação
        </Typography>
        <Typography variant="body1" color="textSecondary" sx={{ mb: 4 }}>
          Gerencie e analise as solicitações de alteração e novos eventos no calendário paroquial.
        </Typography>

        <Paper sx={{ width: '100%', mb: 4, borderRadius: '12px', overflow: 'hidden', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
          <Tabs
            value={tabValue}
            onChange={handleTabChange}
            indicatorColor="primary"
            textColor="primary"
            sx={{ borderBottom: 1, borderColor: 'divider', bgcolor: '#fff' }}
          >
            <Tab label="Pendentes" />
            <Tab label="Aprovadas" />
            <Tab label="Rejeitadas" />
            <Tab label="Falhas" />
          </Tabs>

          {isLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', my: 8 }}>
              <CircularProgress />
            </Box>
          ) : isError ? (
            <Box sx={{ p: 4 }}>
              <Alert severity="error">Não foi possível carregar as solicitações de aprovação.</Alert>
            </Box>
          ) : !data || data.content.length === 0 ? (
            <Box sx={{ p: 8, textAlign: 'center' }}>
              <Typography variant="body1" color="textSecondary">
                Nenhuma solicitação encontrada para o status selecionado.
              </Typography>
            </Box>
          ) : (
            <>
              <TableContainer>
                <Table>
                  <TableHead sx={{ bgcolor: '#f8f9fa' }}>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 600 }}>Tipo</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Evento / Detalhe</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Solicitante</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Data Criação</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                      <TableCell sx={{ fontWeight: 600 }} align="right">Ações</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {data.content.map((approval) => (
                      <TableRow key={approval.id} hover>
                        <TableCell sx={{ fontWeight: 500 }}>
                          {getTipoLabel(approval.tipoSolicitacao)}
                        </TableCell>
                        <TableCell>
                          {approval.tipoSolicitacao === 'CANCELAMENTO' ? (
                            <Typography variant="body2" color="error" sx={{ fontWeight: 500 }}>
                              Cancelamento de Evento
                            </Typography>
                          ) : (
                            <Typography variant="body2" sx={{ fontWeight: 500 }}>
                              {getEventTitle(approval)}
                            </Typography>
                          )}
                        </TableCell>
                        <TableCell>{approval.solicitanteId}</TableCell>
                        <TableCell>
                          {new Date(approval.criadoEmUtc).toLocaleString()}
                        </TableCell>
                        <TableCell>{getStatusChip(approval.status)}</TableCell>
                        <TableCell align="right">
                          <Button
                            variant="outlined"
                            size="small"
                            onClick={() => setSelectedApproval(approval)}
                            sx={{ textTransform: 'none' }}
                          >
                            Visualizar
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
              <TablePagination
                rowsPerPageOptions={[5, 10, 25]}
                component="div"
                count={data.totalElements}
                rowsPerPage={size}
                page={page}
                onPageChange={handleChangePage}
                onRowsPerPageChange={handleChangeRowsPerPage}
              />
            </>
          )}
        </Paper>
      </Box>

      {/* Details Drawer */}
      <Drawer
        anchor="right"
        open={Boolean(selectedApproval)}
        onClose={() => setSelectedApproval(null)}
        slotProps={{
          paper: {
            sx: { width: { xs: '100%', sm: 450 }, p: 3 }
          }
        }}
      >
        {selectedApproval && (
          <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" sx={{ fontWeight: 600 }}>
                Detalhes da Solicitação
              </Typography>
              <IconButton onClick={() => setSelectedApproval(null)}>
                <CloseIcon />
              </IconButton>
            </Box>
            <Divider sx={{ mb: 3 }} />

            {/* Layout details placeholders for Phase 3 */}
            <Typography variant="subtitle2" color="textSecondary" gutterBottom>
              INFORMAÇÕES GERAIS
            </Typography>
            <Box sx={{ mb: 3 }}>
              <Typography variant="body2"><strong>Solicitante:</strong> {selectedApproval.solicitanteId}</Typography>
              <Typography variant="body2"><strong>Tipo:</strong> {getTipoLabel(selectedApproval.tipoSolicitacao)}</Typography>
              <Typography variant="body2"><strong>Criado em:</strong> {new Date(selectedApproval.criadoEmUtc).toLocaleString()}</Typography>
              <Typography variant="body2"><strong>Status:</strong> {selectedApproval.status}</Typography>
            </Box>
          </Box>
        )}
      </Drawer>
    </Container>
  );
};

export default Approvals;

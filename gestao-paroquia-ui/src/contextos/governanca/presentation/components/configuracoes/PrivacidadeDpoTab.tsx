import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  FormControl,
  Grid,
  MenuItem,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import SaveIcon from '@mui/icons-material/Save';
import AddIcon from '@mui/icons-material/Add';
import CloseIcon from '@mui/icons-material/Close';
import SecurityIcon from '@mui/icons-material/Security';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import DownloadIcon from '@mui/icons-material/Download';
import AssignmentIcon from '@mui/icons-material/Assignment';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ActionButton from '@/shared/presentation/components/ActionButton';
import TableActionMenu from '@/shared/presentation/components/TableActionMenu';
import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import SearchableTablePagination from '@/shared/presentation/components/SearchableTablePagination';
import { useConfiguracaoParoquia } from '@/contextos/governanca/presentation/hooks/useConfiguracaoParoquia';
import {
  AtualizarStatusIncidentePayload,
  GravidadeIncidente,
  IncidenteSeguranca,
  StatusIncidente,
  atualizarIncidenteSeguranca,
  listarIncidentesSeguranca,
  registrarIncidenteSeguranca,
} from '@/contextos/governanca/infrastructure/http/incidentesSeguranca';
import {
  listarSolicitacoesDpo,
  responderSolicitacaoDpo,
  SolicitacaoDpoResponse,
} from '@/contextos/governanca/infrastructure/http/consentimentoPublico';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { formatDateBr } from '@/shared/utils/masks';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { extractErrorMessage } from '@/shared/utils/errorUtils';

const OPERACOES_ROPA = [
  {
    processo: 'Assentos Sacramentais (Batismo, Crisma, Matrimônio, Ordem)',
    finalidade: 'Registro e fé pública eclesiástica dos sacramentos cristãos',
    baseLegal: 'Obrigação legal / canônica (Art. 7º, II e 11, II, "a" LGPD / Cân. 535 CIC)',
    dados: 'Nome civil, filiação, data de nascimento, padrinhos, cônjuge, testemunhas',
    guarda: 'Perpétua (Livros Tombo eclesiásticos)',
    seguranca: 'Criptografia em repouso AES-256, acesso restrito e autenticado',
  },
  {
    processo: 'Pastoral do Dízimo e Doações Comunitárias',
    finalidade: 'Sustentação material das obras de evangelização e caridade',
    baseLegal: 'Vínculo associativo e legítimo interesse pastoral (Art. 7º, V e IX LGPD)',
    dados: 'Nome, CPF, endereço, histórico de contribuições voluntárias',
    guarda: '5 anos (cumprimento de obrigações fiscais/tributárias)',
    seguranca: 'Dados bancários tokenizados, logs sem PII, acesso restrito à tesouraria',
  },
  {
    processo: 'Catequese e Pastoral da Infância / Juventude',
    finalidade: 'Formação cristã e preparação para a iniciação sacramental',
    baseLegal: 'Consentimento específico dos pais / responsáveis (Art. 14, § 1º LGPD)',
    dados: 'Nome da criança/adolescente, data de nascimento, nome e CPF do responsável legal',
    guarda: 'Até a conclusão do ciclo sacramental (Crisma)',
    seguranca: 'Campos do responsável legal validados, termo de consentimento arquivado',
  },
  {
    processo: 'Comunicação Pastoral e Transmissões Litúrgicas',
    finalidade: 'Evangelização, transmissão de celebrações e comunicação comunitária',
    baseLegal: 'Consentimento livre e revogável do titular (Art. 7º, I e 11, I LGPD)',
    dados: 'Imagem, voz, data de aniversário, telefone e e-mail de contato',
    guarda: 'Até a revogação expressa pelo titular no portal',
    seguranca: 'Switches independentes por finalidade, revogação em 1 clique via OTP',
  },
  {
    processo: 'Atendimento e Aconselhamento Pastoral',
    finalidade: 'Acompanhamento espiritual e direção pastoral pelo clero',
    baseLegal: 'Sigilo pastoral eclesiástico e exercício de direitos (Art. 11, II, "d" LGPD / Cân. 983 CIC)',
    dados: 'Informações de foro íntimo partilhadas confidencialmente com o sacerdote',
    guarda: 'Dados não digitalizados / Sigilo sacramental absoluto e inviolável',
    seguranca: 'Proibição absoluta de digitalização no sistema de gestão paroquial',
  },
];

export const PrivacidadeDpoTab: React.FC = () => {
  const { configuracao, atualizarDpo } = useConfiguracaoParoquia();
  const { showSuccess, showError } = useNotification();

  // Estado do DPO
  const [dpoNome, setDpoNome] = useState('');
  const [dpoEmail, setDpoEmail] = useState('');
  const [dpoTelefone, setDpoTelefone] = useState('');
  const [dpoSuccess, setDpoSuccess] = useState(false);

  useEffect(() => {
    if (configuracao) {
      setDpoNome(configuracao.dpoNome || '');
      setDpoEmail(configuracao.dpoEmail || '');
      setDpoTelefone(configuracao.dpoTelefone || '');
    }
  }, [configuracao]);

  const handleSalvarDpo = async (e: React.FormEvent) => {
    e.preventDefault();
    setDpoSuccess(false);
    try {
      await atualizarDpo.mutateAsync({
        dpoNome: dpoNome.trim() || null,
        dpoEmail: dpoEmail.trim().toLowerCase() || null,
        dpoTelefone: dpoTelefone.trim() || null,
      });
      setDpoSuccess(true);
      showSuccess('Canais de contato do DPO atualizados com sucesso.');
    } catch (err) {
      showError(extractErrorMessage(err, 'Falha ao salvar dados do DPO.'));
    }
  };

  // Estado de Incidentes
  const [incidentes, setIncidentes] = useState<IncidenteSeguranca[]>([]);
  const [loadingIncidentes, setLoadingIncidentes] = useState(false);
  const [openModalNovo, setOpenModalNovo] = useState(false);
  const [openModalDetalhes, setOpenModalDetalhes] = useState(false);
  const [incidenteSelecionado, setIncidenteSelecionado] = useState<IncidenteSeguranca | null>(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  // Form Novo Incidente
  const [novoTitulo, setNovoTitulo] = useState('');
  const [novoDescricao, setNovoDescricao] = useState('');
  const [novoTipoDado, setNovoTipoDado] = useState('Dados cadastrais civis');
  const [novoGravidade, setNovoGravidade] = useState<GravidadeIncidente>('MEDIA');
  const [novoDataOcorrencia, setNovoDataOcorrencia] = useState(new Date().toISOString().slice(0, 16));
  const [novoDataDeteccao, setNovoDataDeteccao] = useState(new Date().toISOString().slice(0, 16));
  const [novoMedidas, setNovoMedidas] = useState('');

  // Form Atualizar Incidente
  const [editStatus, setEditStatus] = useState<StatusIncidente>('EM_ANALISE');
  const [editNotificadoAnpd, setEditNotificadoAnpd] = useState(false);
  const [editProtocoloAnpd, setEditProtocoloAnpd] = useState('');
  const [editNotificadoTitulares, setEditNotificadoTitulares] = useState(false);
  const [editMedidas, setEditMedidas] = useState('');

  // Estado de Solicitações DPO (DSAR)
  const [solicitacoes, setSolicitacoes] = useState<SolicitacaoDpoResponse[]>([]);
  const [loadingSolicitacoes, setLoadingSolicitacoes] = useState(false);
  const [solicitacaoSelecionada, setSolicitacaoSelecionada] = useState<SolicitacaoDpoResponse | null>(null);
  const [openModalResposta, setOpenModalResposta] = useState(false);
  const [respostaTexto, setRespostaTexto] = useState('');
  const [statusResposta, setStatusResposta] = useState<'ATENDIDA' | 'REJEITADA' | 'EM_ANALISE'>('ATENDIDA');
  const [pageDsar, setPageDsar] = useState(0);
  const [rowsPerPageDsar, setRowsPerPageDsar] = useState(10);

  const carregarIncidentes = async () => {
    setLoadingIncidentes(true);
    try {
      const dados = await listarIncidentesSeguranca();
      setIncidentes(dados);
    } catch {
      // Ignora erro se sem permissão
    } finally {
      setLoadingIncidentes(false);
    }
  };

  const carregarSolicitacoes = async () => {
    setLoadingSolicitacoes(true);
    try {
      const dados = await listarSolicitacoesDpo();
      setSolicitacoes(dados);
    } catch {
      // Ignora erro se sem permissão
    } finally {
      setLoadingSolicitacoes(false);
    }
  };

  useEffect(() => {
    carregarIncidentes();
    carregarSolicitacoes();
  }, []);

  const handleCriarIncidente = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await registrarIncidenteSeguranca({
        titulo: novoTitulo,
        descricao: novoDescricao,
        tipoDadoAfetado: novoTipoDado,
        categoriaGravidade: novoGravidade,
        dataOcorrencia: new Date(novoDataOcorrencia).toISOString(),
        dataDeteccao: new Date(novoDataDeteccao).toISOString(),
        medidasMitigacao: novoMedidas || undefined,
      });
      setOpenModalNovo(false);
      setNovoTitulo('');
      setNovoDescricao('');
      setNovoMedidas('');
      showSuccess('Incidente registrado com sucesso.');
      await carregarIncidentes();
    } catch (err) {
      showError(extractErrorMessage(err, 'Falha ao registrar incidente.'));
    }
  };

  const handleAbrirDetalhes = (incidente: IncidenteSeguranca) => {
    setIncidenteSelecionado(incidente);
    setEditStatus(incidente.status);
    setEditNotificadoAnpd(incidente.notificadoAnpd);
    setEditProtocoloAnpd(incidente.protocoloAnpd || '');
    setEditNotificadoTitulares(incidente.notificadoTitulares);
    setEditMedidas(incidente.medidasMitigacao || '');
    setOpenModalDetalhes(true);
  };

  const handleSalvarDespacho = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!incidenteSelecionado) return;

    const payload: AtualizarStatusIncidentePayload = {
      status: editStatus,
      notificadoAnpd: editNotificadoAnpd,
      protocoloAnpd: editProtocoloAnpd.trim() || undefined,
      notificadoTitulares: editNotificadoTitulares,
      medidasMitigacao: editMedidas.trim() || undefined,
      dataNotificacaoAnpd: editNotificadoAnpd ? new Date().toISOString() : undefined,
      dataNotificacaoTitulares: editNotificadoTitulares ? new Date().toISOString() : undefined,
    };

    try {
      await atualizarIncidenteSeguranca(incidenteSelecionado.id, payload);
      setOpenModalDetalhes(false);
      showSuccess('Despacho do incidente atualizado com sucesso.');
      await carregarIncidentes();
    } catch (err) {
      showError(extractErrorMessage(err, 'Falha ao salvar despacho do incidente.'));
    }
  };

  const handleAbrirRespostaSolicitacao = (sol: SolicitacaoDpoResponse) => {
    setSolicitacaoSelecionada(sol);
    setRespostaTexto(sol.resposta || '');
    setStatusResposta(sol.status === 'RECEBIDA' ? 'ATENDIDA' : sol.status);
    setOpenModalResposta(true);
  };

  const handleSalvarRespostaSolicitacao = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!solicitacaoSelecionada) return;
    try {
      await responderSolicitacaoDpo(solicitacaoSelecionada.id, {
        resposta: respostaTexto.trim(),
        status: statusResposta,
      });
      setOpenModalResposta(false);
      showSuccess('Resposta ao titular registrada com sucesso.');
      await carregarSolicitacoes();
    } catch (err) {
      showError(extractErrorMessage(err, 'Falha ao responder solicitação do titular.'));
    }
  };

  const handleExportarRopa = () => {
    const jsonStr = JSON.stringify(OPERACOES_ROPA, null, 2);
    const blob = new Blob([jsonStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `ROPA-Paroquia-${new Date().toISOString().slice(0, 10)}.json`;
    a.click();
    URL.revokeObjectURL(url);
    showSuccess('Inventário de Operações (ROPA) exportado com sucesso.');
  };

  const getGravidadeColor = (gravidade: GravidadeIncidente) => {
    switch (gravidade) {
      case 'BAIXA': return 'info';
      case 'MEDIA': return 'warning';
      case 'ALTA':
      case 'CRITICA': return 'error';
      default: return 'default';
    }
  };

  return (
    <Stack spacing={4}>
      {/* 1. SEÇÃO DE CONFIGURAÇÃO DO ENCARREGADO (DPO) */}
      <Card variant="outlined" sx={{ borderRadius: 2 }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
            <SecurityIcon color="primary" />
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              Encarregado pelo Tratamento de Dados Pessoais (DPO - Art. 41 LGPD)
            </Typography>
          </Box>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            A identidade e as informações de contato do Encarregado devem ser divulgadas publicamente, de forma clara
            e objetiva (Art. 41, § 1º). Esses dados são exibidos no rodapé do sistema e nos diálogos públicos de transparência.
          </Typography>

          {dpoSuccess && (
            <Alert severity="success" sx={{ mb: 3 }}>
              Canais de contato do Encarregado de Dados salvos com sucesso!
            </Alert>
          )}

          <form onSubmit={handleSalvarDpo}>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 4 }}>
                <TextField
                  fullWidth
                  size="small"
                  label="Nome do Encarregado / Comitê"
                  value={dpoNome}
                  onChange={(e) => setDpoNome(e.target.value)}
                  placeholder="Ex: Pe. João Silva / Comitê LGPD"
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 4 }}>
                <TextField
                  fullWidth
                  size="small"
                  type="email"
                  label="E-mail de Contato Oficial (DPO)"
                  value={dpoEmail}
                  onChange={(e) => setDpoEmail(e.target.value)}
                  placeholder="privacidade@paroquia.org.br"
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 4 }}>
                <TextField
                  fullWidth
                  size="small"
                  label="Telefone / WhatsApp de Contato"
                  value={dpoTelefone}
                  onChange={(e) => setDpoTelefone(e.target.value)}
                  placeholder="(00) 00000-0000"
                />
              </Grid>
            </Grid>

            <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
              <ActionButton
                type="submit"
                title="Salvar Configurações do DPO"
                icon={<SaveIcon />}
                disabled={atualizarDpo.isPending}
                color="primary"
              />
            </Box>
          </form>
        </CardContent>
      </Card>

      {/* 2. REGISTRO DAS OPERAÇÕES DE TRATAMENTO (ROPA - ART. 37 LGPD) */}
      <Card variant="outlined" sx={{ borderRadius: 2 }}>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <MenuBookIcon color="primary" />
              <Typography variant="h6" sx={{ fontWeight: 700 }}>
                Registro das Operações de Tratamento (ROPA - Art. 37 LGPD)
              </Typography>
            </Box>
            <ActionButton
              title="Exportar ROPA Oficial"
              label="Exportar ROPA (JSON)"
              icon={<DownloadIcon />}
              onClick={handleExportarRopa}
              variant="outlined"
              size="small"
            />
          </Box>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2.5 }}>
            Inventário das operações e bases legais aplicáveis para apresentação facilitada à ANPD ou à Diocese.
          </Typography>

          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead sx={{ backgroundColor: 'action.hover' }}>
                <TableRow>
                  <TableCell sx={{ fontWeight: 600 }}>Processo Canônico / Pastoral</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Base Legal (LGPD / CDC)</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Prazo de Guarda</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Medidas de Segurança</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {OPERACOES_ROPA.map((op, idx) => (
                  <TableRow key={idx} hover>
                    <TableCell sx={{ fontWeight: 600, maxWidth: 220 }}>
                      <Typography variant="subtitle2" sx={{ fontSize: '0.85rem', fontWeight: 600 }}>
                        {op.processo}
                      </Typography>
                      <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                        {op.finalidade}
                      </Typography>
                    </TableCell>
                    <TableCell sx={{ fontSize: '0.8rem' }}>{op.baseLegal}</TableCell>
                    <TableCell sx={{ fontSize: '0.8rem' }}>{op.guarda}</TableCell>
                    <TableCell sx={{ fontSize: '0.8rem' }}>{op.seguranca}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      {/* 3. SOLICITAÇÕES DE TITULARES AO DPO (DSAR - ARTS. 18 E 19) */}
      <Card variant="outlined" sx={{ borderRadius: 2 }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 1 }}>
            <AssignmentIcon color="primary" />
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              Solicitações de Titulares ao DPO (DSAR - Arts. 18 e 19 LGPD)
            </Typography>
          </Box>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2.5 }}>
            Controle de pedidos de confirmação, correção, acesso e esclarecimentos recebidos através dos canais de transparência.
            Prazo legal de resposta: 15 dias.
          </Typography>

          {loadingSolicitacoes ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
              <CircularProgress size={32} />
            </Box>
          ) : solicitacoes.length === 0 ? (
            <Alert severity="info" variant="outlined">
              Nenhuma solicitação de titular pendente no momento.
            </Alert>
          ) : (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead sx={{ backgroundColor: 'action.hover' }}>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 600 }}>Protocolo</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Titular</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Tipo de Solicitação</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Data Abertura</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {solicitacoes.slice(pageDsar * rowsPerPageDsar, pageDsar * rowsPerPageDsar + rowsPerPageDsar).map((sol) => (
                    <TableRow
                      key={sol.id}
                      hover
                      sx={{ cursor: 'pointer' }}
                      onClick={() => handleAbrirRespostaSolicitacao(sol)}
                    >
                      <TableCell sx={{ fontWeight: 600, fontSize: '0.8rem' }}>{sol.protocolo.slice(0, 8)}...</TableCell>
                      <TableCell>
                        <Typography variant="subtitle2" sx={{ fontSize: '0.85rem' }}>{sol.titularNome}</Typography>
                        <Typography variant="caption" color="text.secondary">{sol.titularEmail}</Typography>
                      </TableCell>
                      <TableCell sx={{ fontSize: '0.8rem' }}>{sol.tipoSolicitacao}</TableCell>
                      <TableCell sx={{ fontSize: '0.8rem' }}>{formatDateBr(sol.criadoEmUtc)}</TableCell>
                      <TableCell>
                        <Chip
                          size="small"
                          label={sol.status}
                          color={sol.status === 'ATENDIDA' ? 'success' : sol.status === 'RECEBIDA' ? 'warning' : 'default'}
                        />
                      </TableCell>
                      <TableCell align="right" onClick={(e) => e.stopPropagation()}>
                        <TableActionMenu
                          actions={[
                            {
                              label: 'Responder / Detalhes',
                              icon: <CheckCircleIcon fontSize="small" color="primary" />,
                              onClick: () => handleAbrirRespostaSolicitacao(sol),
                            },
                          ]}
                        />
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
                <SearchableTablePagination
                  colSpan={6}
                  count={solicitacoes.length}
                  page={pageDsar}
                  rowsPerPage={rowsPerPageDsar}
                  onPageChange={(_, p) => setPageDsar(p)}
                  onRowsPerPageChange={(e) => {
                    setRowsPerPageDsar(parseInt(e.target.value, 10));
                    setPageDsar(0);
                  }}
                />
              </Table>
            </TableContainer>
          )}
        </CardContent>
      </Card>

      {/* 4. GESTÃO DE INCIDENTES DE SEGURANÇA (ART. 48) */}
      <Card variant="outlined" sx={{ borderRadius: 2 }}>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <WarningAmberIcon color="warning" />
              <Typography variant="h6" sx={{ fontWeight: 700 }}>
                Comunicação e Gestão de Incidentes de Segurança (Art. 48 LGPD)
              </Typography>
            </Box>
            <ActionButton
              title="Registrar Novo Incidente"
              icon={<AddIcon />}
              onClick={() => setOpenModalNovo(true)}
              color="primary"
            />
          </Box>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            O controlador deve comunicar à autoridade nacional (ANPD) e aos titulares a ocorrência de incidentes de
            segurança que possam acarretar risco ou dano relevante, em prazo razoável (Resolução CD/ANPD nº 15/2024).
          </Typography>

          {loadingIncidentes ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
              <CircularProgress size={32} />
            </Box>
          ) : incidentes.length === 0 ? (
            <Alert severity="success" variant="outlined">
              Nenhum incidente de segurança registrado. A infraestrutura e a governança operam em conformidade.
            </Alert>
          ) : (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead sx={{ backgroundColor: 'action.hover' }}>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 600 }}>Título da Ocorrência</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Gravidade</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Data de Detecção</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Notificado ANPD</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {incidentes.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage).map((inc) => (
                    <TableRow
                      key={inc.id}
                      hover
                      sx={{ cursor: 'pointer' }}
                      onClick={() => handleAbrirDetalhes(inc)}
                    >
                      <TableCell sx={{ fontWeight: 600 }}>{inc.titulo}</TableCell>
                      <TableCell>
                        <Chip
                          size="small"
                          label={inc.categoriaGravidade}
                          color={getGravidadeColor(inc.categoriaGravidade)}
                        />
                      </TableCell>
                      <TableCell>{formatDateBr(inc.dataDeteccao)}</TableCell>
                      <TableCell>
                        {inc.notificadoAnpd ? (
                          <Chip size="small" label="Sim" color="success" />
                        ) : (
                          <Chip size="small" label="Pendente" color="warning" />
                        )}
                      </TableCell>
                      <TableCell>
                        <Chip size="small" label={inc.status.replace('_', ' ')} />
                      </TableCell>
                      <TableCell align="right" onClick={(e) => e.stopPropagation()}>
                        <TableActionMenu
                          actions={[
                            {
                              label: 'Visualizar / Despachar',
                              icon: <SecurityIcon fontSize="small" color="primary" />,
                              onClick: () => handleAbrirDetalhes(inc),
                            },
                          ]}
                        />
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
                <SearchableTablePagination
                  colSpan={6}
                  count={incidentes.length}
                  page={page}
                  rowsPerPage={rowsPerPage}
                  onPageChange={(_, p) => setPage(p)}
                  onRowsPerPageChange={(e) => {
                    setRowsPerPage(parseInt(e.target.value, 10));
                    setPage(0);
                  }}
                />
              </Table>
            </TableContainer>
          )}
        </CardContent>
      </Card>

      {/* MODAL NOVO INCIDENTE */}
      <Dialog open={openModalNovo} onClose={() => setOpenModalNovo(false)} maxWidth="sm" fullWidth>
        <form onSubmit={handleCriarIncidente}>
          <DialogTitle sx={{ fontWeight: 700 }}>Registrar Ocorrência de Incidente de Segurança</DialogTitle>
          <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
            <Stack spacing={2.5}>
              <TextField
                required
                fullWidth
                size="small"
                label="Título do Incidente"
                value={novoTitulo}
                onChange={(e) => setNovoTitulo(e.target.value)}
              />
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <FormControl fullWidth size="small">
                    <SearchableSelect
                      labelId="gravidade-label"
                      label="Gravidade Estimada"
                      value={novoGravidade}
                      onChange={(e) => setNovoGravidade(e.target.value as GravidadeIncidente)}
                    >
                      <MenuItem value="BAIXA">Baixa</MenuItem>
                      <MenuItem value="MEDIA">Média</MenuItem>
                      <MenuItem value="ALTA">Alta</MenuItem>
                      <MenuItem value="CRITICA">Crítica</MenuItem>
                    </SearchableSelect>
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    required
                    fullWidth
                    size="small"
                    label="Tipo de Dado Afetado"
                    value={novoTipoDado}
                    onChange={(e) => setNovoTipoDado(e.target.value)}
                  />
                </Grid>
              </Grid>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    required
                    fullWidth
                    size="small"
                    type="datetime-local"
                    label="Data/Hora da Ocorrência"
                    slotProps={{ inputLabel: { shrink: true } }}
                    value={novoDataOcorrencia}
                    onChange={(e) => setNovoDataOcorrencia(e.target.value)}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    required
                    fullWidth
                    size="small"
                    type="datetime-local"
                    label="Data/Hora da Detecção"
                    slotProps={{ inputLabel: { shrink: true } }}
                    value={novoDataDeteccao}
                    onChange={(e) => setNovoDataDeteccao(e.target.value)}
                  />
                </Grid>
              </Grid>
              <TextField
                required
                fullWidth
                multiline
                rows={3}
                size="small"
                label="Descrição Detalhada do Evento"
                value={novoDescricao}
                onChange={(e) => setNovoDescricao(e.target.value)}
              />
              <TextField
                fullWidth
                multiline
                rows={2}
                size="small"
                label="Medidas de Contenção Imediatas"
                value={novoMedidas}
                onChange={(e) => setNovoMedidas(e.target.value)}
              />
            </Stack>
          </DialogContent>
          <DialogActions sx={{ px: 3, py: 2 }}>
            <ActionButton
              title="Cancelar"
              icon={<CloseIcon />}
              onClick={() => setOpenModalNovo(false)}
              color="secondary"
            />
            <ActionButton
              type="submit"
              title="Registrar Incidente"
              icon={<SaveIcon />}
              color="primary"
            />
          </DialogActions>
        </form>
      </Dialog>

      {/* MODAL DETALHES INCIDENTE */}
      {incidenteSelecionado && (
        <Dialog open={openModalDetalhes} onClose={() => setOpenModalDetalhes(false)} maxWidth="md" fullWidth>
          <form onSubmit={handleSalvarDespacho}>
            <DialogTitle sx={{ fontWeight: 700 }}>Despacho do Incidente: {incidenteSelecionado.titulo}</DialogTitle>
            <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
              <Stack spacing={2.5}>
                <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                  <Typography variant="body2"><strong>Protocolo Interno:</strong> {incidenteSelecionado.id}</Typography>
                  <Chip size="small" label={incidenteSelecionado.categoriaGravidade} color={getGravidadeColor(incidenteSelecionado.categoriaGravidade)} />
                </Box>
                <Typography variant="body2">{incidenteSelecionado.descricao}</Typography>
                <Divider />
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <FormControl fullWidth size="small">
                      <SearchableSelect
                        labelId="status-label"
                        label="Status do Incidente"
                        value={editStatus}
                        onChange={(e) => setEditStatus(e.target.value as StatusIncidente)}
                      >
                        <MenuItem value="EM_ANALISE">Em Análise</MenuItem>
                        <MenuItem value="CONTIDO">Contido</MenuItem>
                        <MenuItem value="NOTIFICADO">Notificado</MenuItem>
                        <MenuItem value="ENCERRADO">Encerrado</MenuItem>
                      </SearchableSelect>
                    </FormControl>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <FormControl fullWidth size="small">
                      <SearchableSelect
                        labelId="anpd-label"
                        label="Notificado ANPD?"
                        value={editNotificadoAnpd ? 'sim' : 'nao'}
                        onChange={(e) => setEditNotificadoAnpd(e.target.value === 'sim')}
                      >
                        <MenuItem value="nao">Não</MenuItem>
                        <MenuItem value="sim">Sim, comunicado à ANPD</MenuItem>
                      </SearchableSelect>
                    </FormControl>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField
                      fullWidth
                      size="small"
                      label="Protocolo ANPD"
                      disabled={!editNotificadoAnpd}
                      value={editProtocoloAnpd}
                      onChange={(e) => setEditProtocoloAnpd(e.target.value)}
                    />
                  </Grid>
                </Grid>
                <TextField
                  fullWidth
                  multiline
                  rows={3}
                  size="small"
                  label="Parecer do DPO e Medidas de Remediação"
                  value={editMedidas}
                  onChange={(e) => setEditMedidas(e.target.value)}
                />
              </Stack>
            </DialogContent>
            <DialogActions sx={{ px: 3, py: 2 }}>
              <ActionButton
                title="Fechar"
                icon={<CloseIcon />}
                onClick={() => setOpenModalDetalhes(false)}
                color="secondary"
              />
              <ActionButton
                type="submit"
                title="Salvar Despacho"
                icon={<SaveIcon />}
                color="primary"
              />
            </DialogActions>
          </form>
        </Dialog>
      )}

      {/* MODAL RESPOSTA DSAR */}
      {solicitacaoSelecionada && (
        <Dialog open={openModalResposta} onClose={() => setOpenModalResposta(false)} maxWidth="sm" fullWidth>
          <form onSubmit={handleSalvarRespostaSolicitacao}>
            <DialogTitle sx={{ fontWeight: 700 }}>Atender Solicitação de Titular</DialogTitle>
            <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
              <Stack spacing={2}>
                <Typography variant="body2"><strong>Protocolo:</strong> {solicitacaoSelecionada.protocolo}</Typography>
                <Typography variant="body2"><strong>Titular:</strong> {solicitacaoSelecionada.titularNome} ({solicitacaoSelecionada.titularEmail})</Typography>
                <Typography variant="body2"><strong>Tipo:</strong> {solicitacaoSelecionada.tipoSolicitacao}</Typography>
                <Card variant="outlined" sx={{ p: 1.5, bgcolor: 'action.hover' }}>
                  <Typography variant="caption" color="text.secondary" sx={{ display: 'block', fontWeight: 600 }}>Mensagem do Titular:</Typography>
                  <Typography variant="body2">{solicitacaoSelecionada.mensagem}</Typography>
                </Card>
                <FormControl fullWidth size="small">
                  <SearchableSelect
                    labelId="status-dsar-label"
                    label="Status do Parecer"
                    value={statusResposta}
                    onChange={(e) => setStatusResposta(e.target.value as 'ATENDIDA' | 'REJEITADA' | 'EM_ANALISE')}
                  >
                    <MenuItem value="EM_ANALISE">Em Análise</MenuItem>
                    <MenuItem value="ATENDIDA">Atendida</MenuItem>
                    <MenuItem value="REJEITADA">Rejeitada (com fundamentação)</MenuItem>
                  </SearchableSelect>
                </FormControl>
                <TextField
                  fullWidth
                  multiline
                  rows={4}
                  size="small"
                  label="Resposta Fundamentada ao Titular"
                  value={respostaTexto}
                  onChange={(e) => setRespostaTexto(e.target.value)}
                  required
                />
              </Stack>
            </DialogContent>
            <DialogActions sx={{ px: 3, py: 2 }}>
              <ActionButton
                title="Fechar"
                icon={<CloseIcon />}
                onClick={() => setOpenModalResposta(false)}
                color="secondary"
              />
              <ActionButton
                type="submit"
                title="Salvar Resposta"
                icon={<SaveIcon />}
                color="primary"
              />
            </DialogActions>
          </form>
        </Dialog>
      )}
    </Stack>
  );
};

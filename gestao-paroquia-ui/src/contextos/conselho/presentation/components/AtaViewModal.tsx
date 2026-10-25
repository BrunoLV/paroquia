import React from 'react';
import {
  Card, CardContent, Dialog, DialogActions, DialogContent, DialogTitle, Grid, Skeleton, Stack, Typography,
} from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';
import CloseIcon from '@mui/icons-material/Close';
import EditIcon from '@mui/icons-material/Edit';
import EditNoteIcon from '@mui/icons-material/EditNote';
import EventIcon from '@mui/icons-material/Event';
import GavelIcon from '@mui/icons-material/Gavel';
import GroupsIcon from '@mui/icons-material/Groups';
import HistoryIcon from '@mui/icons-material/History';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import ReplayIcon from '@mui/icons-material/Replay';
import SendIcon from '@mui/icons-material/Send';
import VerifiedIcon from '@mui/icons-material/Verified';
import type { SvgIconProps } from '@mui/material';
import ActionButton from '@/shared/presentation/components/ActionButton';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { InfoField } from '@/contextos/fiel/presentation/components/fieis/fielConstants';
import { formatDateBr } from '@/shared/utils/masks';
import { getAtaStatusMeta } from '@/shared/utils/statusMeta/ataStatus';
import {
  Ata, CARATER_LABEL, MODALIDADE_LABEL, OpcoesExportacao, RESULTADO_LABEL, SITUACAO_PRESENCA_LABEL,
} from '@/contextos/conselho/domain/ata';
import { useAta, useTrilha } from '@/contextos/conselho/presentation/hooks/useAtas';
import AtaExportMenu from '@/contextos/conselho/presentation/components/AtaExportMenu';

export interface AtaViewAcoes {
  onEditar: (ata: Ata) => void;
  onSubmeter: (ata: Ata) => void;
  onHomologar: (ata: Ata) => void;
  onVetar: (ata: Ata) => void;
  onReabrir: (ata: Ata) => void;
  onRetificar: (ata: Ata) => void;
  onExportar: (ata: Ata, opcoes: OpcoesExportacao) => void;
}

interface AtaViewModalProps extends AtaViewAcoes {
  ataId: string | null;
  onClose: () => void;
}

const formatar = (iso: string | null): string => (iso ? new Date(iso).toLocaleString('pt-BR') : '—');

const Secao: React.FC<{ titulo: string; icone: React.ReactElement<SvgIconProps>; children: React.ReactNode }> = ({ titulo, icone, children }) => (
  <Card variant="outlined" sx={{ borderRadius: 2 }}>
    <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
        {React.cloneElement(icone, { color: 'primary', fontSize: 'small' })}
        <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>{titulo}</Typography>
      </Stack>
      {children}
    </CardContent>
  </Card>
);

const Conteudo: React.FC<{ ata: Ata }> = ({ ata }) => (
  <Stack spacing={2.5}>
    <Secao titulo="Reunião" icone={<EventIcon />}>
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, sm: 4 }}><InfoField label="Situação" icon={<VerifiedIcon fontSize="inherit" />} value={<StatusIcon {...getAtaStatusMeta(ata.status)} />} /></Grid>
        <Grid size={{ xs: 12, sm: 4 }}><InfoField label="Ata" icon={<GavelIcon fontSize="inherit" />} value={ata.numero ? `nº ${ata.numero}` : 'Minuta, sem número'} /></Grid>
        <Grid size={{ xs: 12, sm: 4 }}><InfoField label="Data" icon={<EventIcon fontSize="inherit" />} value={formatDateBr(ata.conteudo.dataReuniao)} /></Grid>
        <Grid size={{ xs: 12, sm: 6 }}><InfoField label="Tipo" icon={<GroupsIcon fontSize="inherit" />} value={ata.tipoRotulo} /></Grid>
        <Grid size={{ xs: 12, sm: 6 }}><InfoField label="Órgão" icon={<GroupsIcon fontSize="inherit" />} value={[ata.organizacaoConvocante, ata.organizacaoAssociada].filter(Boolean).join(' e ') || '—'} /></Grid>
        <Grid size={{ xs: 12, sm: 6 }}><InfoField label="Local" icon={<LocationOnIcon fontSize="inherit" />} value={ata.conteudo.local} /></Grid>
        <Grid size={{ xs: 12, sm: 6 }}><InfoField label="Caráter e modalidade" icon={<EventIcon fontSize="inherit" />} value={`${CARATER_LABEL[ata.conteudo.carater]} · ${MODALIDADE_LABEL[ata.conteudo.modalidade]}`} /></Grid>
        {ata.motivoVeto && (
          <Grid size={{ xs: 12 }}><InfoField label="Motivo do veto" icon={<BlockIcon fontSize="inherit" />} value={ata.motivoVeto} /></Grid>
        )}
        {ata.justificativaRetificacao && (
          <Grid size={{ xs: 12 }}><InfoField label="Retifica outra ata" icon={<EditNoteIcon fontSize="inherit" />} value={ata.justificativaRetificacao} /></Grid>
        )}
      </Grid>
    </Secao>
    <Secao titulo="Presença" icone={<GroupsIcon />}>
      {ata.conteudo.totalParticipantes !== null && <Typography variant="body2">{ata.conteudo.totalParticipantes} participantes.</Typography>}
      <Stack spacing={0.5}>
        {ata.conteudo.presencas.map((p) => (
          <Typography key={p.fielId} variant="body2">
            {p.nome}{p.funcao ? ` (${p.funcao})` : ''} — {SITUACAO_PRESENCA_LABEL[p.situacao]}
          </Typography>
        ))}
        {ata.quorum && (
          <Typography variant="caption" color={ata.quorum.atingido ? 'success.main' : 'warning.main'} sx={{ fontWeight: 600 }}>
            Quórum {ata.quorum.atingido ? 'atingido' : 'não atingido'}: {ata.quorum.presentes} de {ata.quorum.membrosConvocados} membros (mínimo {ata.quorum.minimoNecessario}).
          </Typography>
        )}
      </Stack>
    </Secao>
    {ata.conteudo.itens.map((item) => (
      <Secao key={item.ordem} titulo={`${item.ordem}. ${item.titulo}`} icone={<GavelIcon />}>
        <Stack spacing={1}>
          {item.relato && <Typography variant="body2" sx={{ whiteSpace: 'pre-line' }}>{item.relato}</Typography>}
          {item.deliberacoes.map((d) => (
            <Typography key={d.ordem} variant="body2" sx={{ fontWeight: 500 }}>
              {item.ordem}.{d.ordem} — {d.texto} <em>({RESULTADO_LABEL[d.resultado]}{d.votacao ? `; ${d.votacao.favor} a favor, ${d.votacao.contra} contra, ${d.votacao.abstencoes} abstenções` : ''})</em>
              {d.conflitos.map((c) => (
                <Typography key={c.declaranteFielId} component="span" variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                  Conflito de interesses de {c.declaranteNome}: {c.descricao}
                </Typography>
              ))}
            </Typography>
          ))}
        </Stack>
      </Secao>
    ))}
  </Stack>
);

/**
 * Ficha da ata. As ações exibidas vêm de `ata.acoes`, calculadas pelo servidor conforme o papel do usuário e a
 * situação da ata; a tela não replica a regra de autorização. A trilha só aparece a quem tem `VER_TRILHA`.
 */
export const AtaViewModal: React.FC<AtaViewModalProps> = ({ ataId, onClose, ...acoes }) => {
  const { data: ata, isLoading } = useAta(ataId);
  const podeVerTrilha = ata?.acoes.includes('VER_TRILHA') ?? false;
  const { data: trilha = [] } = useTrilha(ataId, podeVerTrilha);
  const pode = (acao: Ata['acoes'][number]): boolean => ata?.acoes.includes(acao) ?? false;

  return (
    <Dialog open={ataId !== null} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>{ata ? ata.conteudo.titulo : 'Ata de reunião'}</DialogTitle>
      <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
        {isLoading || !ata ? (
          <Skeleton variant="rectangular" height={240} />
        ) : (
          <Stack spacing={2.5}>
            <Conteudo ata={ata} />
            {podeVerTrilha && trilha.length > 0 && (
              <Secao titulo="Trilha de auditoria" icone={<HistoryIcon />}>
                <Stack spacing={0.5}>
                  {trilha.map((t) => (
                    <Typography key={t.id} variant="caption" color="text.secondary">
                      {formatar(t.ocorridoEmUtc)} · {t.acao} · {t.ator} ({t.perfil ?? 'sem perfil'})
                    </Typography>
                  ))}
                </Stack>
              </Secao>
            )}
          </Stack>
        )}
      </DialogContent>
      <DialogActions sx={{ px: 3, py: 2 }}>
        <ActionButton title="Fechar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
        {ata && pode('EXPORTAR') && (
          <AtaExportMenu rotulo="Exportar" onExportar={(opcoes) => acoes.onExportar(ata, opcoes)} />
        )}
        {ata && pode('REDIGIR') && <ActionButton title="Editar rascunho" icon={<EditIcon />} variant="outlined" onClick={() => acoes.onEditar(ata)} />}
        {ata && pode('SUBMETER') && <ActionButton title="Submeter" icon={<SendIcon />} onClick={() => acoes.onSubmeter(ata)} />}
        {ata && pode('REABRIR') && <ActionButton title="Reabrir para correção" icon={<ReplayIcon />} onClick={() => acoes.onReabrir(ata)} />}
        {ata && pode('RETIFICAR') && <ActionButton title="Retificar" icon={<EditNoteIcon />} variant="outlined" onClick={() => acoes.onRetificar(ata)} />}
        {ata && pode('VETAR') && <ActionButton title="Vetar" icon={<BlockIcon />} color="error" variant="outlined" onClick={() => acoes.onVetar(ata)} />}
        {ata && pode('HOMOLOGAR') && <ActionButton title="Homologar" icon={<VerifiedIcon />} color="success" onClick={() => acoes.onHomologar(ata)} />}
      </DialogActions>
    </Dialog>
  );
};

export default AtaViewModal;

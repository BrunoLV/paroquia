import {
  Box,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import CircleIcon from '@mui/icons-material/Circle';
import CloseIcon from '@mui/icons-material/Close';
import EventIcon from '@mui/icons-material/Event';
import ActionButton from '@/shared/presentation/components/ActionButton';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { getCorLiturgicaHex, SolemnityInfo } from '@/shared/utils/liturgicalCalendar';

interface SolenidadesDialogProps {
  open: boolean;
  onClose: () => void;
  currentYear: number;
  liturgicalCycle: string;
  solemnities: SolemnityInfo[];
}

/** Catálogo de solenidades/santoral do ano corrente: datas fixas e móveis (via algoritmo de
 * Meeus/Jones/Butcher), com cor litúrgica e fator de demanda de cada data — ver `useCalendarData`. */
const SolenidadesDialog = ({ open, onClose, currentYear, liturgicalCycle, solemnities }: SolenidadesDialogProps) => {
  // Normaliza o ciclo litúrgico para extrair a designação do ciclo sem repetição redundante de ano
  const cicloLimpo = liturgicalCycle
    ? liturgicalCycle.replace(/\s*\(\d+\)/, '').trim()
    : 'Ano A';
  const cicloFormatado = cicloLimpo.startsWith('Ano') ? cicloLimpo : `Ano ${cicloLimpo}`;
  const tituloModal = `Catálogo Litúrgico & Solenidades - ${cicloFormatado} - ${currentYear}`;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle sx={{ color: 'primary.main', fontWeight: 600 }}>
        {tituloModal}
      </DialogTitle>
    <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Solenidades fixas (Santoral) e datas móveis derivadas via algoritmo de Meeus/Jones/Butcher.
      </Typography>
      <TableContainer component={Paper} variant="outlined">
        <Table size="small">
          <TableHead sx={{ bgcolor: 'action.hover' }}>
            <TableRow>
              <TableCell sx={{ fontWeight: 600 }}>Data</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Solenidade / Marco</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Tipo</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Cor Litúrgica</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {solemnities.map((sol) => (
              <TableRow key={sol.chave} hover>
                <TableCell sx={{ fontWeight: 600 }}>{sol.date.toLocaleDateString('pt-BR')}</TableCell>
                <TableCell>{sol.nome}</TableCell>
                <TableCell>
                  <StatusIcon
                    label={sol.tipo === 'FIXA' ? 'Santoral (Fixa)' : 'Móvel'}
                    color={sol.tipo === 'FIXA' ? 'info' : 'secondary'}
                    icon={<EventIcon />}
                    tooltip={sol.tipo === 'FIXA' ? 'Data fixa no calendário' : 'Data móvel, calculada a partir da Páscoa'}
                  />
                </TableCell>
                <TableCell>
                  <Box
                    component="span"
                    title="Cor litúrgica do dia"
                    sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.5, color: getCorLiturgicaHex(sol.corLiturgica) }}
                  >
                    <CircleIcon fontSize="small" sx={{ color: 'inherit' }} />
                    <Typography component="span" variant="caption" sx={{ fontWeight: 600, color: 'inherit' }}>
                      {sol.corLiturgica}
                    </Typography>
                  </Box>
                </TableCell>
              </TableRow>
            ))}
            {solemnities.length === 0 && (
              <TableRow>
                <TableCell colSpan={4} align="center" sx={{ py: 2, color: 'text.secondary' }}>
                  Nenhuma solenidade encontrada para o ano corrente.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </DialogContent>
    <DialogActions>
      <ActionButton title="Fechar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
    </DialogActions>
  </Dialog>
  );
};

export default SolenidadesDialog;

import { Box, Dialog, DialogActions, DialogContent, DialogTitle, Typography, useTheme } from '@mui/material';
import CircleIcon from '@mui/icons-material/Circle';
import CloseIcon from '@mui/icons-material/Close';
import EventIcon from '@mui/icons-material/Event';
import ActionButton from '@/components/ActionButton';
import StatusIcon from '@/components/StatusIcon';
import { SolemnityInfo } from '@/utils/liturgicalCalendar';

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
  const theme = useTheme();
  return (
  <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
    <DialogTitle sx={{ color: 'primary.main' }}>
      <Typography variant="h6" component="span" sx={{ fontWeight: 600 }}>
        Catálogo Litúrgico & Solenidades — {currentYear} ({liturgicalCycle})
      </Typography>
    </DialogTitle>
    <DialogContent sx={{ pt: 3 }}>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Solenidades fixas (Santoral) e datas móveis derivadas via algoritmo de Meeus/Jones/Butcher.
      </Typography>
      <Box sx={{ overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.875rem' }}>
          <thead>
            <tr style={{ borderBottom: `2px solid ${theme.palette.divider}`, backgroundColor: theme.custom.surfaceMuted }}>
              <th style={{ padding: '8px 12px' }}>Data</th>
              <th style={{ padding: '8px 12px' }}>Solenidade / Marco</th>
              <th style={{ padding: '8px 12px' }}>Tipo</th>
              <th style={{ padding: '8px 12px' }}>Cor Litúrgica</th>
              <th style={{ padding: '8px 12px' }}>Fator Demanda</th>
            </tr>
          </thead>
          <tbody>
            {solemnities.map((sol) => (
              <tr key={sol.chave} style={{ borderBottom: `1px solid ${theme.palette.divider}` }}>
                <td style={{ padding: '8px 12px', fontWeight: 600 }}>
                  {sol.date.toLocaleDateString('pt-BR')}
                </td>
                <td style={{ padding: '8px 12px' }}>{sol.nome}</td>
                <td style={{ padding: '8px 12px' }}>
                  <StatusIcon
                    label={sol.tipo === 'FIXA' ? 'Santoral (Fixa)' : 'Móvel'}
                    color={sol.tipo === 'FIXA' ? 'info' : 'secondary'}
                    icon={<EventIcon />}
                    tooltip={sol.tipo === 'FIXA' ? 'Data fixa no calendário' : 'Data móvel, calculada a partir da Páscoa'}
                  />
                </td>
                <td style={{ padding: '8px 12px' }}>
                  <StatusIcon
                    label={sol.corLiturgica}
                    color={
                      sol.corLiturgica === 'ROXO' ? 'secondary' :
                      sol.corLiturgica === 'VERMELHO' ? 'error' :
                      sol.corLiturgica === 'VERDE' ? 'success' : 'warning'
                    }
                    icon={<CircleIcon />}
                    tooltip="Cor litúrgica do dia"
                  />
                </td>
                <td style={{ padding: '8px 12px', fontWeight: 600 }}>
                  {sol.fatorDemanda.toFixed(2)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Box>
    </DialogContent>
    <DialogActions>
      <ActionButton title="Fechar" icon={<CloseIcon />} onClick={onClose} color="primary" />
    </DialogActions>
  </Dialog>
  );
};

export default SolenidadesDialog;

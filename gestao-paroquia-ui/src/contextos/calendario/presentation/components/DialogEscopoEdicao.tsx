import React from 'react';
import {
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    List,
    ListItem,
    ListItemButton,
    ListItemIcon,
    ListItemText,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import EventIcon from '@mui/icons-material/Event';
import EventRepeatIcon from '@mui/icons-material/EventRepeat';
import UpdateIcon from '@mui/icons-material/Update';
import ActionButton from '@/shared/presentation/components/ActionButton';

export type EscopoEdicao = 'SOMENTE_ESTE' | 'ESTE_E_PROXIMOS' | 'TODOS';

interface DialogEscopoEdicaoProps {
  open: boolean;
  onClose: () => void;
  onSelect: (scope: EscopoEdicao) => void;
}

/**
 * Um evento recorrente não existe como várias cópias independentes no backend: ele é uma única
 * regra de recorrência (`RecorrenciaEntity`/`RegraRecorrencia`) que gera as ocorrências
 * exibidas na agenda. Por isso, ao editar uma ocorrência, é preciso perguntar ao usuário até
 * onde a alteração deve se propagar:
 * - `SOMENTE_ESTE`: destaca essa ocorrência da regra (vira uma exceção pontual), sem afetar as
 *   demais nem a regra original.
 * - `ESTE_E_PROXIMOS`: divide a série a partir desta ocorrência, aplicando a mudança nela e
 *   em todas as futuras, preservando as ocorrências passadas como estavam.
 * - `TODOS`: atualiza a regra de recorrência original por completo, afetando passado e
 *   futuro da série.
 */
const DialogEscopoEdicao: React.FC<DialogEscopoEdicaoProps> = ({ open, onClose, onSelect }) => {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>Editar Evento Recorrente</DialogTitle>
      <DialogContent>
        <DialogContentText sx={{ mb: 2 }}>
          Este é um evento recorrente. Como você deseja aplicar as alterações?
        </DialogContentText>
        <List>
          <ListItem disablePadding>
            <ListItemButton onClick={() => onSelect('SOMENTE_ESTE')}>
              <ListItemIcon>
                <EventIcon color="primary" />
              </ListItemIcon>
              <ListItemText 
                primary="Apenas este evento" 
                secondary="As alterações serão aplicadas apenas a esta ocorrência específica."
              />
            </ListItemButton>
          </ListItem>
          <ListItem disablePadding>
            <ListItemButton onClick={() => onSelect('ESTE_E_PROXIMOS')}>
              <ListItemIcon>
                <UpdateIcon color="primary" />
              </ListItemIcon>
              <ListItemText 
                primary="Este e os próximos" 
                secondary="Esta e todas as ocorrências futuras desta série serão atualizadas."
              />
            </ListItemButton>
          </ListItem>
          <ListItem disablePadding>
            <ListItemButton onClick={() => onSelect('TODOS')}>
              <ListItemIcon>
                <EventRepeatIcon color="primary" />
              </ListItemIcon>
              <ListItemText 
                primary="Todos os eventos da série" 
                secondary="Toda a série original de eventos será atualizada com os novos dados."
              />
            </ListItemButton>
          </ListItem>
        </List>
      </DialogContent>
      <DialogActions>
        <ActionButton title="Cancelar" icon={<CloseIcon />} onClick={onClose} color="secondary" />
      </DialogActions>
    </Dialog>
  );
};

export default DialogEscopoEdicao;

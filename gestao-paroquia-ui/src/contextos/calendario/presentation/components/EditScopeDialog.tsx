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

export type EditScope = 'ONLY_THIS' | 'THIS_AND_FOLLOWING' | 'EDIT_ALL';

interface EditScopeDialogProps {
  open: boolean;
  onClose: () => void;
  onSelect: (scope: EditScope) => void;
}

/**
 * Um evento recorrente não existe como várias cópias independentes no backend: ele é uma única
 * regra de recorrência (`RecorrenciaEntity`/`RegraRecorrencia`) que gera as ocorrências
 * exibidas na agenda. Por isso, ao editar uma ocorrência, é preciso perguntar ao usuário até
 * onde a alteração deve se propagar:
 * - `ONLY_THIS`: destaca essa ocorrência da regra (vira uma exceção pontual), sem afetar as
 *   demais nem a regra original.
 * - `THIS_AND_FOLLOWING`: divide a série a partir desta ocorrência, aplicando a mudança nela e
 *   em todas as futuras, preservando as ocorrências passadas como estavam.
 * - `EDIT_ALL`: atualiza a regra de recorrência original por completo, afetando passado e
 *   futuro da série.
 */
const EditScopeDialog: React.FC<EditScopeDialogProps> = ({ open, onClose, onSelect }) => {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>Editar Evento Recorrente</DialogTitle>
      <DialogContent>
        <DialogContentText sx={{ mb: 2 }}>
          Este é um evento recorrente. Como você deseja aplicar as alterações?
        </DialogContentText>
        <List>
          <ListItem disablePadding>
            <ListItemButton onClick={() => onSelect('ONLY_THIS')}>
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
            <ListItemButton onClick={() => onSelect('THIS_AND_FOLLOWING')}>
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
            <ListItemButton onClick={() => onSelect('EDIT_ALL')}>
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

export default EditScopeDialog;

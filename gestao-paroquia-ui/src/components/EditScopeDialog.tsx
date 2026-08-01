import React from 'react';
import {
    Button,
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
import {Event as EventIcon, EventRepeat as EventRepeatIcon, Update as UpdateIcon,} from '@mui/icons-material';

export type EditScope = 'ONLY_THIS' | 'THIS_AND_FOLLOWING' | 'EDIT_ALL';

interface EditScopeDialogProps {
  open: boolean;
  onClose: () => void;
  onSelect: (scope: EditScope) => void;
}

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
        <Button onClick={onClose} color="secondary" sx={{ textTransform: 'none' }}>
          Cancelar
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default EditScopeDialog;

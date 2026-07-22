import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
} from '@mui/material';
import {
  Event as EventIcon,
  EventRepeat as EventRepeatIcon,
  Update as UpdateIcon,
} from '@mui/icons-material';

export type EditScope = 'ONLY_THIS' | 'THIS_AND_FOLLOWING' | 'EDIT_ALL';

interface EditScopeDialogProps {
  open: boolean;
  onClose: () => void;
  onSelect: (scope: EditScope) => void;
}

const EditScopeDialog: React.FC<EditScopeDialogProps> = ({ open, onClose, onSelect }) => {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth sx={{ '& .MuiDialog-paper': { borderRadius: 0 } }}>
      <DialogTitle sx={{ fontWeight: 700 }}>Editar Evento Recorrente</DialogTitle>
      <DialogContent>
        <DialogContentText sx={{ mb: 2 }}>
          Este é um evento recorrente. Como você deseja aplicar as alterações?
        </DialogContentText>
        <List>
          <ListItem disablePadding>
            <ListItemButton onClick={() => onSelect('ONLY_THIS')} sx={{ borderRadius: 0 }}>
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
            <ListItemButton onClick={() => onSelect('THIS_AND_FOLLOWING')} sx={{ borderRadius: 0 }}>
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
            <ListItemButton onClick={() => onSelect('EDIT_ALL')} sx={{ borderRadius: 0 }}>
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
      <DialogActions sx={{ px: 3, py: 2, borderTop: '1px solid #eee' }}>
        <Button onClick={onClose} color="inherit" sx={{ borderRadius: 0, textTransform: 'none' }}>
          Cancelar
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default EditScopeDialog;

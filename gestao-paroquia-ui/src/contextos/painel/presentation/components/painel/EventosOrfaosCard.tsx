import React from 'react';
import {useNavigate} from 'react-router-dom';
import {Box, Chip, List, ListItemButton, ListItemText, Paper, Typography} from '@mui/material';
import LinkOffIcon from '@mui/icons-material/LinkOff';
import {EventoOrfao} from '@/shared/domain/types/types';

interface EventosOrfaosCardProps {
  eventos?: EventoOrfao[];
}

const formatar = (iso: string): string =>
  new Date(iso).toLocaleDateString('pt-BR', {day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit'});

const EventosOrfaosCard: React.FC<EventosOrfaosCardProps> = ({eventos = []}) => {
  const navigate = useNavigate();

  return (
    <Paper elevation={0} sx={{p: 3, border: '1px solid', borderColor: 'divider', height: '100%'}}>
      <Typography variant="h6" sx={{fontWeight: 700, mb: 1}}>
        Eventos Órfãos
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{mb: 2}}>
        Eventos no período sem projeto vinculado. Sugestão não vincula automaticamente.
      </Typography>
      {eventos.length === 0 ? (
        <Box sx={{display: 'flex', alignItems: 'center', gap: 1, color: 'text.secondary'}}>
          <LinkOffIcon fontSize="small" />
          <Typography variant="body2">Nenhum evento órfão no período.</Typography>
        </Box>
      ) : (
        <List dense disablePadding>
          {eventos.slice(0, 6).map((evento) => (
            <ListItemButton key={evento.eventoId} onClick={() => navigate('/projetos')} sx={{px: 1, borderRadius: 1}}>
              <ListItemText primary={evento.titulo} secondary={formatar(evento.inicio)} />
              {evento.projetoSugeridoNome && (
                <Chip size="small" variant="outlined" color="info" label={`Sugestão: ${evento.projetoSugeridoNome}`} />
              )}
            </ListItemButton>
          ))}
        </List>
      )}
    </Paper>
  );
};

export default EventosOrfaosCard;

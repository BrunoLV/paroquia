import React from 'react';
import {Divider, List, ListItem, ListItemText, Paper, Typography} from '@mui/material';

interface Event {
  id: string;
  titulo: string;
  inicio: string;
  categoria: string;
}

interface UpcomingEventsProps {
  events?: Event[];
  isLoading?: boolean;
}

const UpcomingEvents: React.FC<UpcomingEventsProps> = ({ events, isLoading }) => {
  return (
    <Paper sx={{ p: 3, borderRadius: 0, minHeight: 400, boxShadow: '0 4px 20px rgba(0,0,0,0.05)' }}>
      <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
        Próximos Eventos
      </Typography>
      
      {isLoading ? (
        <Typography color="textSecondary">Carregando eventos...</Typography>
      ) : events && events.length > 0 ? (
        <List>
          {events.map((event, index) => (
            <React.Fragment key={event.id}>
              <ListItem alignItems="flex-start" sx={{ px: 0 }}>
                <ListItemText
                  primary={event.titulo}
                  secondary={
                    <React.Fragment>
                      <Typography component="span" variant="body2" color="text.primary">
                        {new Date(event.inicio).toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' })}
                      </Typography>
                      {` — ${event.categoria}`}
                    </React.Fragment>
                  }
                />
              </ListItem>
              {index < events.length - 1 && <Divider component="li" />}
            </React.Fragment>
          ))}
        </List>
      ) : (
        <Typography color="textSecondary">Não há eventos próximos para mostrar.</Typography>
      )}
    </Paper>
  );
};

export default UpcomingEvents;

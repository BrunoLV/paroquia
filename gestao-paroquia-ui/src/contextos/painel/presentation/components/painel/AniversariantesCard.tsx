import React from 'react';
import {Box, Chip, List, ListItem, ListItemText, Paper, Stack, ToggleButton, ToggleButtonGroup, Typography} from '@mui/material';
import CakeIcon from '@mui/icons-material/Cake';
import {Aniversariante, PeriodoDashboard} from '@/shared/domain/types/types';

interface AniversariantesCardProps {
  aniversariantes?: Aniversariante[];
  janela: PeriodoDashboard;
  onJanelaChange: (janela: PeriodoDashboard) => void;
}

const doisDigitos = (n: number): string => String(n).padStart(2, '0');

const AniversariantesCard: React.FC<AniversariantesCardProps> = ({aniversariantes = [], janela, onJanelaChange}) => (
  <Paper elevation={0} sx={{p: 3, border: '1px solid', borderColor: 'divider', height: '100%'}}>
    <Stack direction="row" sx={{justifyContent: 'space-between', alignItems: 'center', mb: 2}}>
      <Typography variant="h6" sx={{fontWeight: 700}}>Aniversariantes</Typography>
      <ToggleButtonGroup
        size="small"
        exclusive
        value={janela}
        onChange={(_e, value: PeriodoDashboard | null) => value && onJanelaChange(value)}
      >
        <ToggleButton value="SEMANA">Semana</ToggleButton>
        <ToggleButton value="MES">Mês</ToggleButton>
      </ToggleButtonGroup>
    </Stack>
    {aniversariantes.length === 0 ? (
      <Box sx={{display: 'flex', alignItems: 'center', gap: 1, color: 'text.secondary'}}>
        <CakeIcon fontSize="small" />
        <Typography variant="body2">Nenhum aniversariante no período.</Typography>
      </Box>
    ) : (
      <List dense disablePadding>
        {aniversariantes.slice(0, 8).map((pessoa, index) => (
          <ListItem key={index} disableGutters>
            <ListItemText primary={pessoa.primeiroNome} />
            <Chip size="small" variant="outlined" label={`${doisDigitos(pessoa.dia)}/${doisDigitos(pessoa.mes)}`} />
          </ListItem>
        ))}
      </List>
    )}
  </Paper>
);

export default AniversariantesCard;

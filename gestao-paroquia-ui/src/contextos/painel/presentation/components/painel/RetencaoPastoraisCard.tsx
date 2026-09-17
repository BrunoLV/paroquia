import React from 'react';
import {Box, Chip, Paper, Stack, Typography} from '@mui/material';
import GroupsIcon from '@mui/icons-material/Groups';
import {RetencaoPastoral} from '@/shared/domain/types/types';

interface RetencaoPastoraisCardProps {
  pastorais?: RetencaoPastoral[];
}

const RetencaoPastoraisCard: React.FC<RetencaoPastoraisCardProps> = ({pastorais = []}) => (
  <Paper elevation={0} sx={{p: 3, border: '1px solid', borderColor: 'divider', height: '100%'}}>
    <Typography variant="h6" sx={{fontWeight: 700, mb: 1}}>Retenção de Pastorais</Typography>
    <Typography variant="body2" color="text.secondary" sx={{mb: 2}}>
      Pastorais com poucos voluntários ou sem liderança designada
    </Typography>
    {pastorais.length === 0 ? (
      <Box sx={{display: 'flex', alignItems: 'center', gap: 1, color: 'text.secondary'}}>
        <GroupsIcon fontSize="small" />
        <Typography variant="body2">Nenhuma pastoral em situação de atenção.</Typography>
      </Box>
    ) : (
      <Stack spacing={1.5}>
        {pastorais.slice(0, 6).map((pastoral) => (
          <Box key={pastoral.grupoId}>
            <Stack direction="row" sx={{justifyContent: 'space-between'}}>
              <Typography variant="body2" sx={{fontWeight: 600}}>{pastoral.nome}</Typography>
              <Typography variant="caption" color="text.secondary">
                {pastoral.totalVoluntarios} voluntário(s)
              </Typography>
            </Stack>
            <Stack direction="row" spacing={0.5} sx={{flexWrap: 'wrap', gap: 0.5, mt: 0.5}}>
              {pastoral.motivos.map((motivo) => (
                <Chip key={motivo} size="small" color="warning" variant="outlined" label={motivo} />
              ))}
            </Stack>
          </Box>
        ))}
      </Stack>
    )}
  </Paper>
);

export default RetencaoPastoraisCard;

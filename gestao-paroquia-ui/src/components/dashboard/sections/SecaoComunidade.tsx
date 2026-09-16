import React from 'react';
import {Grid, Typography} from '@mui/material';
import AniversariantesCard from '@/components/dashboard/AniversariantesCard';
import RetencaoPastoraisCard from '@/components/dashboard/RetencaoPastoraisCard';
import {Aniversariante, DashboardComunidade, PeriodoDashboard} from '@/api/types';

interface SecaoComunidadeProps {
  aniversariantes?: Aniversariante[];
  janelaAniversario: PeriodoDashboard;
  onJanelaChange: (janela: PeriodoDashboard) => void;
  comunidade?: DashboardComunidade;
}

const SecaoComunidade: React.FC<SecaoComunidadeProps> = ({
  aniversariantes,
  janelaAniversario,
  onJanelaChange,
  comunidade,
}) => (
  <>
    <Grid size={{xs: 12}}>
      <Typography variant="h6" sx={{mb: 2, fontWeight: 700}}>Vida Comunitária</Typography>
    </Grid>
    <Grid size={{xs: 12, md: 6}}>
      <AniversariantesCard
        aniversariantes={aniversariantes}
        janela={janelaAniversario}
        onJanelaChange={onJanelaChange}
      />
    </Grid>
    <Grid size={{xs: 12, md: 6}}>
      <RetencaoPastoraisCard pastorais={comunidade?.retencaoPastorais} />
    </Grid>
  </>
);

export default SecaoComunidade;

import React from 'react';
import {render, screen} from '@testing-library/react';
import {ThemeProvider} from '@mui/material/styles';
import theme from '@/theme';
import AniversariantesCard from './AniversariantesCard';
import RetencaoPastoraisCard from './RetencaoPastoraisCard';

const wrap = (node: React.ReactElement) => render(<ThemeProvider theme={theme}>{node}</ThemeProvider>);

describe('AniversariantesCard', () => {
  it('mostra primeiro nome e dia/mês, sem ano', () => {
    wrap(
      <AniversariantesCard
        aniversariantes={[{primeiroNome: 'Maria', dia: 5, mes: 9}]}
        janela="SEMANA"
        onJanelaChange={() => {}}
      />,
    );
    expect(screen.getByText('Maria')).toBeInTheDocument();
    expect(screen.getByText('05/09')).toBeInTheDocument();
  });

  it('mostra estado vazio', () => {
    wrap(<AniversariantesCard aniversariantes={[]} janela="MES" onJanelaChange={() => {}} />);
    expect(screen.getByText(/Nenhum aniversariante/i)).toBeInTheDocument();
  });
});

describe('RetencaoPastoraisCard', () => {
  it('lista pastorais em alerta com os motivos', () => {
    wrap(
      <RetencaoPastoraisCard
        pastorais={[
          {grupoId: 'g1', nome: 'Pastoral do Dízimo', totalVoluntarios: 2, temLideranca: false,
            motivos: ['Menos de 3 voluntários ativos', 'Sem liderança designada']},
        ]}
      />,
    );
    expect(screen.getByText('Pastoral do Dízimo')).toBeInTheDocument();
    expect(screen.getByText('Sem liderança designada')).toBeInTheDocument();
  });

  it('mostra estado saudável quando não há alertas', () => {
    wrap(<RetencaoPastoraisCard pastorais={[]} />);
    expect(screen.getByText(/Nenhuma pastoral em situação de atenção/i)).toBeInTheDocument();
  });
});

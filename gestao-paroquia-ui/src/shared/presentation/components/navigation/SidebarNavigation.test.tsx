import { render, screen, within, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, useLocation } from 'react-router-dom';
import { Typography } from '@mui/material';
import { describe, expect, it, vi } from 'vitest';
import SidebarNavigation from './SidebarNavigation';

const user = { id: 'teste', username: 'teste', name: 'Pessoa de teste', roles: ['ROLE_CLERO_PAROCO'], organizations: [] };
const groups = [
  ['Visão geral', ['Painel', 'Notificações', 'Relatórios']],
  ['Pessoas e comunidades', ['Fiéis', 'Clero', 'Pastorais e Movimentos']],
  ['Vida pastoral', ['Calendário', 'Projetos', 'Sacramentos']],
  ['Governo paroquial', ['Governo & Território', 'Organograma', 'Conselhos & Atas']],
  ['Espaços e recursos', ['Locais', 'Patrimônio', 'Reservas']],
  ['Administração', ['Aprovações', 'Ajustes cadastrais', 'Configurações']],
] as const;

function Preview({ expanded = true, onNavigate = vi.fn() }) {
  const { pathname } = useLocation();
  return <><SidebarNavigation user={user} pathname={pathname} expanded={expanded} onNavigate={onNavigate} />
    <Typography data-testid="route">{pathname}</Typography></>;
}

/** A navegação agrupa por finalidade sem alterar a matriz de autorização das rotas. */
describe('menu lateral por finalidade', () => {
  it('mantém todos os itens, uma vez cada, na ordem funcional aprovada', () => {
    render(<MemoryRouter><Preview /></MemoryRouter>);
    expect(screen.getAllByRole('group').map(group => group.getAttribute('aria-label'))).toEqual(groups.map(([title]) => title));
    for (const [title, labels] of groups) {
      const group = screen.getByRole('group', { name: title });
      expect(within(group).getAllByRole('link').map(link => link.textContent)).toEqual(labels);
    }
    expect(screen.getAllByRole('link')).toHaveLength(18);
  });

  it('recolhe a seção ativa por teclado e reabre ao navegar para outra rota nela', async () => {
    const keyboard = userEvent.setup();
    render(<MemoryRouter initialEntries={['/painel']}><Preview /></MemoryRouter>);
    const heading = screen.getByRole('button', { name: 'Visão geral' });
    await keyboard.tab();
    await keyboard.keyboard('{Enter}');
    expect(heading).toHaveAttribute('aria-expanded', 'false');
    await waitFor(() => expect(screen.queryByRole('link', { name: 'Painel' })).not.toBeInTheDocument());
    await keyboard.keyboard(' ');
    expect(heading).toHaveAttribute('aria-expanded', 'true');
    await keyboard.click(screen.getByRole('link', { name: 'Notificações' }));
    expect(screen.getByTestId('route')).toHaveTextContent('/notificacoes');
    expect(screen.getByRole('link', { name: 'Notificações' })).toHaveAttribute('aria-current', 'page');
  });

  it('preserva navegação e nomes acessíveis no modo compacto', async () => {
    const onNavigate = vi.fn();
    render(<MemoryRouter><Preview expanded={false} onNavigate={onNavigate} /></MemoryRouter>);
    const link = screen.getByRole('link', { name: 'Reservas' });
    expect(link.textContent).toBe('');
    await userEvent.setup().click(link);
    expect(screen.getByTestId('route')).toHaveTextContent('/reservas');
    expect(onNavigate).toHaveBeenCalledOnce();
  });

  it('omite itens restritos e seções vazias, sem reduzir acesso aos itens públicos existentes', () => {
    render(<MemoryRouter><SidebarNavigation user={{ ...user, roles: ['ROLE_PASTORAL_MEMBRO'] }} pathname="/painel" expanded onNavigate={vi.fn()} /></MemoryRouter>);
    expect(screen.queryByRole('group', { name: 'Administração' })).not.toBeInTheDocument();
    for (const label of ['Aprovações', 'Ajustes cadastrais', 'Configurações', 'Governo & Território', 'Locais', 'Pastorais e Movimentos']) {
      expect(screen.queryByRole('link', { name: label })).not.toBeInTheDocument();
    }
    expect(screen.getByRole('link', { name: 'Fiéis' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Organograma' })).toBeInTheDocument();
  });

  it('reabre o grupo de uma nova rota sem reabrir outros grupos recolhidos', async () => {
    const keyboard = userEvent.setup();
    const preview = (pathname: string) => <MemoryRouter><SidebarNavigation user={user} pathname={pathname} expanded onNavigate={vi.fn()} /></MemoryRouter>;
    const { rerender } = render(preview('/painel'));
    await keyboard.click(screen.getByRole('button', { name: 'Vida pastoral' }));
    await keyboard.click(screen.getByRole('button', { name: 'Pessoas e comunidades' }));
    rerender(preview('/calendario'));
    expect(screen.getByRole('button', { name: 'Vida pastoral' })).toHaveAttribute('aria-expanded', 'true');
    expect(screen.getByRole('button', { name: 'Pessoas e comunidades' })).toHaveAttribute('aria-expanded', 'false');
    const heading = screen.getByRole('button', { name: 'Vida pastoral' });
    expect(document.getElementById(heading.getAttribute('aria-controls')!)).toContainElement(screen.getByRole('link', { name: 'Calendário' }));
  });
});

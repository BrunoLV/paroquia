import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Table, TableBody, TableCell, TableRow } from '@mui/material';
import VisibilityIcon from '@mui/icons-material/Visibility';
import TableActionMenu from './TableActionMenu';

/** O menu compacto preserva ações e isola a interação do comportamento da linha. */
describe('menu de ações de tabela', () => {
  it('exibe somente o ícone vertical, com tooltip e estado acessível do menu', async () => {
    const user = userEvent.setup();
    render(<TableActionMenu actions={[{ label: 'Visualizar', icon: <VisibilityIcon />, onClick: vi.fn() }]} />);
    const trigger = screen.getByRole('button', { name: 'Ações' });
    expect(trigger.textContent).toBe('');
    expect(trigger).not.toHaveTextContent('Ações');
    expect(screen.getByTestId('MoreVertIcon')).toBeInTheDocument();
    expect(trigger).toHaveAttribute('aria-haspopup', 'menu');
    expect(trigger).toHaveAttribute('aria-expanded', 'false');
    await user.hover(trigger);
    expect(await screen.findByRole('tooltip')).toHaveTextContent('Ações');
    await user.click(trigger);
    expect(trigger).toHaveAttribute('aria-expanded', 'true');
    expect(trigger).toHaveAttribute('aria-controls', screen.getByRole('menu').id);
  });

  it.each(['mouse', 'teclado'])('abre e executa ação por %s sem acionar a linha', async (mode) => {
    const user = userEvent.setup();
    const rowClick = vi.fn();
    const actionClick = vi.fn();
    render(<Table><TableBody><TableRow onClick={rowClick} onKeyDown={(event) => {
      if (event.key === 'Enter' || event.key === ' ') rowClick();
    }}><TableCell>
      <TableActionMenu actions={[{ label: 'Visualizar', icon: <VisibilityIcon />, onClick: actionClick }]} />
    </TableCell></TableRow></TableBody></Table>);
    const trigger = screen.getByRole('button', { name: 'Ações' });
    if (mode === 'mouse') await user.click(trigger);
    else { await user.tab(); await user.keyboard('{Enter}'); }
    const action = await screen.findByRole('menuitem', { name: 'Visualizar' });
    if (mode === 'mouse') await user.click(action);
    else await user.keyboard('{Enter}');
    expect(actionClick).toHaveBeenCalledOnce();
    expect(rowClick).not.toHaveBeenCalled();
    await waitFor(() => expect(screen.queryByRole('menu')).not.toBeInTheDocument());
    expect(trigger).toHaveFocus();
  });

  it('mantém ações indisponíveis desabilitadas', async () => {
    const user = userEvent.setup();
    const action = vi.fn();
    render(<TableActionMenu actions={[{ label: 'Editar', icon: <VisibilityIcon />, onClick: action, disabled: true }]} />);
    await user.click(screen.getByRole('button', { name: 'Ações' }));
    const item = screen.getByRole('menuitem', { name: 'Editar' });
    expect(item).toHaveAttribute('aria-disabled', 'true');
    await user.keyboard('{Enter}');
    expect(action).not.toHaveBeenCalled();
    await user.keyboard('{Escape}');
    await waitFor(() => expect(screen.queryByRole('menu')).not.toBeInTheDocument());
  });
});

import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Table } from '@mui/material';
import SearchableTablePagination from './SearchableTablePagination';

/** A quantidade de linhas é uma lista fechada pesquisável e reinicia a navegação. */
it('pesquisa a quantidade e retorna à primeira página', async () => {
  const user = userEvent.setup();
  const changeRows = vi.fn();
  const changePage = vi.fn();
  render(<Table><SearchableTablePagination colSpan={4} count={100} page={2} rowsPerPage={10}
    rowsPerPageOptions={[5, 10, 25]} onPageChange={changePage} onRowsPerPageChange={changeRows} /></Table>);
  const input = screen.getByRole('combobox', { name: 'Linhas por página:' });
  await user.clear(input);
  await user.type(input, '25');
  await user.keyboard('{ArrowDown}{Enter}');
  expect(changeRows).toHaveBeenCalledWith({ target: { value: '25' } });
  expect(changePage).toHaveBeenCalledWith(null, 0);
});

it('mantém todos os controles no rodapé semântico e cobre as colunas da tabela', () => {
  render(<Table><SearchableTablePagination colSpan={7} count={42} page={0} rowsPerPage={10}
    onPageChange={vi.fn()} /></Table>);
  const input = screen.getByRole('combobox', { name: 'Linhas por página:' });
  expect(input.closest('tfoot')?.parentElement?.tagName).toBe('TABLE');
  expect(input.closest('td')).toHaveAttribute('colspan', '7');
  expect(screen.getByText('1–10 de 42').closest('tfoot')).toBe(input.closest('tfoot'));
});

it('preserva a navegação de páginas e desabilita a anterior na primeira página', async () => {
  const user = userEvent.setup();
  const changePage = vi.fn();
  render(<Table><SearchableTablePagination colSpan={4} count={42} page={0} rowsPerPage={10}
    onPageChange={changePage} /></Table>);
  expect(screen.getByRole('button', { name: 'Página anterior' })).toBeDisabled();
  await user.click(screen.getByRole('button', { name: 'Próxima página' }));
  expect(changePage).toHaveBeenCalledWith(expect.anything(), 1);
});

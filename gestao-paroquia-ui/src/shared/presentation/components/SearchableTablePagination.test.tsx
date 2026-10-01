import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import SearchableTablePagination from './SearchableTablePagination';

/** A quantidade de linhas é uma lista fechada pesquisável e reinicia a navegação. */
it('pesquisa a quantidade e retorna à primeira página', async () => {
  const user = userEvent.setup();
  const changeRows = vi.fn();
  const changePage = vi.fn();
  render(<SearchableTablePagination count={100} page={2} rowsPerPage={10}
    rowsPerPageOptions={[5, 10, 25]} onPageChange={changePage} onRowsPerPageChange={changeRows} />);
  const input = screen.getByRole('combobox', { name: 'Linhas por página:' });
  await user.clear(input);
  await user.type(input, '25');
  await user.keyboard('{ArrowDown}{Enter}');
  expect(changeRows).toHaveBeenCalledWith({ target: { value: '25' } });
  expect(changePage).toHaveBeenCalledWith(null, 0);
});

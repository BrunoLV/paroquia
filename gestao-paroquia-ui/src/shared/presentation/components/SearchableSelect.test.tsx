import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MenuItem } from '@mui/material';
import { useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import StatusIcon from './StatusIcon';
import ChurchIcon from '@mui/icons-material/Church';
import SearchableSelect from './SearchableSelect';

/** Especifica pesquisa sem alterar os códigos persistidos pelos formulários. */
function Example({ multiple = false }: { multiple?: boolean }) {
  const [value, setValue] = useState<string | string[]>(multiple ? [] : '');
  return <>
    <SearchableSelect label="Organização" multiple={multiple} value={value} onChange={(event) => setValue(event.target.value)}>
      <MenuItem value="acao">Ação pastoral</MenuItem>
      <MenuItem value="coro">Coro</MenuItem>
      <MenuItem value="bloqueada" disabled>Restrita</MenuItem>
    </SearchableSelect>
    <output aria-label="Valor persistido">{JSON.stringify(value)}</output>
  </>;
}

describe('listas pesquisáveis do Design System', () => {
  it('pesquisa sem acentos e seleciona o código por teclado', async () => {
    const user = userEvent.setup();
    render(<Example />);
    await user.type(screen.getByRole('combobox', { name: 'Organização' }), 'ACAO');
    expect(screen.getByLabelText('Valor persistido')).toHaveTextContent('""');
    expect(within(screen.getByRole('listbox')).getAllByRole('option')).toHaveLength(1);
    await user.keyboard('{ArrowDown}{Enter}');
    expect(screen.getByLabelText('Valor persistido')).toHaveTextContent('"acao"');
  });

  it('adiciona e remove opções na seleção múltipla', async () => {
    const user = userEvent.setup();
    render(<Example multiple />);
    const input = screen.getByRole('combobox', { name: 'Organização' });
    await user.type(input, 'acao');
    await user.click(screen.getByRole('option', { name: 'Ação pastoral' }));
    expect(screen.getByLabelText('Valor persistido')).toHaveTextContent('["acao"]');
    await user.click(screen.getByLabelText('Remover Ação pastoral'));
    expect(screen.getByLabelText('Valor persistido')).toHaveTextContent('[]');
  });

  it('não aceita texto livre e informa ausência de resultados', async () => {
    const user = userEvent.setup();
    render(<Example />);
    await user.type(screen.getByRole('combobox', { name: 'Organização' }), 'inexistente');
    expect(screen.getByText('Nenhuma opção encontrada')).toBeVisible();
    await user.keyboard('{Enter}{Tab}');
    expect(screen.getByLabelText('Valor persistido')).toHaveTextContent('""');
  });

  it('preserva opções indisponíveis e campos bloqueados', async () => {
    const user = userEvent.setup();
    render(<Example />);
    await user.type(screen.getByRole('combobox', { name: 'Organização' }), 'Restrita');
    expect(screen.getByRole('option', { name: 'Restrita' })).toHaveAttribute('aria-disabled', 'true');
    await user.keyboard('{ArrowDown}{Enter}');
    expect(screen.getByLabelText('Valor persistido')).toHaveTextContent('""');
    render(<SearchableSelect label="Encargo" value="paroco" disabled><MenuItem value="paroco">Pároco</MenuItem></SearchableSelect>);
    expect(screen.getByRole('combobox', { name: 'Encargo' })).toBeDisabled();
    expect(screen.getByRole('combobox', { name: 'Encargo' })).toHaveValue('Pároco');
  });

  it('pesquisa o texto de indicadores categóricos na edição', async () => {
    const user = userEvent.setup();
    render(<SearchableSelect label="Tipo" value="matriz">
      <MenuItem value="matriz"><StatusIcon label="Igreja Matriz" color="primary" icon={<ChurchIcon />} tooltip="Matriz" /></MenuItem>
    </SearchableSelect>);
    const input = screen.getByRole('combobox', { name: 'Tipo' });
    expect(input).toHaveValue('Igreja Matriz');
    await user.clear(input);
    await user.type(input, 'igreja');
    expect(screen.getByRole('option', { name: 'Igreja Matriz' })).toBeVisible();
  });

  it('integra validação, foco e payload com React Hook Form', async () => {
    const user = userEvent.setup();
    const save = vi.fn();
    function Form() {
      const { control, handleSubmit } = useForm({ defaultValues: { organizacao: '' } });
      return <form noValidate onSubmit={handleSubmit(save)}>
        <Controller name="organizacao" control={control} rules={{ required: 'Escolha uma organização' }}
          render={({ field, fieldState }) => <SearchableSelect {...field} label="Organização" required
            error={Boolean(fieldState.error)} helperText={fieldState.error?.message}>
            <MenuItem value="org-1">Pastoral</MenuItem>
          </SearchableSelect>} />
        <button type="submit">Salvar</button>
      </form>;
    }
    render(<Form />);
    await user.click(screen.getByRole('button', { name: 'Salvar' }));
    expect(screen.getByText('Escolha uma organização')).toBeVisible();
    const input = screen.getByRole('combobox', { name: 'Organização' });
    expect(input).toHaveFocus();
    await user.type(input, 'Pastoral');
    await user.keyboard('{ArrowDown}{Enter}');
    await user.click(screen.getByRole('button', { name: 'Salvar' }));
    expect(save.mock.calls[0][0]).toEqual({ organizacao: 'org-1' });
  });
});

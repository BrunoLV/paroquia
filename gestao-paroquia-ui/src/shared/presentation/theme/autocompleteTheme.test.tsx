import { render, screen } from '@testing-library/react';
import { IconButton, MenuItem, TextField } from '@mui/material';
import { ThemeProvider } from '@mui/material/styles';
import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import { createAppTheme } from './theme';

/** Compara estilos efetivos: listas pesquisáveis pertencem à mesma família visual dos campos de texto. */
describe.each(['light', 'dark'] as const)('Autocomplete no tema %s', (mode) => {
  const renderFields = (multiple = false) => render(
    <ThemeProvider theme={createAppTheme(mode)}>
      <TextField label="Referência" size="small" value="Texto" />
      <SearchableSelect label="Seleção" value={multiple ? ['BRA'] : 'BRA'} multiple={multiple}>
        <MenuItem value="">Nenhuma</MenuItem>
        <MenuItem value="BRA">Brasil</MenuItem>
      </SearchableSelect>
      <IconButton aria-label="Ação externa">+</IconButton>
    </ThemeProvider>,
  );

  test.each([false, true])('iguala padding, tipografia e superfície ao TextField (multiple=%s)', (multiple) => {
    renderFields(multiple);
    const reference = screen.getByLabelText('Referência');
    const selection = screen.getByRole('combobox', { name: 'Seleção' });
    const referenceRoot = getComputedStyle(reference.closest('.MuiOutlinedInput-root')!);
    const selectionRoot = getComputedStyle(selection.closest('.MuiOutlinedInput-root')!);

    // JSDOM não resolve a cascata de seletores descendentes do Autocomplete.
    // O contrato de padding é verificado no tema; geometria real é validada no navegador.
    expect(createAppTheme(mode).components?.MuiAutocomplete?.styleOverrides?.root).toMatchObject({
      '& .MuiOutlinedInput-root.MuiInputBase-sizeSmall': {
        paddingTop: '6px', paddingBottom: '6px', paddingLeft: '6px',
        '& .MuiAutocomplete-input': { padding: '11px 6px' },
      },
    });
    expect(selectionRoot.fontSize).toBe(referenceRoot.fontSize);
    expect(selectionRoot.borderRadius).toBe(referenceRoot.borderRadius);
    expect(selectionRoot.backgroundColor).toBe(referenceRoot.backgroundColor);
  });

  test('não aplica borda de ação à seta e limpeza, preservando botões externos', () => {
    renderFields();
    for (const label of ['Abrir opções', 'Limpar']) {
      expect(getComputedStyle(screen.getByTitle(label)).borderTopWidth).toBe('0px');
    }
    expect(getComputedStyle(screen.getByRole('button', { name: 'Ação externa' })).borderTopWidth).toBe('1px');
  });
});

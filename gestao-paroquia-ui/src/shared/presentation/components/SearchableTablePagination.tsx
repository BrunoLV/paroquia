import { useId } from 'react';
import { MenuItem, Stack, TableCell, TableFooter, TablePagination, TableRow, Typography, type TablePaginationProps } from '@mui/material';
import SearchableSelect from './SearchableSelect';

type Props = Omit<TablePaginationProps, 'onRowsPerPageChange' | 'component' | 'colSpan'> & {
  colSpan: number;
  onRowsPerPageChange?: (event: { target: { value: string } }) => void;
};

/** Rodapé semântico da tabela. A densidade compacta é local, sem alterar inputs de formulário. */
export default function SearchableTablePagination({
  rowsPerPageOptions = [5, 10, 25], labelRowsPerPage = 'Linhas por página:',
  onRowsPerPageChange, colSpan, ...props
}: Props) {
  const labelId = useId();
  return (
    <TableFooter><TableRow><TableCell colSpan={colSpan} sx={{ p: 0, borderTop: 1, borderColor: 'divider' }}>
    <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'flex-end', flexWrap: 'wrap', gap: 1, px: 2, py: 1,
      '& .MuiTablePagination-actions button': { width: 38, height: 38, boxSizing: 'border-box' } }}>
      {rowsPerPageOptions.length > 1 && (
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
        <Typography id={labelId} variant="body2">{labelRowsPerPage}</Typography>
        <SearchableSelect<number> value={props.rowsPerPage} inputProps={{ 'aria-labelledby': labelId }}
          disabled={props.disabled} sx={{ width: 80,
            '& .MuiOutlinedInput-root.MuiInputBase-sizeSmall': { height: 38, padding: 0,
              '& .MuiAutocomplete-input': { padding: '0 10px' } } }} fullWidth={false}
          onChange={({ target }) => {
            onRowsPerPageChange?.({ target: { value: String(target.value) } });
            props.onPageChange(null, 0);
          }}>
          {rowsPerPageOptions.map((option) => {
            const value = typeof option === 'number' ? option : option.value;
            return <MenuItem key={value} value={value}>{typeof option === 'number' ? String(option) : option.label}</MenuItem>;
          })}
        </SearchableSelect>
        </Stack>
      )}
      <TablePagination {...props} component="div" rowsPerPageOptions={[]}
        getItemAriaLabel={props.getItemAriaLabel ?? ((type) => ({ first: 'Primeira página', last: 'Última página', next: 'Próxima página', previous: 'Página anterior' })[type])}
        sx={[{ '& .MuiTablePagination-toolbar': { minHeight: 38, pl: 0, pr: 0 },
          '& .MuiTablePagination-spacer': { display: 'none' },
          '& .MuiTablePagination-actions': { ml: 1 },
          '& .MuiTablePagination-displayedRows': { my: 0 } },
          ...(Array.isArray(props.sx) ? props.sx : [props.sx])]}
        labelDisplayedRows={props.labelDisplayedRows ?? (({ from, to, count }) => `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`)} />
    </Stack>
    </TableCell></TableRow></TableFooter>
  );
}

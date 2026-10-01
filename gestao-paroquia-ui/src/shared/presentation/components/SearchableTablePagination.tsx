import { MenuItem, Stack, TablePagination, type TablePaginationProps } from '@mui/material';
import SearchableSelect from './SearchableSelect';

type Props = Omit<TablePaginationProps, 'onRowsPerPageChange'> & {
  component?: 'div';
  onRowsPerPageChange?: (event: { target: { value: string } }) => void;
};

/** Mantém a navegação MUI e torna pesquisável a quantidade, inclusive em telas compactas. */
export default function SearchableTablePagination({
  rowsPerPageOptions = [5, 10, 25], labelRowsPerPage = 'Linhas por página:',
  onRowsPerPageChange, ...props
}: Props) {
  return (
    <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'flex-end', flexWrap: 'wrap', gap: 1, px: 2 }}>
      {rowsPerPageOptions.length > 1 && (
        <SearchableSelect<number> label={labelRowsPerPage} value={props.rowsPerPage}
          disabled={props.disabled} sx={{ minWidth: 150 }} fullWidth={false}
          onChange={({ target }) => {
            onRowsPerPageChange?.({ target: { value: String(target.value) } });
            props.onPageChange(null, 0);
          }}>
          {rowsPerPageOptions.map((option) => {
            const value = typeof option === 'number' ? option : option.value;
            return <MenuItem key={value} value={value}>{typeof option === 'number' ? String(option) : option.label}</MenuItem>;
          })}
        </SearchableSelect>
      )}
      <TablePagination {...props} component="div" rowsPerPageOptions={[]}
        labelDisplayedRows={props.labelDisplayedRows ?? (({ from, to, count }) => `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`)} />
    </Stack>
  );
}

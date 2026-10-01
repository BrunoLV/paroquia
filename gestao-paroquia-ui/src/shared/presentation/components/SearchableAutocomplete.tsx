import { Autocomplete, type AutocompleteProps } from '@mui/material';

/** Base visual de listas fechadas: pesquisa por rótulo, teclado e mensagens em português. */
export default function SearchableAutocomplete<
  T, Multiple extends boolean | undefined = false, DisableClearable extends boolean | undefined = false,
>(props: AutocompleteProps<T, Multiple, DisableClearable, false>) {
  return <Autocomplete<T, Multiple, DisableClearable, false>
    noOptionsText="Nenhuma opção encontrada" loadingText="Carregando opções..."
    clearText="Limpar" openText="Abrir opções" closeText="Fechar opções"
    {...props} size="small" freeSolo={false} />;
}

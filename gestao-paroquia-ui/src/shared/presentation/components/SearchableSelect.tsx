import { Children, isValidElement, type ReactNode, type Ref, type FocusEventHandler, type InputHTMLAttributes } from 'react';
import { Chip, TextField, useFormControl, type TextFieldProps, type AutocompleteProps } from '@mui/material';
import CancelIcon from '@mui/icons-material/Cancel';
import SearchableAutocomplete from './SearchableAutocomplete';

type SelectionValue = string | number | readonly string[] | readonly number[];
interface Option { value: string | number; label: string; content: ReactNode; disabled: boolean }
interface OptionElementProps { value?: string | number; children?: ReactNode; label?: string; disabled?: boolean }

export interface SearchableSelectProps<T extends SelectionValue = string> {
  value?: T;
  onChange?: (event: { target: { value: T; name: string } }) => void;
  children?: ReactNode;
  label?: ReactNode;
  name?: string;
  id?: string;
  labelId?: string;
  multiple?: boolean;
  disabled?: boolean;
  required?: boolean;
  error?: boolean;
  helperText?: ReactNode;
  fullWidth?: boolean;
  size?: 'small' | 'medium';
  sx?: TextFieldProps['sx'];
  onBlur?: FocusEventHandler<HTMLInputElement | HTMLTextAreaElement>;
  ref?: Ref<HTMLInputElement>;
  inputRef?: Ref<HTMLInputElement>;
  inputProps?: InputHTMLAttributes<HTMLInputElement> & { 'data-testid'?: string };
  'data-testid'?: string;
}

/** Extrai o rótulo visível, inclusive indicadores categóricos, sem pesquisar códigos internos. */
function textLabel(node: ReactNode): string {
  if (typeof node === 'string' || typeof node === 'number') return String(node);
  if (Array.isArray(node)) return node.map(textLabel).join('');
  if (isValidElement<OptionElementProps>(node)) return node.props.label ?? textLabel(node.props.children);
  return '';
}

/** Opções declarativas preservam os códigos e restrições dos formulários existentes. */
function collectOptions(children: ReactNode): Option[] {
  return Children.toArray(children).flatMap((child) => {
    if (!isValidElement<OptionElementProps>(child)) return [];
    const { value, children: content, disabled = false } = child.props;
    return value === undefined ? collectOptions(content) : [{ value, content, label: textLabel(content), disabled }];
  });
}

/**
 * Seleção fechada com pesquisa uniforme. O texto da busca é independente do valor
 * persistido; MenuItems são descritores de opções, nunca uma lista Select renderizada.
 * O contexto proprietário continua responsável por permissões e dependências.
 */
export default function SearchableSelect<T extends SelectionValue = string>(props: SearchableSelectProps<T>) {
  const context = useFormControl();
  const { value, children, multiple = false, onChange, name = '', label, id, helperText } = props;
  const options = collectOptions(children);
  const values: readonly (string | number | undefined)[] = typeof value === 'object' ? value : [value];
  const selected = values.flatMap((code) => {
    if (code === undefined || (code === '' && !options.some((option) => option.value === ''))) return [];
    return [options.find((option) => option.value === code) ?? { value: code, label: String(code), content: String(code), disabled: true }];
  });
  const required = props.required ?? context?.required ?? false;
  const renderSelected: NonNullable<AutocompleteProps<Option, boolean, boolean, false>['renderValue']> = (items, getItemProps) => {
    if (!Array.isArray(items)) return items.label;
    return items.map((option, index) => {
      const { key, ...itemProps } = { key: option.value, ...getItemProps({ index }) };
      return <Chip key={key} {...itemProps} size="small" label={option.label}
        deleteIcon={<CancelIcon aria-label={`Remover ${option.label}`} />} />;
    });
  };
  return (
    <SearchableAutocomplete<Option, boolean, boolean>
      id={id}
      data-testid={props['data-testid']}
      fullWidth={props.fullWidth ?? true}
      size="small"
      sx={props.sx}
      options={options}
      multiple={multiple}
      value={multiple ? selected : selected[0] ?? null}
      disabled={props.disabled ?? context?.disabled}
      disableClearable={required || !options.some((option) => option.value === '')}
      getOptionLabel={(option) => option.label}
      getOptionDisabled={(option) => option.disabled}
      isOptionEqualToValue={(option, current) => option.value === current.value}
      noOptionsText="Nenhuma opção encontrada"
      clearText="Limpar"
      openText="Abrir opções"
      closeText="Fechar opções"
      onChange={(_, next) => {
        const code = Array.isArray(next) ? next.map((option) => option.value) : next?.value ?? '';
        // A forma simples/múltipla segue o contrato tipado do campo proprietário.
        onChange?.({ target: { value: code as T, name } });
      }}
      renderOption={({ key, ...optionProps }, option) => <li key={key} {...optionProps}>{option.content}</li>}
      renderValue={multiple ? renderSelected : undefined}
      renderInput={(params) => (
        <TextField {...params} fullWidth label={label} name={name} required={required} size="small"
          error={props.error ?? context?.error} helperText={helperText} onBlur={props.onBlur}
          inputRef={props.inputRef ?? props.ref}
          slotProps={{ ...params.slotProps, htmlInput: { ...params.slotProps.htmlInput, ...props.inputProps } }} />
      )}
    />
  );
}

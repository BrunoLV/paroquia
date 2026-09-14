import React, { useEffect, useRef, useState } from 'react';
import { Box, InputAdornment, TextField, type SxProps, type Theme } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';

interface SearchFieldProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  sx?: SxProps<Theme>;
  /** Atraso do debounce antes de propagar onChange, em ms. Default: 300. */
  debounceMs?: number;
}

/** Campo de busca canônico do sistema (ver DESIGN_SYSTEM.md). Debounça onChange para não filtrar/refetch a cada tecla. */
const SearchField: React.FC<SearchFieldProps> = ({ value, onChange, placeholder, sx, debounceMs = 300 }) => {
  const [draft, setDraft] = useState(value);
  const onChangeRef = useRef(onChange);
  onChangeRef.current = onChange;

  useEffect(() => {
    setDraft(value);
  }, [value]);

  useEffect(() => {
    if (draft === value) return;
    const timer = setTimeout(() => onChangeRef.current(draft), debounceMs);
    return () => clearTimeout(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [draft, debounceMs]);

  return (
    <Box sx={{ maxWidth: 400, ...sx }}>
      <TextField
        fullWidth
        size="small"
        placeholder={placeholder}
        value={draft}
        onChange={(e) => setDraft(e.target.value)}
        slotProps={{
          input: {
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon color="action" />
              </InputAdornment>
            ),
          },
        }}
      />
    </Box>
  );
};

export default SearchField;

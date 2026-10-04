import React from 'react';
import Box from '@mui/material/Box';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

interface ColorPickerFieldProps {
  label: string;
  value: string;
  onChange: (newValue: string) => void;
  disabled?: boolean;
  helperText?: string;
}

export const ColorPickerField: React.FC<ColorPickerFieldProps> = ({
  label,
  value,
  onChange,
  disabled = false,
  helperText,
}) => {
  const isValidHex = /^#([A-Fa-f0-9]{6})$/.test(value);

  const handleHexChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    let val = e.target.value;
    if (!val.startsWith('#') && val.length > 0) {
      val = `#${val}`;
    }
    onChange(val.toUpperCase());
  };

  const handleNativeColorChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChange(e.target.value.toUpperCase());
  };

  return (
    <Stack spacing={1}>
      <Typography variant="subtitle2" color="text.secondary">
        {label}
      </Typography>
      <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
        <Box
          component="label"
          sx={{
            width: 40,
            height: 40,
            borderRadius: 1,
            border: '2px solid',
            borderColor: 'divider',
            backgroundColor: isValidHex ? value : '#CCCCCC',
            cursor: disabled ? 'not-allowed' : 'pointer',
            display: 'inline-block',
            position: 'relative',
            overflow: 'hidden',
            flexShrink: 0,
            boxShadow: '0 1px 3px rgba(0,0,0,0.12)',
            '&:hover': {
              borderColor: 'primary.main',
            },
          }}
        >
          <input
            type="color"
            value={isValidHex ? value : '#000000'}
            onChange={handleNativeColorChange}
            disabled={disabled}
            style={{
              position: 'absolute',
              top: -10,
              left: -10,
              width: 60,
              height: 60,
              opacity: 0,
              cursor: disabled ? 'not-allowed' : 'pointer',
            }}
          />
        </Box>
        <TextField
          size="small"
          value={value}
          onChange={handleHexChange}
          disabled={disabled}
          placeholder="#000000"
          error={!isValidHex && value.length > 0}
          helperText={!isValidHex && value.length > 0 ? 'Hexadecimal inválido (#RRGGBB)' : helperText}
          slotProps={{
            htmlInput: {
              maxLength: 7,
              style: { fontFamily: 'monospace', textTransform: 'uppercase' },
            },
          }}
        />
      </Stack>
    </Stack>
  );
};

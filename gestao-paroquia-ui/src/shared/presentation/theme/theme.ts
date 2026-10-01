import {createTheme, type PaletteMode} from '@mui/material/styles';

interface CustomThemeTokens {
  headerIconBg: string;
  subtlePanelBg: string;
  surfaceMuted: string;
  surfaceRaised: string;
  focusRing: string;
  navigationSelectedBg: string;
  navigationHoverBg: string;
  borderSubtle: string;
}

declare module '@mui/material/styles' {
  interface Theme {
    custom: CustomThemeTokens;
  }
  interface ThemeOptions {
    custom?: CustomThemeTokens;
  }
}

const customTokensByMode: Record<PaletteMode, CustomThemeTokens> = {
  light: {
    headerIconBg: 'rgba(26, 35, 126, 0.08)',
    subtlePanelBg: '#F8F9FA',
    surfaceMuted: '#F3F5F8',
    surfaceRaised: '#FFFFFF',
    focusRing: 'rgba(26, 35, 126, 0.28)',
    navigationSelectedBg: 'rgba(26, 35, 126, 0.10)',
    navigationHoverBg: 'rgba(26, 35, 126, 0.05)',
    borderSubtle: 'rgba(26, 35, 126, 0.14)',
  },
  dark: {
    headerIconBg: 'rgba(121, 134, 203, 0.16)',
    subtlePanelBg: '#242430',
    surfaceMuted: '#2A2A38',
    surfaceRaised: '#2E2E3D',
    focusRing: 'rgba(121, 134, 203, 0.45)',
    navigationSelectedBg: 'rgba(121, 134, 203, 0.20)',
    navigationHoverBg: 'rgba(121, 134, 203, 0.10)',
    borderSubtle: 'rgba(121, 134, 203, 0.24)',
  },
};

/** Fábrica de tema — mesmos tokens semânticos/estruturais em light e dark, só a paleta muda. */
export const createAppTheme = (mode: PaletteMode) => {
  const isDark = mode === 'dark';
  const custom = customTokensByMode[mode];

  return createTheme({
    palette: {
      mode,
      primary: {
        main: isDark ? '#7986CB' : '#1A237E', // Marian Blue (mais claro no dark p/ contraste)
      },
      secondary: {
        main: '#C59B27', // Gold
      },
      error: {
        main: isDark ? '#EF5350' : '#C62828', // Crimson Red
      },
      success: {
        main: isDark ? '#66BB6A' : '#2E7D32',
      },
      warning: {
        main: isDark ? '#FFA726' : '#ED6C02',
      },
      info: {
        main: isDark ? '#4FC3F7' : '#0288D1',
      },
      background: {
        default: isDark ? '#16161F' : '#FFFDF4', // Cream / Light Yellow no light
        paper: isDark ? '#1E1E29' : '#FFFFFF',
      },
      divider: isDark ? 'rgba(255, 255, 255, 0.12)' : 'rgba(0, 0, 0, 0.12)',
    },
    custom: customTokensByMode[mode],
    shape: {
      borderRadius: 12,
    },
    spacing: 7,
    typography: {
      fontFamily: [
        'IBM Plex Sans Variable',
        'IBM Plex Sans',
        'sans-serif',
      ].join(','),
      fontSize: 14,
      h1: { fontSize: '1.75rem', fontWeight: 700, color: isDark ? '#B3BEEA' : '#1A237E' },
      h2: { fontSize: '1.5rem', fontWeight: 700, color: isDark ? '#B3BEEA' : '#1A237E' },
      h3: { fontSize: '1.25rem', fontWeight: 700, color: isDark ? '#B3BEEA' : '#1A237E' },
      h4: { fontSize: '1.125rem', fontWeight: 700, color: isDark ? '#B3BEEA' : '#1A237E' },
      h5: { fontSize: '1rem', fontWeight: 700, color: isDark ? '#B3BEEA' : '#1A237E' },
      h6: { fontSize: '0.9375rem', fontWeight: 700, color: isDark ? '#B3BEEA' : '#1A237E' },
      subtitle1: { fontSize: '0.875rem' },
      subtitle2: { fontSize: '0.8125rem' },
      body1: { fontSize: '0.875rem' },
      body2: { fontSize: '0.8125rem' },
      button: { fontSize: '0.8125rem' },
      caption: { fontSize: '0.6875rem' },
      overline: { fontSize: '0.6875rem' },
    },
    components: {
      MuiButton: {
        // Fundo sólido por padrão em todo o sistema — cobre também os poucos lugares
        // (ex.: módulo de Reservas) que usam <Button> do MUI direto em vez de ActionButton.
        defaultProps: {
          variant: 'contained',
        },
        styleOverrides: {
          root: {
            textTransform: 'none',
            fontWeight: 600,
            borderRadius: '10px',
            minHeight: '32px',
            padding: '4px 10px',
            whiteSpace: 'nowrap',
            boxShadow: 'none',
            '&:focus-visible': {
              outline: '3px solid',
              outlineColor: custom.focusRing,
              outlineOffset: 2,
            },
          },
          // Força texto/ícone brancos no botão sólido — o cálculo automático de contraste do
          // MUI escolhe preto para cores claras (ex.: o dourado do secondary), o que a
          // diretriz visual do sistema não quer.
          contained: { boxShadow: '0 4px 12px rgba(26, 35, 126, 0.18)', color: '#FFFFFF' },
        },
      },
      MuiIconButton: {
        styleOverrides: {
          root: {
            borderRadius: '8px',
            border: '1px solid',
            borderColor: custom.borderSubtle,
            padding: '6px',
            '&:focus-visible': {
              outline: '3px solid',
              outlineColor: custom.focusRing,
              outlineOffset: 2,
            },
          },
        },
      },
      MuiOutlinedInput: {
        styleOverrides: {
          root: {
            borderRadius: '10px',
            fontSize: '0.875rem',
            backgroundColor: isDark ? '#1E1E29' : '#FFFFFF',
            '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
              borderWidth: 2,
            },
          },
          input: {
            padding: '11px 12px',
          },
          sizeSmall: {
            paddingTop: '6px',
            paddingBottom: '6px',
          },
        },
      },
      MuiAutocomplete: {
        // O MUI compacta o input interno e reutiliza IconButton para adornos.
        // Alinha a densidade ao TextField sem transformar seta/limpeza em ações delineadas.
        styleOverrides: {
          root: {
            '& .MuiOutlinedInput-root.MuiInputBase-sizeSmall': {
              paddingTop: '6px',
              paddingBottom: '6px',
              paddingLeft: '6px',
              '& .MuiAutocomplete-input': {
                padding: '11px 6px',
              },
            },
          },
          popupIndicator: { border: 0, padding: '2px' },
          clearIndicator: { border: 0, padding: '2px' },
        },
      },
      MuiLink: {
        defaultProps: {
          underline: 'hover',
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            borderRadius: '12px',
            boxShadow: isDark ? '0 1px 2px rgba(0, 0, 0, 0.24)' : '0 1px 2px rgba(16, 24, 40, 0.04)',
            border: '1px solid',
            borderColor: custom.borderSubtle,
          },
        },
      },
      MuiPaper: {
        styleOverrides: {
          rounded: {
            borderRadius: '12px',
          },
          root: {
            backgroundImage: 'none',
          },
        },
      },
      MuiChip: {
        // Chip é etiqueta/rótulo informativo (status, categoria, tag), não botão — o padrão do
        // MUI é "filled" (fundo sólido), que agora se confundiria com o estilo dos botões
        // reais do sistema. Default outlined; quem precisar de destaque sólido passa
        // variant="filled" explicitamente.
        defaultProps: {
          variant: 'outlined',
        },
        styleOverrides: {
          root: {
            borderRadius: '8px',
          },
          sizeMedium: {
            height: '28px',
          },
          sizeSmall: {
            height: '22px',
          },
        },
      },
      MuiTableCell: {
        styleOverrides: {
          root: {
            padding: '10px 12px',
            borderColor: custom.borderSubtle,
          },
          head: {
            color: isDark ? '#E4E4EC' : '#344054',
            fontWeight: 700,
          },
        },
      },
      MuiTableHead: {
        styleOverrides: {
          root: {
            backgroundColor: custom.surfaceMuted,
          },
        },
      },
      MuiTableRow: {
        styleOverrides: {
          root: {
            '&:last-child td, &:last-child th': { borderBottom: 0 },
            '&.MuiTableRow-hover:hover': { backgroundColor: custom.navigationHoverBg },
          },
        },
      },
      MuiListItemButton: {
        styleOverrides: {
          root: {
            borderRadius: '10px',
            '&:hover': { backgroundColor: custom.navigationHoverBg },
            '&.Mui-selected': { backgroundColor: custom.navigationSelectedBg },
          },
        },
      },
      MuiAlert: {
        styleOverrides: {
          root: { borderRadius: '12px', alignItems: 'center' },
        },
      },
      MuiMenu: {
        styleOverrides: {
          paper: {
            border: '1px solid',
            borderColor: custom.borderSubtle,
            boxShadow: isDark ? '0 12px 28px rgba(0, 0, 0, 0.4)' : '0 12px 28px rgba(16, 24, 40, 0.12)',
          },
        },
      },
      MuiPaginationItem: {
        styleOverrides: {
          root: {
            borderRadius: '8px',
            '&.Mui-selected': { fontWeight: 700 },
          },
        },
      },
      MuiSkeleton: {
        styleOverrides: {
          root: { backgroundColor: custom.borderSubtle },
        },
      },
      MuiCardContent: {
        styleOverrides: {
          root: {
            padding: 16,
            '&:last-child': { paddingBottom: 16 },
          },
        },
      },
      MuiDialog: {
        styleOverrides: {
          paper: {
            borderRadius: '12px',
          },
        },
      },
      MuiDialogTitle: {
        styleOverrides: {
          root: {
            borderBottom: '1px solid',
            borderColor: isDark ? 'rgba(255, 255, 255, 0.12)' : 'rgba(0, 0, 0, 0.12)',
            paddingBottom: '12px',
            fontWeight: 700,
          },
        },
      },
      MuiDialogActions: {
        styleOverrides: {
          root: {
            borderTop: '1px solid',
            borderColor: isDark ? 'rgba(255, 255, 255, 0.12)' : 'rgba(0, 0, 0, 0.12)',
            padding: '16px 24px',
            gap: '8px',
          },
        },
      },
    },
  });
};

/** Tema padrão (claro) — mantido como export default para os consumidores estáticos existentes. */
const theme = createAppTheme('light');

export default theme;

import {createTheme} from '@mui/material/styles';

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

const theme = createTheme({
  palette: {
    primary: {
      main: '#1A237E', // Marian Blue
    },
    secondary: {
      main: '#C59B27', // Gold
    },
    error: {
      main: '#C62828', // Crimson Red
    },
    success: {
      main: '#2E7D32',
    },
    warning: {
      main: '#ED6C02',
    },
    info: {
      main: '#0288D1',
    },
    background: {
      default: '#FFFDF4', // Cream / Light Yellow
      paper: '#FFFFFF', // White
    },
    divider: 'rgba(0, 0, 0, 0.12)',
  },
  custom: {
    headerIconBg: 'rgba(26, 35, 126, 0.08)',
    subtlePanelBg: '#F8F9FA',
    surfaceMuted: '#F3F5F8',
    surfaceRaised: '#FFFFFF',
    focusRing: 'rgba(26, 35, 126, 0.28)',
    navigationSelectedBg: 'rgba(26, 35, 126, 0.10)',
    navigationHoverBg: 'rgba(26, 35, 126, 0.05)',
    borderSubtle: 'rgba(26, 35, 126, 0.14)',
  },
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
    h1: {
      fontSize: '1.75rem',
      fontWeight: 700,
      color: '#1A237E',
    },
    h2: {
      fontSize: '1.5rem',
      fontWeight: 700,
      color: '#1A237E',
    },
    h3: {
      fontSize: '1.25rem',
      fontWeight: 700,
      color: '#1A237E',
    },
    h4: {
      fontSize: '1.125rem',
      fontWeight: 700,
      color: '#1A237E',
    },
    h5: {
      fontSize: '1rem',
      fontWeight: 700,
      color: '#1A237E',
    },
    h6: {
      fontSize: '0.9375rem',
      fontWeight: 700,
      color: '#1A237E',
    },
    subtitle1: {
      fontSize: '0.875rem',
    },
    subtitle2: {
      fontSize: '0.8125rem',
    },
    body1: {
      fontSize: '0.875rem',
    },
    body2: {
      fontSize: '0.8125rem',
    },
    button: {
      fontSize: '0.8125rem',
    },
    caption: {
      fontSize: '0.6875rem',
    },
    overline: {
      fontSize: '0.6875rem',
    },
  },
  components: {
    MuiButton: {
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
            outlineColor: 'rgba(26, 35, 126, 0.28)',
            outlineOffset: 2,
          },
        },
        contained: { boxShadow: '0 4px 12px rgba(26, 35, 126, 0.18)' },
      },
    },
    MuiIconButton: {
      styleOverrides: {
        root: {
          borderRadius: '8px',
          border: '1px solid rgba(26, 35, 126, 0.14)',
          padding: '6px',
          '&:focus-visible': {
            outline: '3px solid rgba(26, 35, 126, 0.28)',
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
          backgroundColor: '#FFFFFF',
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
    MuiLink: {
      defaultProps: {
        underline: 'hover',
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: '12px',
          boxShadow: '0 1px 2px rgba(16, 24, 40, 0.04)',
          border: '1px solid rgba(26, 35, 126, 0.14)',
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
          borderColor: 'rgba(26, 35, 126, 0.10)',
        },
        head: {
          color: '#344054',
          fontWeight: 700,
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: '#F3F5F8',
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          '&:last-child td, &:last-child th': { borderBottom: 0 },
          '&.MuiTableRow-hover:hover': { backgroundColor: 'rgba(26, 35, 126, 0.03)' },
        },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: '10px',
          '&:hover': { backgroundColor: 'rgba(26, 35, 126, 0.05)' },
          '&.Mui-selected': { backgroundColor: 'rgba(26, 35, 126, 0.10)' },
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
          border: '1px solid rgba(26, 35, 126, 0.14)',
          boxShadow: '0 12px 28px rgba(16, 24, 40, 0.12)',
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
        root: { backgroundColor: 'rgba(26, 35, 126, 0.08)' },
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
          borderBottom: '1px solid rgba(0, 0, 0, 0.12)',
          paddingBottom: '12px',
          fontWeight: 700,
        },
      },
    },
    MuiDialogActions: {
      styleOverrides: {
        root: {
          borderTop: '1px solid rgba(0, 0, 0, 0.12)',
          padding: '16px 24px',
          gap: '8px',
        },
      },
    },
  },
});


export default theme;

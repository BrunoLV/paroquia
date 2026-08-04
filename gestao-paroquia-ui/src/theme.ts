import {createTheme} from '@mui/material/styles';

interface CustomThemeTokens {
  headerIconBg: string;
  subtlePanelBg: string;
}

declare module '@mui/material/styles' {
  interface Theme {
    custom: CustomThemeTokens;
  }
  interface ThemeOptions {
    custom?: CustomThemeTokens;
  }
}

const DENSITY_SCALE = 0.875; // ~12.5% reduction, keeps body1 at the 14px accessibility floor

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
    background: {
      default: '#FFFDF4', // Cream / Light Yellow
      paper: '#FFFFFF', // White
    },
  },
  custom: {
    headerIconBg: 'rgba(26, 35, 126, 0.08)',
    subtlePanelBg: '#F8F9FA',
  },
  shape: {
    borderRadius: 12,
  },
  spacing: 8 * DENSITY_SCALE,
  typography: {
    fontFamily: [
      'Roboto',
      'Inter',
      'sans-serif',
    ].join(','),
    h1: {
      fontSize: `${2.5 * DENSITY_SCALE}rem`,
      fontWeight: 500,
    },
    h2: {
      fontSize: `${3.75 * DENSITY_SCALE}rem`,
    },
    h3: {
      fontSize: `${3 * DENSITY_SCALE}rem`,
    },
    h4: {
      fontSize: `${2.125 * DENSITY_SCALE}rem`,
    },
    h5: {
      fontSize: `${1.5 * DENSITY_SCALE}rem`,
    },
    h6: {
      fontSize: `${1.25 * DENSITY_SCALE}rem`,
    },
    subtitle1: {
      fontSize: `${1 * DENSITY_SCALE}rem`,
    },
    subtitle2: {
      fontSize: `${0.875 * DENSITY_SCALE}rem`,
    },
    body1: {
      fontSize: `${1 * DENSITY_SCALE}rem`,
    },
    body2: {
      fontSize: `${0.875 * DENSITY_SCALE}rem`,
    },
    button: {
      fontSize: `${0.875 * DENSITY_SCALE}rem`,
    },
    caption: {
      fontSize: `${0.75 * DENSITY_SCALE}rem`,
    },
    overline: {
      fontSize: `${0.75 * DENSITY_SCALE}rem`,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          borderRadius: '10px',
        },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: '10px',
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
          borderRadius: '16px',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: '16px',
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: '8px',
        },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          borderRadius: '16px',
        },
      },
    },
    MuiDialogTitle: {
      styleOverrides: {
        root: {
          borderBottom: '1px solid rgba(0, 0, 0, 0.12)',
          paddingBottom: '16px',
          fontWeight: 700,
        },
      },
    },
    MuiDialogActions: {
      styleOverrides: {
        root: {
          borderTop: '1px solid rgba(0, 0, 0, 0.12)',
          padding: '16px 24px',
        },
      },
    },
  },
});


export default theme;

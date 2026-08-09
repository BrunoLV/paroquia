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

const DENSITY_SCALE = 0.8; // 20% reduction (max allowed) across typography, spacing and component sizing
const BODY_TEXT_FONT_SIZE_REM = 0.875; // body1 stays at the 14px accessibility floor, exempt from DENSITY_SCALE

const scaledRem = (baseRem: number): string => `${Math.round(baseRem * DENSITY_SCALE * 10000) / 10000}rem`;

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
    fontSize: 14,
    h1: {
      fontSize: scaledRem(2.5),
      fontFamily: "'Merriweather', 'Georgia', serif",
      fontWeight: 600,
      color: '#1A237E',
    },
    h2: {
      fontSize: scaledRem(3.75),
      fontFamily: "'Merriweather', 'Georgia', serif",
      fontWeight: 600,
      color: '#1A237E',
    },
    h3: {
      fontSize: scaledRem(3),
      fontFamily: "'Merriweather', 'Georgia', serif",
      fontWeight: 600,
      color: '#1A237E',
    },
    h4: {
      fontSize: scaledRem(2.125),
      fontFamily: "'Merriweather', 'Georgia', serif",
      fontWeight: 600,
      color: '#1A237E',
    },
    h5: {
      fontSize: scaledRem(1.5),
      fontFamily: "'Merriweather', 'Georgia', serif",
      fontWeight: 600,
      color: '#1A237E',
    },
    h6: {
      fontSize: scaledRem(1.25),
      fontFamily: "'Merriweather', 'Georgia', serif",
      fontWeight: 600,
      color: '#1A237E',
    },
    subtitle1: {
      fontSize: scaledRem(1),
    },
    subtitle2: {
      fontSize: scaledRem(0.875),
    },
    body1: {
      fontSize: `${BODY_TEXT_FONT_SIZE_REM}rem`,
    },
    body2: {
      fontSize: scaledRem(0.875),
    },
    button: {
      fontSize: scaledRem(0.875),
    },
    caption: {
      fontSize: scaledRem(0.75),
    },
    overline: {
      fontSize: scaledRem(0.75),
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          fontWeight: 600,
          borderRadius: '10px',
          padding: `${Math.round(6 * DENSITY_SCALE)}px ${Math.round(16 * DENSITY_SCALE)}px`,
        },
      },
    },
    MuiIconButton: {
      styleOverrides: {
        root: {
          borderRadius: '8px',
          border: '1px solid rgba(0, 0, 0, 0.12)',
          padding: `${Math.round(8 * DENSITY_SCALE)}px`,
        },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: '10px',
          fontSize: '0.875rem',
        },
        input: {
          padding: `${Math.round(16.5 * DENSITY_SCALE)}px ${Math.round(14 * DENSITY_SCALE)}px`,
        },
        sizeSmall: {
          paddingTop: `${Math.round(8.5 * DENSITY_SCALE)}px`,
          paddingBottom: `${Math.round(8.5 * DENSITY_SCALE)}px`,
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
          boxShadow: 'none',
          border: '1px solid rgba(0, 0, 0, 0.12)',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        rounded: {
          borderRadius: '16px',
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: '8px',
        },
        sizeMedium: {
          height: `${Math.round(32 * DENSITY_SCALE)}px`,
        },
        sizeSmall: {
          height: `${Math.round(24 * DENSITY_SCALE)}px`,
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          padding: `${Math.round(16 * DENSITY_SCALE)}px`,
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
          gap: `${Math.round(8 * DENSITY_SCALE)}px`,
        },
      },
    },
  },
});


export default theme;

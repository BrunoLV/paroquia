import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1A237E', // Marian Blue
    },
    secondary: {
      main: '#FFB300', // Gold / Yellow
    },
  },
  typography: {
    h1: {
      fontSize: '2.5rem',
      fontWeight: 500,
    },
  },
});

export default theme;

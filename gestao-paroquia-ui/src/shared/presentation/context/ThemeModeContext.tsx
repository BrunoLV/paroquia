import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { ThemeProvider } from '@mui/material/styles';
import type { PaletteMode } from '@mui/material';
import CssBaseline from '@mui/material/CssBaseline';
import { createAppTheme } from '@/shared/presentation/theme/theme';

const STORAGE_KEY = 'paroquia-theme-mode';

interface ThemeModeContextValue {
  mode: PaletteMode;
  toggleMode: () => void;
}

const ThemeModeContext = createContext<ThemeModeContextValue | undefined>(undefined);

const getInitialMode = (): PaletteMode => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === 'light' || stored === 'dark') return stored;
  } catch {
    // localStorage indisponível (ex.: navegação privada) — cai no fallback do sistema.
  }
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
};

/** Provider único de tema light/dark — persiste a escolha do usuário e substitui o ThemeProvider estático. */
export const ThemeModeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [mode, setMode] = useState<PaletteMode>(getInitialMode);

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, mode);
    } catch {
      // ignora falha de persistência — modo continua válido só nesta sessão.
    }
  }, [mode]);

  const toggleMode = () => setMode((prev) => (prev === 'dark' ? 'light' : 'dark'));

  const theme = useMemo(() => createAppTheme(mode), [mode]);
  const value = useMemo(() => ({ mode, toggleMode }), [mode]);

  return (
    <ThemeModeContext.Provider value={value}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        {children}
      </ThemeProvider>
    </ThemeModeContext.Provider>
  );
};

const noopContext: ThemeModeContextValue = { mode: 'light', toggleMode: () => {} };

/** Fora de um ThemeModeProvider (ex.: componente montado isolado em teste), cai em light/no-op. */
export const useThemeMode = (): ThemeModeContextValue => useContext(ThemeModeContext) ?? noopContext;

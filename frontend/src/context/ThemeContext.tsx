import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';

type Theme = 'light' | 'dark';
interface ThemeContextValue { theme: Theme; toggleTheme: () => void; }
const ThemeContext = createContext<ThemeContextValue | null>(null);
const STORAGE_KEY = 'kodbtw_theme';

function resolveInitialTheme(): Theme {
  try {
    const saved = window.localStorage.getItem(STORAGE_KEY);
    if (saved === 'light' || saved === 'dark') return saved;
  } catch {
    // Storage can be unavailable in private or restricted browser contexts.
  }
  return window.matchMedia?.('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
}

/** Apply the persisted/system theme before React paints the first route. */
export function applyInitialTheme(): void {
  if (typeof document !== 'undefined' && typeof window !== 'undefined') {
    document.documentElement.dataset.theme = resolveInitialTheme();
  }
}

function getInitialTheme(): Theme {
  const theme = resolveInitialTheme();
  if (typeof document !== 'undefined') document.documentElement.dataset.theme = theme;
  return theme;
}

export const ThemeProvider: React.FC<React.PropsWithChildren> = ({ children }) => {
  const [theme, setTheme] = useState<Theme>(getInitialTheme);
  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    try {
      window.localStorage.setItem(STORAGE_KEY, theme);
    } catch {
      // The in-memory theme still works when browser storage is unavailable.
    }
  }, [theme]);
  const value = useMemo(() => ({ theme, toggleTheme: () => setTheme((current) => current === 'dark' ? 'light' : 'dark') }), [theme]);
  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
};

export function useTheme(): ThemeContextValue {
  const context = useContext(ThemeContext);
  if (!context) throw new Error('useTheme must be used inside ThemeProvider');
  return context;
}

import React, { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';
import { User } from '../types/auth';
import { authApi } from '../services/api/authApi';
import { TOKEN_STORAGE_KEY } from '../services/api/apiClient';
import { AUTH_TIMEOUT_MESSAGES } from '../services/api/authTimeoutMessages';
import { runPostRegistrationLogin } from '../services/api/authErrors';
import { restoreSession } from '../services/api/sessionRestore';

export interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  sessionUnavailable: boolean;
  retrySession: () => Promise<void>;
  login: (email: string, password: string) => Promise<void>;
  loginWithGoogle: (credential: string, challengeId: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_STORAGE_KEY));
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [sessionUnavailable, setSessionUnavailable] = useState(false);
  const validationAttempt = useRef(0);

  const validateStoredSession = useCallback(async (signal?: AbortSignal): Promise<void> => {
    const attempt = ++validationAttempt.current;
    setIsLoading(true);
    const storedToken = localStorage.getItem(TOKEN_STORAGE_KEY);
    const result = await restoreSession(
      storedToken,
      (requestSignal) => authApi.getMe(requestSignal),
      () => {
        if (validationAttempt.current === attempt) {
          localStorage.removeItem(TOKEN_STORAGE_KEY);
        }
      },
      signal,
    );

    if (signal?.aborted || validationAttempt.current !== attempt || result.status === 'cancelled') return;

    if (result.status === 'authenticated') {
      setUser(result.user);
      setToken(storedToken);
      setSessionUnavailable(false);
    } else if (result.status === 'unavailable') {
      // Keep the saved token so a temporary timeout/network/server error does
      // not silently destroy a potentially valid session. Protected routes
      // remain blocked until validation succeeds.
      setUser(null);
      setToken(storedToken);
      setSessionUnavailable(true);
    } else {
      setUser(null);
      setToken(null);
      setSessionUnavailable(false);
    }

    setIsLoading(false);
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    void validateStoredSession(controller.signal);

    const handleUnauthorized = () => {
      validationAttempt.current += 1;
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      setUser(null);
      setToken(null);
      setSessionUnavailable(false);
      setIsLoading(false);
    };

    window.addEventListener('auth:unauthorized', handleUnauthorized);
    return () => {
      controller.abort();
      window.removeEventListener('auth:unauthorized', handleUnauthorized);
    };
  }, [validateStoredSession]);

  const retrySession = useCallback(() => validateStoredSession(), [validateStoredSession]);

  const loginWithTimeoutMessage = async (
    email: string,
    password: string,
    timeoutMessage?: string,
  ): Promise<void> => {
    validationAttempt.current += 1;
    const response = await authApi.login({ email, password }, timeoutMessage);
    localStorage.setItem(TOKEN_STORAGE_KEY, response.token);
    setToken(response.token);
    setUser(response.user);
    setSessionUnavailable(false);
  };

  const login = async (email: string, password: string): Promise<void> => {
    await loginWithTimeoutMessage(email, password);
  };

  const loginWithGoogle = useCallback(async (credential: string, challengeId: string): Promise<void> => {
    validationAttempt.current += 1;
      const response = await authApi.googleLogin({ credential, challengeId });
    localStorage.setItem(TOKEN_STORAGE_KEY, response.token);
    setToken(response.token);
    setUser(response.user);
    setSessionUnavailable(false);
  }, []);

  const register = async (name: string, email: string, password: string): Promise<void> => {
    validationAttempt.current += 1;
    await authApi.register({ name, email, password });
    // Auto-login after successful registration
    await runPostRegistrationLogin(() =>
      loginWithTimeoutMessage(email, password, AUTH_TIMEOUT_MESSAGES.registrationAutoLogin));
  };

  const logout = (): void => {
    validationAttempt.current += 1;
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    setToken(null);
    setUser(null);
    setSessionUnavailable(false);
  };

  const value: AuthContextType = {
    user,
    token,
    isAuthenticated: !!token && !!user,
    isLoading,
    sessionUnavailable,
    retrySession,
    login,
    loginWithGoogle,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

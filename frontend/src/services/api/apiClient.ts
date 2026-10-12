import type { ApiErrorResponse } from '../../types/auth';

export const TOKEN_STORAGE_KEY = 'kodbtw_jwt';

function safeApiErrorMessage(status: number): string {
  if (status === 0) return 'Unable to reach KodBTW. Check your connection and try again.';
  if (status === 408) return 'The request timed out. Please try again.';
  if (status === 400 || status === 422) return 'Please check the information you entered and try again.';
  if (status === 401) return 'Authentication was not accepted. Check your sign-in details or sign in again.';
  if (status === 403) return 'You do not have permission to complete this request.';
  if (status === 404) return 'The requested information could not be found.';
  if (status === 409) return 'This request conflicts with existing information.';
  if (status === 429) return 'Too many requests. Please wait a moment and try again.';
  if (status >= 500) return 'KodBTW could not complete the request. Please try again later.';
  return 'The request could not be completed. Please try again.';
}

export class ApiError extends Error {
  status: number;
  data: ApiErrorResponse;

  constructor(status: number, data: ApiErrorResponse, safeMessage?: string) {
    super(safeMessage || safeApiErrorMessage(status));
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

interface RequestOptions extends RequestInit {
  params?: Record<string, string | number | boolean | undefined>;
  timeoutMs?: number;
  timeoutMessage?: string;
  auth?: boolean;
}

const BASE_URL = (import.meta.env?.VITE_API_BASE_URL || '').replace(/\/+$/, '');

export async function apiClient<T>(endpoint: string, options: RequestOptions = {}): Promise<T> {
  const {
    params,
    headers = {},
    signal: externalSignal,
    timeoutMs = 15_000,
    timeoutMessage,
    auth = true,
    ...customConfig
  } = options;

  if (externalSignal?.aborted) {
    throw new DOMException('The operation was aborted.', 'AbortError');
  }

  let url = `${BASE_URL}${endpoint}`;
  if (params) {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined) {
        searchParams.append(key, String(value));
      }
    });
    const queryString = searchParams.toString();
    if (queryString) {
      url += (url.includes('?') ? '&' : '?') + queryString;
    }
  }

  const requestHeaders: Record<string, string> = {
    ...(auth ? { 'Content-Type': 'application/json' } : {}),
    ...(headers as Record<string, string>),
  };

  if (auth) {
    const token = localStorage.getItem(TOKEN_STORAGE_KEY);
    if (token) {
      requestHeaders['Authorization'] = `Bearer ${token}`;
    }
  }

  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), timeoutMs);
  const abortFromCaller = () => controller.abort();
  externalSignal?.addEventListener('abort', abortFromCaller, { once: true });
  const config: RequestInit = {
    ...customConfig,
    headers: requestHeaders,
    signal: controller.signal,
    ...(auth ? {} : { credentials: 'omit' }),
  };

  try {
    const response = await fetch(url, config);

    // An unauthenticated login/register request can return 401 for credentials
    // without saying anything about a previously saved bearer token.
    if (response.status === 401 && auth) {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      window.dispatchEvent(new CustomEvent('auth:unauthorized'));
    }

    if (!response.ok) {
      let errorData: ApiErrorResponse = {};
      try {
        errorData = await response.json();
      } catch (error) {
        if (controller.signal.aborted || externalSignal?.aborted) throw error;
        errorData = {
          error: response.statusText,
          message: `HTTP error ${response.status}`,
        };
      }
      errorData.status = response.status;
      throw new ApiError(response.status, errorData);
    }

    if (response.status === 204) {
      return {} as T;
    }

    return await response.json();
  } catch (error) {
    if (error instanceof ApiError) throw error;
    if (controller.signal.aborted && !externalSignal?.aborted) {
      throw new ApiError(408, { error: 'Request timeout', message: timeoutMessage || 'The request took too long. Please try again.' }, timeoutMessage);
    }
    if (externalSignal?.aborted) throw error;
    throw new ApiError(0, { error: 'Network error', message: 'Unable to reach KodBTW. Check your connection and try again.' });
  } finally {
    window.clearTimeout(timeoutId);
    externalSignal?.removeEventListener('abort', abortFromCaller);
  }
}

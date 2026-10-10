import type { ApiErrorResponse } from '../../types/auth';

export const TOKEN_STORAGE_KEY = 'kodbtw_jwt';

export class ApiError extends Error {
  status: number;
  data: ApiErrorResponse;

  constructor(status: number, data: ApiErrorResponse) {
    super(data.message || data.error || 'An unexpected API error occurred');
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

interface RequestOptions extends RequestInit {
  params?: Record<string, string | number | boolean | undefined>;
  timeoutMs?: number;
  timeoutMessage?: string;
}

const BASE_URL = (import.meta.env?.VITE_API_BASE_URL || '').replace(/\/+$/, '');

export async function apiClient<T>(endpoint: string, options: RequestOptions = {}): Promise<T> {
  const { params, headers = {}, signal: externalSignal, timeoutMs = 15_000, timeoutMessage, ...customConfig } = options;

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

  const token = localStorage.getItem(TOKEN_STORAGE_KEY);
  const requestHeaders: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(headers as Record<string, string>),
  };

  if (token) {
    requestHeaders['Authorization'] = `Bearer ${token}`;
  }

  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), timeoutMs);
  const abortFromCaller = () => controller.abort();
  externalSignal?.addEventListener('abort', abortFromCaller, { once: true });
  const config: RequestInit = {
    ...customConfig,
    headers: requestHeaders,
    signal: controller.signal,
  };

  let response: Response;
  try {
    response = await fetch(url, config);
  } catch (error) {
    if (controller.signal.aborted && !externalSignal?.aborted) {
      throw new ApiError(408, { error: 'Request timeout', message: timeoutMessage || 'The request took too long. Please try again.' });
    }
    if (externalSignal?.aborted) throw error;
    throw new ApiError(0, { error: 'Network error', message: 'Unable to reach KodBTW. Check your connection and try again.' });
  } finally {
    window.clearTimeout(timeoutId);
    externalSignal?.removeEventListener('abort', abortFromCaller);
  }

  if (response.status === 401) {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    window.dispatchEvent(new CustomEvent('auth:unauthorized'));
  }

  if (!response.ok) {
    let errorData: ApiErrorResponse = {};
    try {
      errorData = await response.json();
    } catch {
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

  return response.json();
}

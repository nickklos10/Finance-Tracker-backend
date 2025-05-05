"use client";

import { useState, useCallback } from "react";
import { ApiError } from "@/lib/api";

interface UseApiOptions<T> {
  onSuccess?: (data: T) => void;
  onError?: (error: ApiError) => void;
}

interface UseApiState<T> {
  data: T | null;
  isLoading: boolean;
  error: ApiError | null;
}

export function useApi<T, P extends any[]>(
  apiFunction: (...args: P) => Promise<T>,
  options: UseApiOptions<T> = {}
) {
  const [state, setState] = useState<UseApiState<T>>({
    data: null,
    isLoading: false,
    error: null,
  });

  const execute = useCallback(
    async (...args: P) => {
      setState((prev) => ({ ...prev, isLoading: true, error: null }));

      try {
        const data = await apiFunction(...args);
        setState({ data, isLoading: false, error: null });
        options.onSuccess?.(data);
        return data;
      } catch (err) {
        const error =
          err instanceof ApiError
            ? err
            : new ApiError(
                err instanceof Error ? err.message : "Unknown error",
                500
              );

        setState({ data: null, isLoading: false, error });
        options.onError?.(error);
        throw error;
      }
    },
    [apiFunction, options]
  );

  const reset = useCallback(() => {
    setState({ data: null, isLoading: false, error: null });
  }, []);

  return {
    ...state,
    execute,
    reset,
  };
}

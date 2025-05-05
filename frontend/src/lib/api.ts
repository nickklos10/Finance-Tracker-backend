import { User } from "@/types/user";
import {
  Transaction,
  TransactionListResponse,
  TransactionType,
} from "@/types/transaction";
import { Category, CategoryListResponse } from "@/types/category";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "";

// Custom error class for API errors
export class ApiError extends Error {
  status: number;
  detail?: string;

  constructor(message: string, status: number, detail?: string) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.detail = detail;
  }
}

// Reusable fetch with error handling and token handling
async function apiRequest<T>(
  url: string,
  options: RequestInit = {}
): Promise<T> {
  // Set default headers
  const headers = new Headers(options.headers);

  if (
    !headers.has("Content-Type") &&
    options.method !== "GET" &&
    options.body
  ) {
    headers.set("Content-Type", "application/json");
  }

  const requestOptions: RequestInit = {
    ...options,
    headers,
    credentials: "include", // Include cookies for Auth0 session
  };

  try {
    const response = await fetch(`${API_URL}${url}`, requestOptions);

    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new ApiError(
        errorData?.message || response.statusText,
        response.status,
        errorData?.detail
      );
    }

    // Return null for 204 No Content responses
    if (response.status === 204) {
      return null as T;
    }

    return await response.json();
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }

    // Rethrow network errors or other issues
    throw new ApiError(
      error instanceof Error ? error.message : "Unknown error occurred",
      0
    );
  }
}

// User endpoints
export async function getCurrentUser(): Promise<User> {
  return apiRequest<User>("/api/users/me");
}

export async function updateCurrentUser(data: {
  name: string;
  email: string;
}): Promise<User> {
  return apiRequest<User>("/api/users/me", {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export async function deleteCurrentUser(): Promise<void> {
  return apiRequest<void>("/api/users/me", {
    method: "DELETE",
  });
}

// Transaction endpoints
export async function getTransactions(
  page = 0,
  size = 10
): Promise<TransactionListResponse> {
  return apiRequest<TransactionListResponse>(
    `/api/transactions?page=${page}&size=${size}`
  );
}

export async function getTransactionById(id: number): Promise<Transaction> {
  return apiRequest<Transaction>(`/api/transactions/${id}`);
}

export async function createTransaction(
  transaction: Transaction
): Promise<Transaction> {
  return apiRequest<Transaction>("/api/transactions", {
    method: "POST",
    body: JSON.stringify(transaction),
  });
}

export async function updateTransaction(
  id: number,
  transaction: Transaction
): Promise<Transaction> {
  return apiRequest<Transaction>(`/api/transactions/${id}`, {
    method: "PUT",
    body: JSON.stringify(transaction),
  });
}

export async function deleteTransaction(id: number): Promise<void> {
  return apiRequest<void>(`/api/transactions/${id}`, {
    method: "DELETE",
  });
}

export async function getTransactionsByType(
  type: TransactionType,
  page = 0,
  size = 10
): Promise<TransactionListResponse> {
  return apiRequest<TransactionListResponse>(
    `/api/transactions/type/${type}?page=${page}&size=${size}`
  );
}

export async function getTransactionsByDateRange(
  startDate: string,
  endDate: string,
  page = 0,
  size = 10
): Promise<TransactionListResponse> {
  return apiRequest<TransactionListResponse>(
    `/api/transactions/date-range?startDate=${startDate}&endDate=${endDate}&page=${page}&size=${size}`
  );
}

export async function getTransactionsByCategory(
  categoryId: number,
  page = 0,
  size = 10
): Promise<TransactionListResponse> {
  return apiRequest<TransactionListResponse>(
    `/api/transactions/category/${categoryId}?page=${page}&size=${size}`
  );
}

// Category endpoints
export async function getCategories(
  page = 0,
  size = 10
): Promise<CategoryListResponse> {
  return apiRequest<CategoryListResponse>(
    `/api/categories?page=${page}&size=${size}`
  );
}

export async function getCategoryById(id: number): Promise<Category> {
  return apiRequest<Category>(`/api/categories/${id}`);
}

export async function getCategoryByName(name: string): Promise<Category> {
  return apiRequest<Category>(`/api/categories/name/${name}`);
}

export async function createCategory(category: Category): Promise<Category> {
  return apiRequest<Category>("/api/categories", {
    method: "POST",
    body: JSON.stringify(category),
  });
}

export async function updateCategory(
  id: number,
  category: Category
): Promise<Category> {
  return apiRequest<Category>(`/api/categories/${id}`, {
    method: "PUT",
    body: JSON.stringify(category),
  });
}

export async function deleteCategory(id: number): Promise<void> {
  return apiRequest<void>(`/api/categories/${id}`, {
    method: "DELETE",
  });
}

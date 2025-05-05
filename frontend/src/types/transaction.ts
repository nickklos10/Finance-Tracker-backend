export enum TransactionType {
  INCOME = "INCOME",
  EXPENSE = "EXPENSE",
  TRANSFER = "TRANSFER",
}

export interface Transaction {
  id?: number;
  description: string;
  amount: number;
  date: string; // ISO format date string
  type: TransactionType;
  categoryId?: number;
  categoryName?: string;
  notes?: string;
}

export interface TransactionListResponse {
  content: Transaction[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

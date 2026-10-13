export type TransactionType = 'EXPENSE' | 'INCOME';
export type RecurrenceFrequency = 'DAILY' | 'WEEKLY' | 'MONTHLY';

export interface TransactionRequest {
  categoryId: string;
  amount: number;
  description: string;
  type: TransactionType;
  transactionDate: string; // yyyy-MM-dd
}

export interface TransactionResponse {
  id: string;
  categoryId: string;
  categoryName: string;
  amount: number;
  description: string;
  type: TransactionType;
  transactionDate: string;
}

export interface BudgetRequest {
  categoryId: string;
  month: string; // yyyy-MM
  monthlyLimit: number;
}

export interface BudgetStatusResponse {
  categoryId: string;
  categoryName: string;
  month: string;
  monthlyLimit: number;
  spent: number;
  remaining: number;
  percentUsed: number;
  overBudget: boolean;
}

export interface BudgetUpdateRequest {
  monthlyLimit: number;
}

export interface RecurringTransactionRequest {
  categoryId: string;
  amount: number;
  description: string;
  type: TransactionType;
  frequency: RecurrenceFrequency;
  startDate: string; // yyyy-MM-dd
}

export interface RecurringTransactionResponse {
  id: string;
  categoryId: string;
  categoryName: string;
  createdById?: string | null;
  amount: number;
  description: string;
  type: TransactionType;
  frequency: RecurrenceFrequency;
  nextRunDate: string;
  active: boolean;
}

export interface RecurringTransactionUpdateRequest {
  categoryId: string;
  amount: number;
  description: string;
  type: TransactionType;
  frequency: RecurrenceFrequency;
  nextRunDate: string;
  active: boolean;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

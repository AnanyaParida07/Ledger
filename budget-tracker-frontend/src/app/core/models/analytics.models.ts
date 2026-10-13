/** Raw response shapes returned by the analytics API. */
export interface CategorySpendResponse {
  categoryName: string;
  totalAmount: number;
}

export interface MonthlyTrendResponse {
  month: string;
  income: number;
  expense: number;
  net: number;
}

export interface SummaryResponse {
  month: string;
  totalIncome: number;
  totalExpense: number;
  net: number;
  topCategory: string | null;
  topCategorySpend: number;
}

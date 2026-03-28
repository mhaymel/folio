export interface ImportResult {
  success: boolean;
  imported: number;
  errors: string[];
}

export interface PaginatedResponse<T> {
  items: T[];
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
}

export interface TransactionPaginatedResponse extends PaginatedResponse<TransactionDto> {
  filteredCount: number;
  sumCount: number;
}

export interface TransactionDto {
  id: number;
  date: string;
  isin: string;
  name: string | null;
  depot: string;
  count: number;
  sharePrice: number;
}

export interface StockDto {
  isin: string;
  name: string | null;
  country: string | null;
  branch: string | null;
  totalShares: number;
  avgEntryPrice: number;
  currentQuote: number | null;
  performancePercent: number | null;
  dividendPerShare: number | null;
  estimatedAnnualIncome: number | null;
}

export interface StockFiltersDto {
  countries: string[];
  branches: string[];
}

export interface TransactionFiltersDto {
  depots: string[];
}

export interface DashboardDto {
  totalPortfolioValue: number;
  stockCount: number;
  totalDividendRatio: number;
  top5Holdings: HoldingDto[];
  top5DividendSources: DividendSourceDto[];
  lastQuoteFetchAt: string | null;
}

export interface HoldingDto {
  isin: string;
  name: string | null;
  investedAmount: number;
}

export interface DividendSourceDto {
  isin: string;
  name: string | null;
  estimatedAnnualIncome: number;
}

export interface DiversificationEntry {
  name: string;
  investedAmount: number;
  percentage: number;
}

export interface QuoteSettingsDto {
  enabled: boolean;
  intervalMinutes: number;
  lastFetchAt: string | null;
}

export interface Country {
  id: number;
  name: string;
}

export interface Branch {
  id: number;
  name: string;
}

export interface Depot {
  id: number;
  name: string;
}

export interface Currency {
  id: number;
  name: string;
}

export interface TickerSymbolDto {
  isin: string;
  tickerSymbol: string;
  name: string | null;
}

export interface IsinNameDto {
  isin: string;
  name: string;
}
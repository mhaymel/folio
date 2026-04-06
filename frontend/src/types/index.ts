export interface ImportResult {
  success: boolean;
  imported: number;
  durationMs: number;
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

export interface StockPaginatedResponse extends PaginatedResponse<StockDto> {
  sumCount: number;
}

export interface TransactionDto {
  id: number;
  date: string;
  isin: string;
  tickerSymbol: string | null;
  name: string | null;
  depot: string;
  count: number;
  sharePrice: number;
}

export interface StockDto {
  isin: string;
  tickerSymbol: string | null;
  name: string | null;
  country: string | null;
  branch: string | null;
  depot: string | null;
  count: number;
  avgEntryPrice: number;
  currentQuote: number | null;
  performancePercent: number | null;
  dividendPerShare: number | null;
  estimatedAnnualIncome: number | null;
}

export interface StockFiltersDto {
  countries: string[];
  branches: string[];
  depots: string[];
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

export interface IsinDto {
  isin: string;
  tickerSymbol: string | null;
  name: string | null;
  country: string | null;
  branch: string | null;
}

export interface IsinFiltersDto {
  countries: string[];
  branches: string[];
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

export interface DividendPaymentDto {
  id: number;
  timestamp: string;
  isin: string;
  name: string | null;
  depot: string;
  value: number;
}

export interface DividendPaymentPaginatedResponse extends PaginatedResponse<DividendPaymentDto> {
  sumValue: number;
}

export interface DividendPaymentFiltersDto {
  depots: string[];
}

export interface YahooIsinWithTickerItem {
  isin: string;
  tickerSymbol: string;
  name: string | null;
}

export interface YahooIsinWithoutTickerItem {
  isin: string;
  name: string | null;
}

export interface YahooIsinDuplicateTickerItem {
  isin: string;
  tickerSymbol: string;
  name: string | null;
}

export interface YahooIsinFetchResult {
  withTicker: YahooIsinWithTickerItem[];
  withoutTicker: YahooIsinWithoutTickerItem[];
  duplicateTickers: YahooIsinDuplicateTickerItem[];
}

export interface YahooIsinSaveResult {
  created: number;
  updated: number;
}

export interface YahooQuoteWithQuoteDto {
  isin: string;
  name: string | null;
  tickerSymbol: string | null;
  price: number;
  currency: string | null;
  provider: string;
  fetchedAt: string;
}

export interface YahooQuoteWithoutQuoteDto {
  isin: string;
  name: string | null;
  tickerSymbol: string | null;
}

export interface YahooFetchResultDto {
  total: number;
  fetched: number;
  noTicker: number;
  noQuote: number;
}
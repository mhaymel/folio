export interface ImportResult {
  success: boolean;
  imported: number;
  errors: string[];
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

export interface SecurityDto {
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

export interface DashboardDto {
  totalPortfolioValue: number;
  securityCount: number;
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

export interface DiversificationDto {
  entries: DiversificationEntry[];
  totalInvested: number;
}

export interface DiversificationEntry {
  name: string;
  investedAmount: number;
  percentage: number;
}

export interface QuoteSettingsDto {
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

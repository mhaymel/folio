import { useEffect, useState, useCallback } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import { Button } from '@dynatrace/strato-components/buttons';
import { Select } from '@dynatrace/strato-components/forms';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import api from '../api/client';
import type { StockDto, StockFiltersDto, PaginatedResponse } from '../types';
import ExportButtons from '../components/ExportButtons';
import PaginationControls from '../components/PaginationControls';

const fmtNum = (v: number | null) =>
  v != null ? v.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '\u2014';
const fmtPct = (v: number | null) =>
  v != null ? v.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' %' : '\u2014';

const columns = [
  { id: 'isin', header: 'ISIN', accessor: 'isin', sortType: 'text' as const, alignment: 'left' as const, width: 140, minWidth: 140 },
  { id: 'name', header: 'Name', accessor: 'name', sortType: 'text' as const, alignment: 'left' as const, width: 240, minWidth: 240 },
  { id: 'country', header: 'Country', accessor: 'country', sortType: 'text' as const, alignment: 'left' as const, width: 120, minWidth: 80 },
  { id: 'branch', header: 'Branch', accessor: 'branch', sortType: 'text' as const, alignment: 'left' as const, width: 160, minWidth: 80 },
  { id: 'totalShares', header: 'Total Shares', accessor: (r: StockDto) => fmtNum(r.totalShares), sortType: 'number' as const, alignment: 'right' as const, width: 100, minWidth: 80 },
  { id: 'avgEntryPrice', header: 'Avg Entry Price', accessor: (r: StockDto) => fmtNum(r.avgEntryPrice), sortType: 'number' as const, alignment: 'right' as const, width: 120, minWidth: 100 },
  { id: 'currentQuote', header: 'Current Quote', accessor: (r: StockDto) => fmtNum(r.currentQuote), sortType: 'number' as const, alignment: 'right' as const, width: 110, minWidth: 100 },
  { id: 'performancePercent', header: 'Performance (%)', accessor: (r: StockDto) => fmtPct(r.performancePercent), sortType: 'number' as const, alignment: 'right' as const, width: 120, minWidth: 100 },
  { id: 'dividendPerShare', header: 'Expected Dividend/Share', accessor: (r: StockDto) => fmtNum(r.dividendPerShare), sortType: 'number' as const, alignment: 'right' as const, width: 160, minWidth: 130 },
  { id: 'estimatedAnnualIncome', header: 'Est. Annual Income', accessor: (r: StockDto) => fmtNum(r.estimatedAnnualIncome), sortType: 'number' as const, alignment: 'right' as const, width: 140, minWidth: 110 },
];

export default function Stocks() {
  const [data, setData] = useState<StockDto[]>([]);
  const [totalItems, setTotalItems] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [sortField, setSortField] = useState('isin');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc');
  const [country, setCountry] = useState('');
  const [branch, setBranch] = useState('');
  const [filterOptions, setFilterOptions] = useState<StockFiltersDto>({ countries: [], branches: [] });

  const loadFilters = useCallback(async () => {
    try {
      const r = await api.get<StockFiltersDto>('/stocks/filters');
      setFilterOptions(r.data);
    } catch { /* ignore */ }
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const params: Record<string, string | number> = { sortField, sortDir, page, pageSize };
      if (country) params.country = country;
      if (branch) params.branch = branch;
      const r = await api.get<PaginatedResponse<StockDto>>('/stocks', { params });
      setData(r.data.items);
      setTotalItems(r.data.totalItems);
      setTotalPages(r.data.totalPages);
    } finally {
      setLoading(false);
    }
  }, [sortField, sortDir, page, pageSize, country, branch]);

  useEffect(() => { loadFilters(); }, [loadFilters]);
  useEffect(() => { load(); }, [load]);

  const handleShowAll = () => {
    if (pageSize === -1) { setPageSize(10); setPage(1); } else { setPageSize(-1); }
  };

  const exportParams: Record<string, string> = { sortField, sortDir };
  if (country) exportParams.country = country;
  if (branch) exportParams.branch = branch;

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Stocks</Heading>

      <Flex gap={16} alignItems="center">
        <Select value={country || '__all'} onChange={(v: string) => { setCountry(v === '__all' ? '' : v); setPage(1); }}>
          <Select.Content>
            <Select.Option value="__all">All countries</Select.Option>
            {filterOptions.countries.map(c => <Select.Option key={c} value={c}>{c}</Select.Option>)}
          </Select.Content>
        </Select>
        <Select value={branch || '__all'} onChange={(v: string) => { setBranch(v === '__all' ? '' : v); setPage(1); }}>
          <Select.Content>
            <Select.Option value="__all">All branches</Select.Option>
            {filterOptions.branches.map(b => <Select.Option key={b} value={b}>{b}</Select.Option>)}
          </Select.Content>
        </Select>
        <Button variant="default" onClick={() => { load(); loadFilters(); }}>Refresh</Button>
      </Flex>

      <Flex alignItems="center" justifyContent="space-between">
        <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
          {totalItems} stocks
        </Paragraph>
        <Flex gap={8} alignItems="center">
          <ExportButtons endpoint="/stocks/export" params={exportParams} />
          <Button variant="default" onClick={handleShowAll}>
            {pageSize === -1 ? 'Paginate' : 'Show All'}
          </Button>
        </Flex>
      </Flex>

      {loading ? (
        <Flex alignItems="center" gap={12}>
          <ProgressCircle aria-label="Loading stocks" size="small" />
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>Loading...</Paragraph>
        </Flex>
      ) : (
        <>
          <DataTable data={data} columns={columns} sortable={{ manualSort: true }} resizable fullWidth sortBy={[{ id: sortField, desc: sortDir === 'desc' }]}
            onSortByChange={(s: any) => { if (s?.[0]) { setSortField(s[0].id); setSortDir(s[0].desc ? 'desc' : 'asc'); } else { setSortDir(d => d === 'asc' ? 'desc' : 'asc'); } setPage(1); }}>
          </DataTable>
          {pageSize !== -1 && (
            <PaginationControls page={page} totalPages={totalPages} pageSize={pageSize}
              onPageChange={setPage} onPageSizeChange={(size) => { setPageSize(size); setPage(1); }} />
          )}
        </>
      )}
    </Flex>
  );
}
import { useEffect, useState, useCallback, useMemo } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import { Button } from '@dynatrace/strato-components/buttons';
import { Select } from '@dynatrace/strato-components/forms';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import api from '../api/client';
import type { StockDto, StockFiltersDto } from '../types';
import useServerTable from '../hooks/useServerTable';
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
  const [country, setCountry] = useState('');
  const [branch, setBranch] = useState('');
  const [filterOptions, setFilterOptions] = useState<StockFiltersDto>({ countries: [], branches: [] });

  const extraParams = useMemo(() => {
    const p: Record<string, string> = {};
    if (country) p.country = country;
    if (branch) p.branch = branch;
    return p;
  }, [country, branch]);

  const table = useServerTable<StockDto>({
    endpoint: '/stocks',
    defaultSortField: 'isin',
    extraParams,
  });

  const loadFilters = useCallback(async () => {
    try {
      const r = await api.get<StockFiltersDto>('/stocks/filters');
      setFilterOptions(r.data);
    } catch { /* ignore */ }
  }, []);

  useEffect(() => { loadFilters(); }, [loadFilters]);

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Stocks</Heading>

      <Flex gap={16} alignItems="center">
        <Select value={country || '__all'} onChange={(v: string) => { setCountry(v === '__all' ? '' : v); table.setPage(1); }}>
          <Select.Content>
            <Select.Option value="__all">All countries</Select.Option>
            {filterOptions.countries.map(c => <Select.Option key={c} value={c}>{c}</Select.Option>)}
          </Select.Content>
        </Select>
        <Select value={branch || '__all'} onChange={(v: string) => { setBranch(v === '__all' ? '' : v); table.setPage(1); }}>
          <Select.Content>
            <Select.Option value="__all">All branches</Select.Option>
            {filterOptions.branches.map(b => <Select.Option key={b} value={b}>{b}</Select.Option>)}
          </Select.Content>
        </Select>
        <Button variant="default" onClick={() => { table.reload(); loadFilters(); }}>Refresh</Button>
      </Flex>

      <Flex alignItems="center" justifyContent="space-between">
        <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
          {table.totalItems} stocks
        </Paragraph>
        <Flex gap={8} alignItems="center">
          <ExportButtons endpoint="/stocks/export" params={table.exportParams} />
          <Button variant="default" onClick={table.handleShowAll}>
            {table.pageSize === -1 ? 'Paginate' : 'Show All'}
          </Button>
        </Flex>
      </Flex>

      {table.loading ? (
        <Flex alignItems="center" gap={12}>
          <ProgressCircle aria-label="Loading stocks" size="small" />
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>Loading...</Paragraph>
        </Flex>
      ) : (
        <>
          <DataTable data={table.data} columns={columns} sortable={{ manualSort: true }} resizable fullWidth
            sortBy={table.sortBy} onSortByChange={table.handleSortChange} />
          {table.pageSize !== -1 && (
            <PaginationControls page={table.page} totalPages={table.totalPages} pageSize={table.pageSize}
              onPageChange={table.setPage} onPageSizeChange={table.handlePageSizeChange} />
          )}
        </>
      )}
    </Flex>
  );
}

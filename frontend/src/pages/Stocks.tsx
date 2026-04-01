import { useEffect, useState, useCallback, useMemo } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import { Button } from '@dynatrace/strato-components/buttons';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import api from '../api/client';
import type { StockDto, StockFiltersDto, StockPaginatedResponse } from '../types';
import useServerTable from '../hooks/useServerTable';
import ExportButtons from '../components/ExportButtons';
import PaginationControls from '../components/PaginationControls';
import MultiSelect from '../components/MultiSelect';
import LabeledInput from '../components/LabeledInput';
import IsinCell from '../components/IsinCell';

const STORAGE_KEY = 'stocks_filters';

function loadFiltersFromStorage(): { isin: string; name: string; countries: string[]; branches: string[] } {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    if (raw) return JSON.parse(raw);
  } catch { /* ignore */ }
  return { isin: '', name: '', countries: [], branches: [] };
}

function saveFiltersToStorage(state: { isin: string; name: string; countries: string[]; branches: string[] }) {
  try { sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state)); } catch { /* ignore */ }
}

const fmtNum = (v: number | null) =>
  v != null ? v.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '\u2014';
const fmtPct = (v: number | null) =>
  v != null ? v.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' %' : '\u2014';

export default function Stocks() {
  const saved = loadFiltersFromStorage();
  const [isinFilter, setIsinFilter] = useState(saved.isin);
  const [nameFilter, setNameFilter] = useState(saved.name);
  const [countryFilter, setCountryFilter] = useState<string[]>(saved.countries);
  const [branchFilter, setBranchFilter] = useState<string[]>(saved.branches);
  const [filterOptions, setFilterOptions] = useState<StockFiltersDto>({ countries: [], branches: [], depots: [] });
  const [sumCount, setSumCount] = useState(0);

  useEffect(() => {
    saveFiltersToStorage({ isin: isinFilter, name: nameFilter, countries: countryFilter, branches: branchFilter });
  }, [isinFilter, nameFilter, countryFilter, branchFilter]);

  const extraParams = useMemo(() => {
    const p: Record<string, string> = {};
    if (isinFilter) p.isin = isinFilter;
    if (nameFilter) p.name = nameFilter;
    if (countryFilter.length > 0) p.country = countryFilter.join(',');
    if (branchFilter.length > 0) p.branch = branchFilter.join(',');
    return p;
  }, [isinFilter, nameFilter, countryFilter, branchFilter]);

  const table = useServerTable<StockDto, StockPaginatedResponse>({
    endpoint: '/stocks',
    defaultSortField: 'isin',
    extraParams,
    responseMapper: (resp) => {
      setSumCount(resp.sumCount);
    },
  });

  const loadFilters = useCallback(async () => {
    try {
      const r = await api.get<StockFiltersDto>('/stocks/filters');
      setFilterOptions(r.data);
    } catch { /* ignore */ }
  }, []);

  useEffect(() => { loadFilters(); }, [loadFilters]);

  const handleCellDoubleClick = (field: string, value: string) => {
    if (field === 'isin') { setIsinFilter(value); table.setPage(1); }
    else if (field === 'name') { setNameFilter(value); table.setPage(1); }
  };

  const columns = [
    {
      id: 'isin', header: 'ISIN', accessor: 'isin', sortType: 'text' as const, alignment: 'left' as const, width: 140, minWidth: 140,
      cell: ({ rowData }: { rowData: StockDto }) => (
        <IsinCell isin={rowData.isin} onFilter={(v) => { setIsinFilter(v); table.setPage(1); }} activeFilter={isinFilter} onDoubleClick={() => handleCellDoubleClick('isin', rowData.isin)} />
      ),
    },
    {
      id: 'name', header: 'Name', accessor: (r: StockDto) => r.name ?? '', sortType: 'text' as const, alignment: 'left' as const, width: 240, minWidth: 240,
      cell: ({ rowData }: { rowData: StockDto }) => (
        <span onDoubleClick={() => rowData.name && handleCellDoubleClick('name', rowData.name)} style={{ paddingLeft: 10, display: 'flex', alignItems: 'center', height: '100%', cursor: 'pointer' }}>{rowData.name ?? ''}</span>
      ),
    },
    { id: 'count', header: 'Count', accessor: (r: StockDto) => fmtNum(r.count), sortType: 'number' as const, alignment: 'right' as const, width: 100, minWidth: 80 },
    { id: 'avgEntryPrice', header: 'Avg Entry Price', accessor: (r: StockDto) => fmtNum(r.avgEntryPrice), sortType: 'number' as const, alignment: 'right' as const, width: 120, minWidth: 100 },
    { id: 'currentQuote', header: 'Current Quote', accessor: (r: StockDto) => fmtNum(r.currentQuote), sortType: 'number' as const, alignment: 'right' as const, width: 110, minWidth: 100 },
    { id: 'country', header: 'Country', accessor: 'country', sortType: 'text' as const, alignment: 'left' as const, width: 120, minWidth: 80 },
    { id: 'branch', header: 'Branch', accessor: 'branch', sortType: 'text' as const, alignment: 'left' as const, width: 160, minWidth: 80 },
    { id: 'performancePercent', header: 'Performance (%)', accessor: (r: StockDto) => fmtPct(r.performancePercent), sortType: 'number' as const, alignment: 'right' as const, width: 120, minWidth: 100 },
    { id: 'dividendPerShare', header: 'Expected Dividend/Share', accessor: (r: StockDto) => fmtNum(r.dividendPerShare), sortType: 'number' as const, alignment: 'right' as const, width: 160, minWidth: 130 },
    { id: 'estimatedAnnualIncome', header: 'Est. Annual Income', accessor: (r: StockDto) => fmtNum(r.estimatedAnnualIncome), sortType: 'number' as const, alignment: 'right' as const, width: 140, minWidth: 110 },
  ];

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Stocks</Heading>

      <Flex gap={16} alignItems="flex-end" flexWrap="wrap">
        <LabeledInput label="ISIN" value={isinFilter}
          onChange={(v: string) => { setIsinFilter(v); table.setPage(1); }} />
        <LabeledInput label="Name" value={nameFilter}
          onChange={(v: string) => { setNameFilter(v); table.setPage(1); }} />
        <MultiSelect options={filterOptions.countries} selected={countryFilter}
          onChange={(v) => { setCountryFilter(v); table.setPage(1); }} label="Country" />
        <MultiSelect options={filterOptions.branches} selected={branchFilter}
          onChange={(v) => { setBranchFilter(v); table.setPage(1); }} label="Branch" />
        <Button variant="emphasized" onClick={() => { setIsinFilter(''); setNameFilter(''); setCountryFilter([]); setBranchFilter([]); table.setPage(1); }}>Clear</Button>
        <Button variant="emphasized" onClick={() => { table.reload(); loadFilters(); }}>Refresh</Button>
      </Flex>

      <Flex alignItems="center" justifyContent="space-between">
        <Flex gap={16} alignItems="center">
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
            {table.totalItems} stocks
          </Paragraph>
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
            Sum: {fmtNum(sumCount)}
          </Paragraph>
        </Flex>
        <Flex gap={8} alignItems="center">
          <ExportButtons endpoint="/stocks/export" params={table.exportParams} />
          <Button variant="emphasized" onClick={table.handleShowAll}>
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

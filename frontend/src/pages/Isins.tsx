import { useEffect, useState, useCallback, useMemo } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import { Button } from '@dynatrace/strato-components/buttons';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import api from '../api/client';
import type { IsinDto, IsinFiltersDto, PaginatedResponse } from '../types';
import useServerTable from '../hooks/useServerTable';
import useDebounce from '../hooks/useDebounce';
import ExportButtons from '../components/ExportButtons';
import PaginationControls from '../components/PaginationControls';
import MultiSelect from '../components/MultiSelect';
import LabeledInput from '../components/LabeledInput';
import IsinCell from '../components/IsinCell';

const STORAGE_KEY = 'isins_filters';

function loadFiltersFromStorage(): { isin: string; tickerSymbol: string; name: string; countries: string[]; branches: string[] } {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    if (raw) return JSON.parse(raw);
  } catch { /* ignore */ }
  return { isin: '', tickerSymbol: '', name: '', countries: [], branches: [] };
}

function saveFiltersToStorage(state: { isin: string; tickerSymbol: string; name: string; countries: string[]; branches: string[] }) {
  try { sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state)); } catch { /* ignore */ }
}

export default function Isins() {
  const saved = loadFiltersFromStorage();
  const [isinFilter, setIsinFilter] = useState(saved.isin);
  const [tickerFilter, setTickerFilter] = useState(saved.tickerSymbol);
  const [nameFilter, setNameFilter] = useState(saved.name);
  const [countryFilter, setCountryFilter] = useState<string[]>(saved.countries);
  const [branchFilter, setBranchFilter] = useState<string[]>(saved.branches);
  const [filterOptions, setFilterOptions] = useState<IsinFiltersDto>({ countries: [], branches: [] });

  useEffect(() => {
    saveFiltersToStorage({ isin: isinFilter, tickerSymbol: tickerFilter, name: nameFilter, countries: countryFilter, branches: branchFilter });
  }, [isinFilter, tickerFilter, nameFilter, countryFilter, branchFilter]);

  const debouncedIsin = useDebounce(isinFilter, 300);
  const debouncedTicker = useDebounce(tickerFilter, 300);
  const debouncedName = useDebounce(nameFilter, 300);

  const extraParams = useMemo(() => {
    const p: Record<string, string> = {};
    if (debouncedIsin) p.isin = debouncedIsin;
    if (debouncedTicker) p.tickerSymbol = debouncedTicker;
    if (debouncedName) p.name = debouncedName;
    if (countryFilter.length > 0) p.country = countryFilter.join(',');
    if (branchFilter.length > 0) p.branch = branchFilter.join(',');
    return p;
  }, [debouncedIsin, debouncedTicker, debouncedName, countryFilter, branchFilter]);

  const table = useServerTable<IsinDto, PaginatedResponse<IsinDto>>({
    endpoint: '/isins',
    defaultSortField: 'isin',
    extraParams,
  });

  const loadFilters = useCallback(async () => {
    try {
      const r = await api.get<IsinFiltersDto>('/isins/filters');
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
      cell: ({ rowData }: { rowData: IsinDto }) => (
        <IsinCell isin={rowData.isin} onFilter={(v) => { setIsinFilter(v); table.setPage(1); }} activeFilter={isinFilter} onDoubleClick={() => handleCellDoubleClick('isin', rowData.isin)} />
      ),
    },
    {
      id: 'tickerSymbol', header: 'Ticker Symbol', accessor: (r: IsinDto) => r.tickerSymbol ?? '', sortType: 'text' as const, alignment: 'left' as const, width: 100, minWidth: 80,
    },
    {
      id: 'name', header: 'Name', accessor: (r: IsinDto) => r.name ?? '', sortType: 'text' as const, alignment: 'left' as const, width: 240, minWidth: 120,
      cell: ({ rowData }: { rowData: IsinDto }) => (
        <span onDoubleClick={() => rowData.name && handleCellDoubleClick('name', rowData.name)} style={{ paddingLeft: 10, display: 'flex', alignItems: 'center', height: '100%', cursor: 'pointer' }}>{rowData.name ?? ''}</span>
      ),
    },
    { id: 'country', header: 'Country', accessor: (r: IsinDto) => r.country ?? '', sortType: 'text' as const, alignment: 'left' as const, width: 120, minWidth: 80 },
    { id: 'branch', header: 'Branch', accessor: (r: IsinDto) => r.branch ?? '', sortType: 'text' as const, alignment: 'left' as const, width: 160, minWidth: 80 },
  ];

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>ISINs</Heading>

      <Flex gap={16} alignItems="flex-end" flexWrap="wrap">
        <LabeledInput label="ISIN" value={isinFilter}
          onChange={(v: string) => { setIsinFilter(v); table.setPage(1); }} />
        <LabeledInput label="Ticker Symbol" value={tickerFilter}
          onChange={(v: string) => { setTickerFilter(v); table.setPage(1); }} />
        <LabeledInput label="Name" value={nameFilter}
          onChange={(v: string) => { setNameFilter(v); table.setPage(1); }} />
        <MultiSelect options={filterOptions.countries} selected={countryFilter}
          onChange={(v) => { setCountryFilter(v); table.setPage(1); }} label="Country" />
        <MultiSelect options={filterOptions.branches} selected={branchFilter}
          onChange={(v) => { setBranchFilter(v); table.setPage(1); }} label="Branch" />
        <Button variant="emphasized" onClick={() => { setIsinFilter(''); setTickerFilter(''); setNameFilter(''); setCountryFilter([]); setBranchFilter([]); table.setPage(1); }}>Clear</Button>
        <Button variant="emphasized" onClick={() => { table.reload(); loadFilters(); }}>Refresh</Button>
      </Flex>

      <Flex alignItems="center" justifyContent="space-between">
        <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
          {table.totalItems} ISINs
        </Paragraph>
        <Flex gap={8} alignItems="center">
          <ExportButtons endpoint="/isins/export" params={table.exportParams} />
          <Button variant="emphasized" onClick={table.handleShowAll}>
            {table.pageSize === -1 ? 'Paginate' : 'Show All'}
          </Button>
        </Flex>
      </Flex>

      {table.loading ? (
        <Flex alignItems="center" gap={12}>
          <ProgressCircle aria-label="Loading ISINs" size="small" />
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

import { useEffect, useState, useCallback, useMemo } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import { Button } from '@dynatrace/strato-components/buttons';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import { TimeframeSelector } from '@dynatrace/strato-components/filters';
import type { Timeframe } from '@dynatrace/strato-components/core';
import api from '../api/client';
import type { DividendPaymentDto, DividendPaymentPaginatedResponse, DividendPaymentFiltersDto } from '../types';
import useServerTable from '../hooks/useServerTable';
import ExportButtons from '../components/ExportButtons';
import PaginationControls from '../components/PaginationControls';
import MultiSelect from '../components/MultiSelect';
import LabeledInput from '../components/LabeledInput';
import IsinCell from '../components/IsinCell';

const STORAGE_KEY = 'dividend_payments_filters';

const DEFAULT_FROM = '2026-01-01T00:00:00.000Z';
const DEFAULT_TO = '2026-12-31T23:59:59.999Z';

const YEAR_PRESETS = [
  { label: 'All', from: '2000-01-01T00:00:00.000Z', to: new Date().toISOString() },
  { label: '2026', from: '2026-01-01T00:00:00.000Z', to: '2026-12-31T23:59:59.999Z' },
  { label: '2025', from: '2025-01-01T00:00:00.000Z', to: '2025-12-31T23:59:59.999Z' },
  { label: '2024', from: '2024-01-01T00:00:00.000Z', to: '2024-12-31T23:59:59.999Z' },
  { label: '2023', from: '2023-01-01T00:00:00.000Z', to: '2023-12-31T23:59:59.999Z' },
  { label: '2022', from: '2022-01-01T00:00:00.000Z', to: '2022-12-31T23:59:59.999Z' },
  { label: '2021', from: '2021-01-01T00:00:00.000Z', to: '2021-12-31T23:59:59.999Z' },
  { label: '2020', from: '2020-01-01T00:00:00.000Z', to: '2020-12-31T23:59:59.999Z' },
  { label: '2019', from: '2019-01-01T00:00:00.000Z', to: '2019-12-31T23:59:59.999Z' },
  { label: '2018', from: '2018-01-01T00:00:00.000Z', to: '2018-12-31T23:59:59.999Z' },
  { label: '2017', from: '2017-01-01T00:00:00.000Z', to: '2017-12-31T23:59:59.999Z' },
  { label: '2016', from: '2016-01-01T00:00:00.000Z', to: '2016-12-31T23:59:59.999Z' },
  { label: '2015', from: '2015-01-01T00:00:00.000Z', to: '2015-12-31T23:59:59.999Z' },
];

interface SavedFilters {
  isin: string;
  name: string;
  depots: string[];
  fromDate: string;
  toDate: string;
}

function loadFiltersFromStorage(): SavedFilters {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    if (raw) return JSON.parse(raw);
  } catch { /* ignore */ }
  return { isin: '', name: '', depots: [], fromDate: DEFAULT_FROM, toDate: DEFAULT_TO };
}

function saveFiltersToStorage(state: SavedFilters) {
  try { sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state)); } catch { /* ignore */ }
}

function toDateOnly(iso: string): string {
  if (!iso) return '';
  return iso.slice(0, 10);
}

const fmtNum = (v: number | null) =>
  v != null ? v.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '—';

export default function DividendPayments() {
  const saved = loadFiltersFromStorage();
  const [sumValue, setSumValue] = useState(0);
  const [isinFilter, setIsinFilter] = useState(saved.isin);
  const [nameFilter, setNameFilter] = useState(saved.name);
  const [depotFilter, setDepotFilter] = useState<string[]>(saved.depots);
  const [fromDate, setFromDate] = useState(saved.fromDate);
  const [toDate, setToDate] = useState(saved.toDate);
  const [depotOptions, setDepotOptions] = useState<string[]>([]);

  const timeframeValue = useMemo(() => {
    if (!fromDate && !toDate) return null;
    const result: { from?: string; to?: string } = {};
    if (fromDate) result.from = fromDate;
    if (toDate) result.to = toDate;
    return result;
  }, [fromDate, toDate]);

  const handleTimeframeChange = useCallback((value: Timeframe | null) => {
    if (value) {
      setFromDate(value.from.absoluteDate);
      setToDate(value.to.absoluteDate);
    } else {
      setFromDate('');
      setToDate('');
    }
  }, []);

  useEffect(() => {
    saveFiltersToStorage({ isin: isinFilter, name: nameFilter, depots: depotFilter, fromDate, toDate });
  }, [isinFilter, nameFilter, depotFilter, fromDate, toDate]);

  const extraParams = useMemo(() => {
    const p: Record<string, string> = {};
    if (isinFilter) p.isin = isinFilter;
    if (nameFilter) p.name = nameFilter;
    if (depotFilter.length > 0) p.depot = depotFilter.join(',');
    if (fromDate) p.fromDate = toDateOnly(fromDate);
    if (toDate) p.toDate = toDateOnly(toDate);
    return p;
  }, [isinFilter, nameFilter, depotFilter, fromDate, toDate]);

  const table = useServerTable<DividendPaymentDto, DividendPaymentPaginatedResponse>({
    endpoint: '/dividend-payments',
    defaultSortField: 'timestamp',
    defaultSortDir: 'desc',
    extraParams,
    responseMapper: (resp) => {
      setSumValue(resp.sumValue);
    },
  });

  const loadFilters = useCallback(async () => {
    try {
      const r = await api.get<DividendPaymentFiltersDto>('/dividend-payments/filters');
      setDepotOptions(r.data.depots);
    } catch { /* ignore */ }
  }, []);

  useEffect(() => { loadFilters(); }, [loadFilters]);

  const handleCellDoubleClick = (field: string, value: string) => {
    if (field === 'isin') { setIsinFilter(value); table.setPage(1); }
    else if (field === 'name') { setNameFilter(value); table.setPage(1); }
    else if (field === 'depot') { setDepotFilter([value]); table.setPage(1); }
  };

  const columns = [
    { id: 'timestamp', header: 'Date', accessor: 'timestamp', sortType: 'text' as const, alignment: 'center' as const, width: 110, minWidth: 110 },
    {
      id: 'isin', header: 'ISIN', accessor: 'isin', sortType: 'text' as const, alignment: 'left' as const, width: 140, minWidth: 140,
      cell: ({ rowData }: { rowData: DividendPaymentDto }) => (
        <IsinCell isin={rowData.isin} onFilter={(v) => { setIsinFilter(v); table.setPage(1); }} activeFilter={isinFilter} onDoubleClick={() => handleCellDoubleClick('isin', rowData.isin)} />
      ),
    },
    {
      id: 'name', header: 'Name', accessor: (row: DividendPaymentDto) => row.name ?? '—', sortType: 'text' as const, alignment: 'left' as const, width: 240, minWidth: 120,
      cell: ({ rowData }: { rowData: DividendPaymentDto }) => (
        <span onDoubleClick={() => rowData.name && handleCellDoubleClick('name', rowData.name)} style={{ paddingLeft: 10, display: 'flex', alignItems: 'center', height: '100%', cursor: 'pointer' }}>{rowData.name ?? '—'}</span>
      ),
    },
    {
      id: 'depot', header: 'Depot', accessor: 'depot', sortType: 'text' as const, alignment: 'left' as const, width: 100, minWidth: 80,
      cell: ({ rowData }: { rowData: DividendPaymentDto }) => (
        <span onDoubleClick={() => handleCellDoubleClick('depot', rowData.depot)} style={{ paddingLeft: 10, display: 'flex', alignItems: 'center', height: '100%', cursor: 'pointer' }}>{rowData.depot}</span>
      ),
    },
    { id: 'value', header: 'Amount (EUR)', accessor: (row: DividendPaymentDto) => fmtNum(row.value), sortType: 'number' as const, alignment: 'right' as const, width: 110, minWidth: 100 },
  ];

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Dividend Payments</Heading>

      <Flex gap={16} alignItems="flex-end" flexWrap="wrap">
        <LabeledInput label="ISIN" value={isinFilter}
          onChange={(v: string) => { setIsinFilter(v); table.setPage(1); }} />
        <LabeledInput label="Name" value={nameFilter}
          onChange={(v: string) => { setNameFilter(v); table.setPage(1); }} />
        <MultiSelect options={depotOptions} selected={depotFilter}
          onChange={(v) => { setDepotFilter(v); table.setPage(1); }} label="Depot" />
        <TimeframeSelector value={timeframeValue} onChange={handleTimeframeChange} clearable>
          <TimeframeSelector.Presets>
            {YEAR_PRESETS.map(p => (
              <TimeframeSelector.PresetItem key={p.label} value={{ from: p.from, to: p.to }}>
                {p.label}
              </TimeframeSelector.PresetItem>
            ))}
          </TimeframeSelector.Presets>
        </TimeframeSelector>
        <Button variant="emphasized" onClick={() => { setIsinFilter(''); setNameFilter(''); setDepotFilter([]); setFromDate(''); setToDate(''); table.setPage(1); }}>Clear</Button>
        <Button variant="emphasized" onClick={() => { table.reload(); loadFilters(); }}>Refresh</Button>
      </Flex>

      <Flex alignItems="center" justifyContent="space-between">
        <Flex gap={16} alignItems="center">
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
            {table.totalItems} dividend payments
          </Paragraph>
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
            Total: {fmtNum(sumValue)} EUR
          </Paragraph>
        </Flex>
        <Flex gap={8} alignItems="center">
          <ExportButtons endpoint="/dividend-payments/export" params={table.exportParams} />
          <Button variant="emphasized" onClick={table.handleShowAll}>
            {table.pageSize === -1 ? 'Paginate' : 'Show All'}
          </Button>
        </Flex>
      </Flex>

      {table.loading ? (
        <Flex alignItems="center" gap={12}>
          <ProgressCircle aria-label="Loading dividend payments" size="small" />
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
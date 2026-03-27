import { useEffect, useMemo, useState } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable, DataTablePagination } from '@dynatrace/strato-components/tables';
import { Select } from '@dynatrace/strato-components/forms';
import { Button } from '@dynatrace/strato-components/buttons';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import api from '../api/client';
import type { StockDto } from '../types';
import ExportButtons from '../components/ExportButtons';

const fmt = (n: number | null) =>
  n != null ? n.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '—';

export default function Stocks() {
  const [allStocks, setAllStocks] = useState<StockDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [countryFilter, setCountryFilter] = useState('');
  const [branchFilter, setBranchFilter] = useState('');
  const [showAll, setShowAll] = useState(false);
  const [sortField, setSortField] = useState('isin');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc');

  const load = async () => {
    setLoading(true);
    try {
      const r = await api.get<StockDto[]>('/stocks');
      setAllStocks(r.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const countries = useMemo(
    () => [...new Set(allStocks.map(s => s.country).filter(Boolean) as string[])].sort(),
    [allStocks]
  );

  const branches = useMemo(
    () => [...new Set(allStocks.map(s => s.branch).filter(Boolean) as string[])].sort(),
    [allStocks]
  );

  const filtered = useMemo(() => allStocks.filter(s => {
    const matchesCountry = !countryFilter || s.country === countryFilter;
    const matchesBranch = !branchFilter || s.branch === branchFilter;
    return matchesCountry && matchesBranch;
  }), [allStocks, countryFilter, branchFilter]);

  const columns = useMemo(() => [
    { id: 'isin', header: 'ISIN', accessor: 'isin', sortType: 'text' as const, width: 140, minWidth: 140 },
    { id: 'name', header: 'Name', accessor: 'name', sortType: 'text' as const, width: 240, minWidth: 240 },
    { id: 'country', header: 'Country', accessor: 'country', sortType: 'text' as const, width: 120, minWidth: 80 },
    { id: 'branch', header: 'Branch', accessor: 'branch', sortType: 'text' as const, width: 160, minWidth: 80 },
    { id: 'totalShares', header: 'Total Shares', accessor: (r: StockDto) => fmt(r.totalShares), sortType: 'number' as const, alignment: 'right' as const, minWidth: 80 },
    { id: 'avgEntryPrice', header: 'Avg Entry Price', accessor: (r: StockDto) => fmt(r.avgEntryPrice), sortType: 'number' as const, alignment: 'right' as const, minWidth: 90 },
    { id: 'currentQuote', header: 'Current Quote', accessor: (r: StockDto) => fmt(r.currentQuote), sortType: 'number' as const, alignment: 'right' as const, minWidth: 80 },
    { id: 'performancePercent', header: 'Performance (%)', accessor: (r: StockDto) => fmt(r.performancePercent), sortType: 'number' as const, alignment: 'right' as const, minWidth: 80 },
    { id: 'dividendPerShare', header: 'Expected Dividend/Share', accessor: (r: StockDto) => fmt(r.dividendPerShare), sortType: 'number' as const, alignment: 'right' as const, minWidth: 90 },
    { id: 'estimatedAnnualIncome', header: 'Est. Annual Income', accessor: (r: StockDto) => fmt(r.estimatedAnnualIncome), sortType: 'number' as const, alignment: 'right' as const, minWidth: 90 },
  ], []);

  const exportParams = useMemo(() => ({
    country: countryFilter || undefined,
    branch: branchFilter || undefined,
    sortField,
    sortDir,
  }), [countryFilter, branchFilter, sortField, sortDir]);

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Stocks</Heading>

      <Flex gap={8} alignItems="center">
        <Select<string> value={countryFilter || null} onChange={val => setCountryFilter(val ?? '')}>
          <Select.Content>
            <Select.Option value="">All countries</Select.Option>
            {countries.map(c => (
              <Select.Option key={c} value={c}>{c}</Select.Option>
            ))}
          </Select.Content>
        </Select>
        <Select<string> value={branchFilter || null} onChange={val => setBranchFilter(val ?? '')}>
          <Select.Content>
            <Select.Option value="">All branches</Select.Option>
            {branches.map(b => (
              <Select.Option key={b} value={b}>{b}</Select.Option>
            ))}
          </Select.Content>
        </Select>
        <Button onClick={load} variant="default">Refresh</Button>
      </Flex>

      {loading ? (
        <Flex alignItems="center" gap={12}>
          <ProgressCircle aria-label="Loading stocks" size="small" />
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>Loading...</Paragraph>
        </Flex>
      ) : (
        <>
          <Flex alignItems="center" justifyContent="space-between">
            <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
              {filtered.length !== allStocks.length
                ? `${filtered.length} of ${allStocks.length} stocks`
                : `${allStocks.length} stocks`}
            </Paragraph>
            <Flex gap={8} alignItems="center">
              <ExportButtons endpoint="/stocks/export" params={exportParams} />
              <Button variant="default" onClick={() => setShowAll(s => !s)}>
                {showAll ? 'Paginate' : 'Show All'}
              </Button>
            </Flex>
          </Flex>
          <DataTable data={filtered} columns={columns} sortable resizable fullWidth
            defaultSortBy={[{ id: 'isin', desc: false }]}
            onSortByChange={(s: any) => { if (s?.[0]) { setSortField(s[0].id); setSortDir(s[0].desc ? 'desc' : 'asc'); } }}>
            {!showAll && (
              <DataTablePagination defaultPageSize={10} pageSizeOptions={[10, 20, 50, 100]} />
            )}
          </DataTable>
        </>
      )}
    </Flex>
  );
}

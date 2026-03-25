import { useEffect, useMemo, useState } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable, DataTablePagination } from '@dynatrace/strato-components/tables';
import { Select } from '@dynatrace/strato-components/forms';
import { Button } from '@dynatrace/strato-components/buttons';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import api from '../api/client';
import type { SecurityDto } from '../types';

const fmt = (n: number | null) =>
  n != null ? n.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '—';

export default function Securities() {
  const [allSecurities, setAllSecurities] = useState<SecurityDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [countryFilter, setCountryFilter] = useState('');
  const [branchFilter, setBranchFilter] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const r = await api.get<SecurityDto[]>('/securities');
      setAllSecurities(r.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const countries = useMemo(
    () => [...new Set(allSecurities.map(s => s.country).filter(Boolean) as string[])].sort(),
    [allSecurities]
  );

  const branches = useMemo(
    () => [...new Set(allSecurities.map(s => s.branch).filter(Boolean) as string[])].sort(),
    [allSecurities]
  );

  const filtered = useMemo(() => allSecurities.filter(s => {
    const matchesCountry = !countryFilter || s.country === countryFilter;
    const matchesBranch = !branchFilter || s.branch === branchFilter;
    return matchesCountry && matchesBranch;
  }), [allSecurities, countryFilter, branchFilter]);

  const columns = useMemo(() => [
    { id: 'isin', header: 'ISIN', accessor: 'isin', sortType: 'text' as const, width: 140, minWidth: 140 },
    { id: 'name', header: 'Name', accessor: 'name', sortType: 'text' as const, width: 240, minWidth: 240 },
    { id: 'country', header: 'Country', accessor: 'country', sortType: 'text' as const, width: 120, minWidth: 80 },
    { id: 'branch', header: 'Branch', accessor: 'branch', sortType: 'text' as const, width: 160, minWidth: 80 },
    { id: 'totalShares', header: 'Shares', accessor: (r: SecurityDto) => fmt(r.totalShares), sortType: 'number' as const, alignment: 'right' as const, minWidth: 80 },
    { id: 'avgEntryPrice', header: 'Avg Price', accessor: (r: SecurityDto) => fmt(r.avgEntryPrice), sortType: 'number' as const, alignment: 'right' as const, minWidth: 90 },
    { id: 'currentQuote', header: 'Quote', accessor: (r: SecurityDto) => fmt(r.currentQuote), sortType: 'number' as const, alignment: 'right' as const, minWidth: 80 },
    { id: 'performancePercent', header: 'Perf %', accessor: (r: SecurityDto) => fmt(r.performancePercent), sortType: 'number' as const, alignment: 'right' as const, minWidth: 80 },
    { id: 'dividendPerShare', header: 'Div/Share', accessor: (r: SecurityDto) => fmt(r.dividendPerShare), sortType: 'number' as const, alignment: 'right' as const, minWidth: 90 },
    { id: 'estimatedAnnualIncome', header: 'Est. Income', accessor: (r: SecurityDto) => fmt(r.estimatedAnnualIncome), sortType: 'number' as const, alignment: 'right' as const, minWidth: 90 },
  ], []);

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Securities</Heading>

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
          <ProgressCircle aria-label="Loading securities" size="small" />
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>Loading...</Paragraph>
        </Flex>
      ) : (
        <>
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
            {filtered.length !== allSecurities.length
              ? `${filtered.length} of ${allSecurities.length} securities`
              : `${allSecurities.length} securities`}
          </Paragraph>
          <DataTable data={filtered} columns={columns} sortable resizable fullWidth>
            <DataTablePagination defaultPageSize={10} pageSizeOptions={[10, 20, 50, 100]} />
          </DataTable>
        </>
      )}
    </Flex>
  );
}
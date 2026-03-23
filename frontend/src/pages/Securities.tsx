import { useEffect, useMemo, useState } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import api from '../api/client';
import type { SecurityDto } from '../types';

const fmt = (n: number | null) =>
  n != null ? n.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '—';

const columns = [
  { id: 'isin', header: 'ISIN', accessor: 'isin', sortType: 'text' as const },
  { id: 'name', header: 'Name', accessor: 'name', sortType: 'text' as const },
  { id: 'country', header: 'Country', accessor: 'country', sortType: 'text' as const },
  { id: 'branch', header: 'Branch', accessor: 'branch', sortType: 'text' as const },
  { id: 'totalShares', header: 'Shares', accessor: (r: SecurityDto) => fmt(r.totalShares), sortType: 'number' as const, alignment: 'right' as const },
  { id: 'avgEntryPrice', header: 'Avg Price', accessor: (r: SecurityDto) => fmt(r.avgEntryPrice), sortType: 'number' as const, alignment: 'right' as const },
  { id: 'currentQuote', header: 'Quote', accessor: (r: SecurityDto) => fmt(r.currentQuote), sortType: 'number' as const, alignment: 'right' as const },
  { id: 'performancePercent', header: 'Perf %', accessor: (r: SecurityDto) => fmt(r.performancePercent), sortType: 'number' as const, alignment: 'right' as const },
  { id: 'dividendPerShare', header: 'Div/Share', accessor: (r: SecurityDto) => fmt(r.dividendPerShare), sortType: 'number' as const, alignment: 'right' as const },
  { id: 'estimatedAnnualIncome', header: 'Est. Income', accessor: (r: SecurityDto) => fmt(r.estimatedAnnualIncome), sortType: 'number' as const, alignment: 'right' as const },
];

export default function Securities() {
  const [securities, setSecurities] = useState<SecurityDto[]>([]);

  useEffect(() => {
    api.get<SecurityDto[]>('/securities').then(r => setSecurities(r.data));
  }, []);

  const data = useMemo(() => securities, [securities]);

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Securities ({securities.length})</Heading>
      <DataTable data={data} columns={columns} sortable fullWidth />
    </Flex>
  );
}
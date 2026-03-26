import { useEffect, useMemo, useState } from 'react';
import { Flex, Surface } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import api from '../api/client';
import type { DashboardDto } from '../types';
import ExportButtons from '../components/ExportButtons';

const fmt = (n: number) =>
  n.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

const holdingColumns = [
  { id: 'isin', header: 'ISIN', accessor: 'isin' },
  { id: 'name', header: 'Name', accessor: 'name' },
  { id: 'investedAmount', header: 'Invested (EUR)', accessor: (row: any) => fmt(row.investedAmount), alignment: 'right' as const },
];

const dividendColumns = [
  { id: 'isin', header: 'ISIN', accessor: 'isin' },
  { id: 'name', header: 'Name', accessor: 'name' },
  { id: 'estimatedAnnualIncome', header: 'Est. Annual Income (EUR)', accessor: (row: any) => fmt(row.estimatedAnnualIncome), alignment: 'right' as const },
];

export default function Dashboard() {
  const [data, setData] = useState<DashboardDto | null>(null);

  useEffect(() => {
    api.get<DashboardDto>('/dashboard').then(r => setData(r.data));
  }, []);

  const holdings = useMemo(() => data?.top5Holdings ?? [], [data]);
  const dividends = useMemo(() => data?.top5DividendSources ?? [], [data]);

  if (!data) return <Paragraph>Loading...</Paragraph>;

  return (
    <Flex flexDirection="column" gap={24}>
      <Heading level={1}>Dashboard</Heading>

      <div className="kpi-grid">
        {[
          { label: 'Total Portfolio Value', value: fmt(data.totalPortfolioValue) + ' EUR' },
          { label: 'Stocks', value: String(data.stockCount) },
          { label: 'Dividend Ratio', value: fmt(data.totalDividendRatio) + '%' },
        ].map(kpi => (
          <Surface key={kpi.label} p={16}>
            <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>{kpi.label}</Paragraph>
            <Heading level={3}>{kpi.value}</Heading>
          </Surface>
        ))}
      </div>

      {data.lastQuoteFetchAt && (
        <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
          Last quote fetch: {new Date(data.lastQuoteFetchAt).toLocaleString('de-DE')}
        </Paragraph>
      )}

      <div className="section">
        <Flex alignItems="center" justifyContent="space-between">
          <Heading level={2}>Top 5 Holdings</Heading>
          <ExportButtons endpoint="/dashboard/holdings/export" />
        </Flex>
        <DataTable data={holdings} columns={holdingColumns} resizable fullWidth />
      </div>

      <div className="section">
        <Flex alignItems="center" justifyContent="space-between">
          <Heading level={2}>Top 5 Dividend Sources</Heading>
          <ExportButtons endpoint="/dashboard/dividends/export" />
        </Flex>
        <DataTable data={dividends} columns={dividendColumns} resizable fullWidth />
      </div>
    </Flex>
  );
}
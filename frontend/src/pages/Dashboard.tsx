import { useEffect, useState } from 'react';
import { Flex, Surface } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import api from '../api/client';
import type { DashboardDto } from '../types';
import ExportButtons from '../components/ExportButtons';
import IsinCell from '../components/IsinCell';

const fmtEur = (v: number) => v.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' EUR';
const fmtPct = (v: number) => v.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' %';

const holdingColumns = [
  { id: 'isin', header: 'ISIN', accessor: 'isin',
    cell: ({ rowData }: { rowData: any }) => <IsinCell isin={rowData.isin} /> },
  { id: 'name', header: 'Name', accessor: 'name' },
  { id: 'investedAmount', header: 'Invested Amount', accessor: (r: any) => fmtEur(r.investedAmount) },
];

const dividendColumns = [
  { id: 'isin', header: 'ISIN', accessor: 'isin',
    cell: ({ rowData }: { rowData: any }) => <IsinCell isin={rowData.isin} /> },
  { id: 'name', header: 'Name', accessor: 'name' },
  { id: 'estimatedAnnualIncome', header: 'Annual Income', accessor: (r: any) => fmtEur(r.estimatedAnnualIncome) },
];

export default function Dashboard() {
  const [dashboard, setDashboard] = useState<DashboardDto | null>(null);

  useEffect(() => {
    api.get<DashboardDto>('/dashboard').then(r => setDashboard(r.data));
  }, []);

  if (!dashboard) return <Paragraph>Loading...</Paragraph>;

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Dashboard</Heading>

      <div className="kpi-grid">
        <Surface>
          <Heading level={5}>Portfolio Value</Heading>
          <Paragraph>{fmtEur(dashboard.totalPortfolioValue)}</Paragraph>
        </Surface>
        <Surface>
          <Heading level={5}>Stock Count</Heading>
          <Paragraph>{dashboard.stockCount}</Paragraph>
        </Surface>
        <Surface>
          <Heading level={5}>Dividend Ratio</Heading>
          <Paragraph>{fmtPct(dashboard.totalDividendRatio)}</Paragraph>
        </Surface>
      </div>

      <Heading level={3}>Top 5 Holdings</Heading>
      <ExportButtons endpoint="/dashboard/holdings/export" />
      <DataTable data={dashboard.top5Holdings} columns={holdingColumns} resizable fullWidth />

      <Heading level={3}>Top 5 Dividend Sources</Heading>
      <ExportButtons endpoint="/dashboard/dividends/export" />
      <DataTable data={dashboard.top5DividendSources} columns={dividendColumns} resizable fullWidth />

      <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
        Last quote fetch: {dashboard.lastQuoteFetchAt ?? '\u2014'}
      </Paragraph>
    </Flex>
  );
}
import { useEffect, useState, useCallback } from 'react';
import { useParams } from 'react-router';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import { Button } from '@dynatrace/strato-components/buttons';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import { PieChart, Pie, Cell, Legend, Tooltip, ResponsiveContainer } from 'recharts';
import api from '../api/client';
import type { DiversificationEntry, PaginatedResponse } from '../types';
import useServerTable from '../hooks/useServerTable';
import ExportButtons from '../components/ExportButtons';
import PaginationControls from '../components/PaginationControls';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#845EC2',
  '#D65DB1', '#FF6F91', '#FF9671', '#FFC75F', '#F9F871'];

const fmtEur = (v: number) => v.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' EUR';
const fmtPct = (v: number) => v.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' %';

export default function Analytics() {
  const { type } = useParams<{ type: string }>();
  const label = type === 'countries' ? 'Country' : 'Branch';

  const [allEntries, setAllEntries] = useState<DiversificationEntry[]>([]);
  const [chartLoading, setChartLoading] = useState(false);

  const table = useServerTable<DiversificationEntry>({
    endpoint: `/analytics/${type}`,
    defaultSortField: 'investedAmount',
    defaultSortDir: 'desc',
  });

  // Reset page when type changes
  useEffect(() => { table.setPage(1); }, [type]); // eslint-disable-line react-hooks/exhaustive-deps

  // Fetch all entries for pie chart
  const loadChart = useCallback(async () => {
    setChartLoading(true);
    try {
      const r = await api.get<PaginatedResponse<DiversificationEntry>>(`/analytics/${type}`, {
        params: { sortField: 'investedAmount', sortDir: 'desc', pageSize: -1 },
      });
      setAllEntries(r.data.items);
    } finally {
      setChartLoading(false);
    }
  }, [type]);

  useEffect(() => { loadChart(); }, [loadChart]);

  const columns = [
    { id: 'name', header: label, accessor: 'name', sortType: 'text' as const, alignment: 'left' as const, width: 240, minWidth: 200 },
    { id: 'investedAmount', header: 'Invested (EUR)', accessor: (row: DiversificationEntry) => fmtEur(row.investedAmount), sortType: 'number' as const, alignment: 'right' as const, width: 160, minWidth: 120 },
    { id: 'percentage', header: '%', accessor: (row: DiversificationEntry) => fmtPct(row.percentage), sortType: 'number' as const, alignment: 'right' as const, width: 120, minWidth: 80 },
  ];

  const loading = table.loading || chartLoading;

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>{label} Diversification</Heading>

      {loading ? (
        <Flex alignItems="center" gap={12}>
          <ProgressCircle aria-label={`Loading ${type}`} size="small" />
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>Loading...</Paragraph>
        </Flex>
      ) : (
        <>
          {allEntries.length > 0 && (
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie data={allEntries} dataKey="investedAmount" nameKey="name" innerRadius={60} outerRadius={120} paddingAngle={2}>
                  {allEntries.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                </Pie>
                <Legend />
                <Tooltip formatter={(v: number) => fmtEur(v)} />
              </PieChart>
            </ResponsiveContainer>
          )}

          <Flex alignItems="center" justifyContent="space-between">
            <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
              {table.totalItems} entries
            </Paragraph>
            <Flex gap={8} alignItems="center">
              <ExportButtons endpoint={`/analytics/${type}/export`} params={table.exportParams} />
              <Button variant="default" onClick={table.handleShowAll}>
                {table.pageSize === -1 ? 'Paginate' : 'Show All'}
              </Button>
            </Flex>
          </Flex>
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

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
import ExportButtons from '../components/ExportButtons';
import PaginationControls from '../components/PaginationControls';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#845EC2',
  '#D65DB1', '#FF6F91', '#FF9671', '#FFC75F', '#F9F871'];

const fmtEur = (v: number) => v.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' EUR';
const fmtPct = (v: number) => v.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' %';

export default function Analytics() {
  const { type } = useParams<{ type: string }>();
  const label = type === 'countries' ? 'Country' : 'Branch';

  const [data, setData] = useState<DiversificationEntry[]>([]);
  const [allEntries, setAllEntries] = useState<DiversificationEntry[]>([]);
  const [totalItems, setTotalItems] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [sortField, setSortField] = useState('investedAmount');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('desc');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [paged, all] = await Promise.all([
        api.get<PaginatedResponse<DiversificationEntry>>(`/analytics/${type}`, {
          params: { sortField, sortDir, page, pageSize },
        }),
        api.get<PaginatedResponse<DiversificationEntry>>(`/analytics/${type}`, {
          params: { sortField: 'investedAmount', sortDir: 'desc', pageSize: -1 },
        }),
      ]);
      setData(paged.data.items);
      setTotalItems(paged.data.totalItems);
      setTotalPages(paged.data.totalPages);
      setAllEntries(all.data.items);
    } finally {
      setLoading(false);
    }
  }, [type, sortField, sortDir, page, pageSize]);

  useEffect(() => { setPage(1); }, [type]);
  useEffect(() => { load(); }, [load]);

  const handleShowAll = () => {
    if (pageSize === -1) { setPageSize(10); setPage(1); } else { setPageSize(-1); }
  };

  const columns = [
    { id: 'name', header: label, accessor: 'name', sortType: 'text' as const, alignment: 'left' as const, width: 240, minWidth: 200 },
    { id: 'investedAmount', header: 'Invested (EUR)', accessor: (row: DiversificationEntry) => fmtEur(row.investedAmount), sortType: 'number' as const, alignment: 'right' as const, width: 160, minWidth: 120 },
    { id: 'percentage', header: '%', accessor: (row: DiversificationEntry) => fmtPct(row.percentage), sortType: 'number' as const, alignment: 'right' as const, width: 120, minWidth: 80 },
  ];

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
              {totalItems} entries
            </Paragraph>
            <Flex gap={8} alignItems="center">
              <ExportButtons endpoint={`/analytics/${type}/export`} params={{ sortField, sortDir }} />
              <Button variant="default" onClick={handleShowAll}>
                {pageSize === -1 ? 'Paginate' : 'Show All'}
              </Button>
            </Flex>
          </Flex>
          <DataTable data={data} columns={columns} sortable={{ manualSort: true }} resizable fullWidth sortBy={[{ id: sortField, desc: sortDir === 'desc' }]}
            onSortByChange={(s: any) => { if (s?.[0]) { setSortField(s[0].id); setSortDir(s[0].desc ? 'desc' : 'asc'); } else { setSortDir(d => d === 'asc' ? 'desc' : 'asc'); } setPage(1); }}>
          </DataTable>
          {pageSize !== -1 && (
            <PaginationControls page={page} totalPages={totalPages} pageSize={pageSize}
              onPageChange={setPage} onPageSizeChange={(size) => { setPageSize(size); setPage(1); }} />
          )}
        </>
      )}
    </Flex>
  );
}
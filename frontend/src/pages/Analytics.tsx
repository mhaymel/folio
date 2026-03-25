import { useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable, DataTablePagination } from '@dynatrace/strato-components/tables';
import api from '../api/client';
import type { DiversificationDto } from '../types';

const COLORS = [
  '#4e79a7', '#f28e2b', '#e15759', '#76b7b2', '#59a14f',
  '#edc948', '#b07aa1', '#ff9da7', '#9c755f', '#bab0ac',
];

const fmt = (n: number) =>
  n.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

export default function Analytics() {
  const { type } = useParams<{ type: string }>();
  const [data, setData] = useState<DiversificationDto | null>(null);

  useEffect(() => {
    api.get<DiversificationDto>(`/analytics/${type}`).then(r => setData(r.data));
  }, [type]);

  const title = type === 'countries' ? 'Country' : 'Branch';
  const tableData = useMemo(() => data?.entries ?? [], [data]);

  const columns = useMemo(() => [
    { id: 'name', header: title, accessor: 'name', sortType: 'text' as const },
    { id: 'investedAmount', header: 'Invested (EUR)', accessor: (r: any) => fmt(r.investedAmount), sortType: 'number' as const, alignment: 'right' as const },
    { id: 'percentage', header: '%', accessor: (r: any) => fmt(r.percentage) + '%', sortType: 'number' as const, alignment: 'right' as const },
  ], [title]);

  if (!data) return <Paragraph>Loading...</Paragraph>;

  return (
    <Flex flexDirection="column" gap={24}>
      <Heading level={1}>{title} Diversification</Heading>

      {data.entries.length === 0 ? (
        <Paragraph>No data available. Import transactions and {type} first.</Paragraph>
      ) : (
        <>
          <div className="chart-container">
            <ResponsiveContainer width="100%" height={400}>
              <PieChart>
                <Pie
                  data={data.entries}
                  dataKey="investedAmount"
                  nameKey="name"
                  cx="50%" cy="50%"
                  outerRadius={150}
                  innerRadius={80}
                  label={({ name, payload }: any) => `${name} ${payload.percentage.toFixed(1)}%`}
                >
                  {data.entries.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                </Pie>
                <Tooltip formatter={(value: any) => fmt(Number(value)) + ' EUR'} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <DataTable data={tableData} columns={columns} sortable resizable fullWidth>
            <DataTablePagination defaultPageSize={10} pageSizeOptions={[10, 20, 50, 100]} />
          </DataTable>
        </>
      )}
    </Flex>
  );
}
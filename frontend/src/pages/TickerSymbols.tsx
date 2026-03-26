import { useEffect, useMemo, useState } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable, DataTablePagination } from '@dynatrace/strato-components/tables';
import { Button } from '@dynatrace/strato-components/buttons';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import api from '../api/client';
import type { TickerSymbolDto } from '../types';
import ExportButtons from '../components/ExportButtons';

const columns = [
  { id: 'isin', header: 'ISIN', accessor: 'isin', sortType: 'text' as const, width: 140, minWidth: 140 },
  { id: 'tickerSymbol', header: 'Ticker Symbol', accessor: 'tickerSymbol', sortType: 'text' as const, width: 140, minWidth: 100 },
  { id: 'name', header: 'Name', accessor: 'name', sortType: 'text' as const, width: 300, minWidth: 200 },
];

export default function TickerSymbols() {
  const [data, setData] = useState<TickerSymbolDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [showAll, setShowAll] = useState(false);
  const [sortField, setSortField] = useState('isin');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc');

  useEffect(() => {
    setLoading(true);
    api.get<TickerSymbolDto[]>('/ticker-symbols')
      .then(r => setData(r.data))
      .finally(() => setLoading(false));
  }, []);

  const tableData = useMemo(() => data, [data]);

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Ticker Symbols</Heading>

      {loading ? (
        <Flex alignItems="center" gap={12}>
          <ProgressCircle aria-label="Loading ticker symbols" size="small" />
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>Loading...</Paragraph>
        </Flex>
      ) : (
        <>
          <Flex alignItems="center" justifyContent="space-between">
            <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
              {data.length} ticker symbol mappings
            </Paragraph>
            <Flex gap={8} alignItems="center">
              <ExportButtons endpoint="/ticker-symbols/export" params={{ sortField, sortDir }} />
              <Button variant="default" onClick={() => setShowAll(s => !s)}>
                {showAll ? 'Paginate' : 'Show All'}
              </Button>
            </Flex>
          </Flex>
          <DataTable
            data={tableData}
            columns={columns}
            sortable
            resizable
            fullWidth
            defaultSortBy={[{ id: 'isin', desc: false }]}
            onSortByChange={(s: any) => { if (s?.[0]) { setSortField(s[0].id); setSortDir(s[0].desc ? 'desc' : 'asc'); } }}
          >
            {!showAll && (
              <DataTablePagination defaultPageSize={10} pageSizeOptions={[10, 20, 50, 100]} />
            )}
          </DataTable>
        </>
      )}
    </Flex>
  );
}

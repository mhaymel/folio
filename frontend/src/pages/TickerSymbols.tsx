import { useMemo, useState } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import { Button } from '@dynatrace/strato-components/buttons';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import { Paragraph } from '@dynatrace/strato-components/typography';
import type { TickerSymbolDto } from '../types';
import useServerTable from '../hooks/useServerTable';
import type { PaginatedResponse } from '../types';
import ExportButtons from '../components/ExportButtons';
import PaginationControls from '../components/PaginationControls';
import LabeledInput from '../components/LabeledInput';
import IsinCell from '../components/IsinCell';

export default function TickerSymbols() {
  const [isinFilter, setIsinFilter] = useState('');
  const [tickerFilter, setTickerFilter] = useState('');
  const [nameFilter, setNameFilter] = useState('');

  const extraParams = useMemo(() => {
    const p: Record<string, string> = {};
    if (isinFilter) p.isin = isinFilter;
    if (tickerFilter) p.tickerSymbol = tickerFilter;
    if (nameFilter) p.name = nameFilter;
    return p;
  }, [isinFilter, tickerFilter, nameFilter]);

  const table = useServerTable<TickerSymbolDto, PaginatedResponse<TickerSymbolDto>>({
    endpoint: '/ticker-symbols',
    defaultSortField: 'isin',
    extraParams,
  });

  const columns = [
    {
      id: 'isin', header: 'ISIN', accessor: 'isin', sortType: 'text' as const, alignment: 'left' as const, width: 140, minWidth: 140,
      cell: ({ rowData }: { rowData: TickerSymbolDto }) => (
        <IsinCell isin={rowData.isin} onFilter={(v) => { setIsinFilter(v); table.setPage(1); }} activeFilter={isinFilter} />
      ),
    },
    { id: 'tickerSymbol', header: 'Ticker Symbol', accessor: 'tickerSymbol', sortType: 'text' as const, alignment: 'left' as const, width: 140, minWidth: 100 },
    { id: 'name', header: 'Name', accessor: (r: TickerSymbolDto) => r.name ?? '', sortType: 'text' as const, alignment: 'left' as const, width: 300, minWidth: 200 },
  ];

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Ticker Symbols</Heading>

      <Flex gap={16} alignItems="flex-end" flexWrap="wrap">
        <LabeledInput label="ISIN" value={isinFilter}
          onChange={(v: string) => { setIsinFilter(v); table.setPage(1); }} />
        <LabeledInput label="Ticker Symbol" value={tickerFilter}
          onChange={(v: string) => { setTickerFilter(v); table.setPage(1); }} />
        <LabeledInput label="Name" value={nameFilter}
          onChange={(v: string) => { setNameFilter(v); table.setPage(1); }} />
        <Button variant="emphasized" onClick={() => { setIsinFilter(''); setTickerFilter(''); setNameFilter(''); table.setPage(1); }}>Clear</Button>
      </Flex>

      <Flex alignItems="center" justifyContent="space-between">
        <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
          {table.totalItems} ticker symbols
        </Paragraph>
        <Flex gap={8} alignItems="center">
          <ExportButtons endpoint="/ticker-symbols/export" params={table.exportParams} />
          <Button variant="emphasized" onClick={table.handleShowAll}>
            {table.pageSize === -1 ? 'Paginate' : 'Show All'}
          </Button>
        </Flex>
      </Flex>

      {table.loading ? (
        <Flex alignItems="center" gap={12}>
          <ProgressCircle aria-label="Loading ticker symbols" size="small" />
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

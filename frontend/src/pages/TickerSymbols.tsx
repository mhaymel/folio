import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading } from '@dynatrace/strato-components/typography';
import type { TickerSymbolDto } from '../types';
import ServerTable from '../components/ServerTable';
import IsinCell from '../components/IsinCell';

const columns = [
  { id: 'isin', header: 'ISIN', accessor: 'isin', sortType: 'text' as const, alignment: 'left' as const, width: 140, minWidth: 140,
    cell: ({ rowData }: { rowData: TickerSymbolDto }) => <IsinCell isin={rowData.isin} /> },
  { id: 'tickerSymbol', header: 'Ticker Symbol', accessor: 'tickerSymbol', sortType: 'text' as const, alignment: 'left' as const, width: 140, minWidth: 100 },
  { id: 'name', header: 'Name', accessor: 'name', sortType: 'text' as const, alignment: 'left' as const, width: 300, minWidth: 200 },
];

export default function TickerSymbols() {
  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Ticker Symbols</Heading>
      <ServerTable<TickerSymbolDto>
        endpoint="/ticker-symbols"
        exportEndpoint="/ticker-symbols/export"
        columns={columns}
        defaultSortField="isin"
        itemLabel="ticker symbols"
      />
    </Flex>
  );
}

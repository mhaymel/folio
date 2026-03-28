import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading } from '@dynatrace/strato-components/typography';
import type { Currency } from '../types';
import ServerTable from '../components/ServerTable';

const columns = [
  { id: 'name', header: 'Currency', accessor: 'name', sortType: 'text' as const, alignment: 'left' as const, width: 300, minWidth: 200 },
];

export default function Currencies() {
  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Currencies</Heading>
      <ServerTable<Currency>
        endpoint="/currencies"
        exportEndpoint="/currencies/export"
        columns={columns}
        defaultSortField="name"
        itemLabel="currencies"
      />
    </Flex>
  );
}

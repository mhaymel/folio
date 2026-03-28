import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading } from '@dynatrace/strato-components/typography';
import type { Country } from '../types';
import ServerTable from '../components/ServerTable';

const columns = [
  { id: 'name', header: 'Country', accessor: 'name', sortType: 'text' as const, alignment: 'left' as const, width: 300, minWidth: 200 },
];

export default function Countries() {
  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Countries</Heading>
      <ServerTable<Country>
        endpoint="/countries"
        exportEndpoint="/countries/export"
        columns={columns}
        defaultSortField="name"
        itemLabel="countries"
      />
    </Flex>
  );
}

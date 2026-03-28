import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading } from '@dynatrace/strato-components/typography';
import type { Depot } from '../types';
import ServerTable from '../components/ServerTable';

const columns = [
  { id: 'name', header: 'Depot', accessor: 'name', sortType: 'text' as const, alignment: 'left' as const, width: 300, minWidth: 200 },
];

export default function Depots() {
  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Depots</Heading>
      <ServerTable<Depot>
        endpoint="/depots"
        exportEndpoint="/depots/export"
        columns={columns}
        defaultSortField="name"
        itemLabel="depots"
      />
    </Flex>
  );
}

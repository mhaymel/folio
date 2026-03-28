import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading } from '@dynatrace/strato-components/typography';
import type { Branch } from '../types';
import ServerTable from '../components/ServerTable';

const columns = [
  { id: 'name', header: 'Branch', accessor: 'name', sortType: 'text' as const, alignment: 'left' as const, width: 300, minWidth: 200 },
];

export default function Branches() {
  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Branches</Heading>
      <ServerTable<Branch>
        endpoint="/branches"
        exportEndpoint="/branches/export"
        columns={columns}
        defaultSortField="name"
        itemLabel="branches"
      />
    </Flex>
  );
}

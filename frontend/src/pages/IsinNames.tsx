import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading } from '@dynatrace/strato-components/typography';
import type { IsinNameDto } from '../types';
import ServerTable from '../components/ServerTable';
import IsinCell from '../components/IsinCell';

const columns = [
  { id: 'isin', header: 'ISIN', accessor: 'isin', sortType: 'text' as const, alignment: 'left' as const, width: 140, minWidth: 140,
    cell: ({ rowData }: { rowData: IsinNameDto }) => <IsinCell isin={rowData.isin} /> },
  { id: 'name', header: 'Name', accessor: 'name', sortType: 'text' as const, alignment: 'left' as const, width: 400, minWidth: 200 },
];

export default function IsinNames() {
  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>ISIN Names</Heading>
      <ServerTable<IsinNameDto>
        endpoint="/isin-names"
        exportEndpoint="/isin-names/export"
        columns={columns}
        defaultSortField="name"
        itemLabel="ISIN names"
      />
    </Flex>
  );
}

import { useEffect, useMemo, useState } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import api from '../api/client';
import type { Depot } from '../types';

const columns = [
  { id: 'name', header: 'Depot', accessor: 'name', sortType: 'text' as const },
];

export default function Depots() {
  const [depots, setDepots] = useState<Depot[]>([]);

  useEffect(() => {
    api.get<Depot[]>('/depots').then(r => setDepots(r.data));
  }, []);

  const data = useMemo(() => depots, [depots]);

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Depots ({depots.length})</Heading>
      <DataTable data={data} columns={columns} sortable fullWidth />
    </Flex>
  );
}
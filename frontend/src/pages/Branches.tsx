import { useEffect, useMemo, useState } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import api from '../api/client';
import type { Branch } from '../types';

const columns = [
  { id: 'name', header: 'Branch', accessor: 'name', sortType: 'text' as const },
];

export default function Branches() {
  const [branches, setBranches] = useState<Branch[]>([]);

  useEffect(() => {
    api.get<Branch[]>('/branches').then(r => setBranches(r.data));
  }, []);

  const data = useMemo(() => branches, [branches]);

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Branches ({branches.length})</Heading>
      <DataTable data={data} columns={columns} sortable fullWidth />
    </Flex>
  );
}
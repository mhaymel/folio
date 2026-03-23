import { useEffect, useMemo, useState } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import api from '../api/client';
import type { Country } from '../types';

const columns = [
  { id: 'name', header: 'Country', accessor: 'name', sortType: 'text' as const },
];

export default function Countries() {
  const [countries, setCountries] = useState<Country[]>([]);

  useEffect(() => {
    api.get<Country[]>('/countries').then(r => setCountries(r.data));
  }, []);

  const data = useMemo(() => countries, [countries]);

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Countries ({countries.length})</Heading>
      <DataTable data={data} columns={columns} sortable fullWidth />
    </Flex>
  );
}
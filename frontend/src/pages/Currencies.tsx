import { useEffect, useMemo, useState } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import api from '../api/client';
import type { Currency } from '../types';

const columns = [
  { id: 'name', header: 'Currency', accessor: 'name', sortType: 'text' as const },
];

export default function Currencies() {
  const [currencies, setCurrencies] = useState<Currency[]>([]);

  useEffect(() => {
    api.get<Currency[]>('/currencies').then(r => setCurrencies(r.data));
  }, []);

  const data = useMemo(() => currencies, [currencies]);

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Currencies ({currencies.length})</Heading>
      <DataTable data={data} columns={columns} sortable fullWidth />
    </Flex>
  );
}
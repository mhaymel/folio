import { useCallback } from 'react';
import { Button } from '@dynatrace/strato-components/buttons';
import { Flex } from '@dynatrace/strato-components/layouts';

const API_BASE = 'http://localhost:8080/api';

interface ExportButtonsProps {
  /** Backend export path, e.g. "/stocks/export" */
  endpoint: string;
  /** Extra query params to forward (filters, sort). Values that are empty/null are omitted. */
  params?: Record<string, string | undefined | null>;
}

function buildUrl(endpoint: string, format: string, params?: Record<string, string | undefined | null>): string {
  const url = new URL(API_BASE + endpoint);
  url.searchParams.set('format', format);
  if (params) {
    for (const [key, val] of Object.entries(params)) {
      if (val) url.searchParams.set(key, val);
    }
  }
  return url.toString();
}

export default function ExportButtons({ endpoint, params }: ExportButtonsProps) {
  const download = useCallback((format: string) => {
    const url = buildUrl(endpoint, format, params);
    // Use a hidden link to trigger browser download
    const a = document.createElement('a');
    a.href = url;
    a.download = '';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }, [endpoint, params]);

  return (
    <Flex gap={4}>
      <Button variant="emphasized" onClick={() => download('csv')}>Export CSV</Button>
      <Button variant="emphasized" onClick={() => download('xlsx')}>Export Excel</Button>
    </Flex>
  );
}


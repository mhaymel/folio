import { useState } from 'react';
import { Flex, Surface } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import api from '../api/client';
import type { ImportResult } from '../types';

interface ImportSection {
  title: string;
  endpoint: string;
  description: string;
}

const sections: ImportSection[] = [
  { title: 'DeGiro Transactions', endpoint: '/import/degiro/transactions', description: 'Upload DeGiro Transactions.csv' },
  { title: 'DeGiro Account (Dividends)', endpoint: '/import/degiro/account', description: 'Upload DeGiro Account.csv' },
  { title: 'ZERO Orders', endpoint: '/import/zero/orders', description: 'Upload ZERO orders CSV' },
  { title: 'ZERO Account (Dividends)', endpoint: '/import/zero/account', description: 'Upload ZERO kontoumsaetze CSV' },
  { title: 'Dividends', endpoint: '/import/dividends', description: 'Upload dividende.csv' },
  { title: 'Branches', endpoint: '/import/branches', description: 'Upload branches.csv' },
  { title: 'Countries', endpoint: '/import/countries', description: 'Upload countries.csv' },
  { title: 'Ticker Symbols', endpoint: '/import/ticker-symbols', description: 'Upload ticker_symbol.csv' },
];

function ImportCard({ section }: { section: ImportSection }) {
  const [status, setStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
  const [result, setResult] = useState<ImportResult | null>(null);

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setStatus('loading');
    const form = new FormData();
    form.append('file', file);

    try {
      const res = await api.post<ImportResult>(section.endpoint, form, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setResult(res.data);
      setStatus(res.data.success ? 'success' : 'error');
    } catch (err: any) {
      setResult({ success: false, imported: 0, errors: [err.message] });
      setStatus('error');
    }
    e.target.value = '';
  };

  const borderColor = status === 'success' ? 'success' : status === 'error' ? 'critical' : 'neutral';

  return (
    <Surface p={20} color={borderColor} selected={status === 'success' || status === 'error'}>
      <Flex flexDirection="column" gap={8}>
        <Heading level={3}>{section.title}</Heading>
        <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>{section.description}</Paragraph>
        <input type="file" accept=".csv" onChange={handleUpload} disabled={status === 'loading'} />
        {status === 'loading' && <Paragraph>Uploading...</Paragraph>}
        {status === 'success' && result && (
          <Paragraph style={{ color: 'var(--dt-color-text-positive)' }}>Imported {result.imported} rows</Paragraph>
        )}
        {status === 'error' && result && (
          <Paragraph style={{ color: 'var(--dt-color-text-critical)' }}>
            {result.errors.length > 0 ? result.errors.slice(0, 3).join(' | ') : 'Import failed'}
            {result.imported > 0 && ` (${result.imported} rows imported)`}
          </Paragraph>
        )}
      </Flex>
    </Surface>
  );
}

export default function Import() {
  return (
    <Flex flexDirection="column" gap={24}>
      <Heading level={1}>Import Data</Heading>
      <div className="import-grid">
        {sections.map(s => <ImportCard key={s.endpoint} section={s} />)}
      </div>
    </Flex>
  );
}
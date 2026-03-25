import { useEffect, useState } from 'react';
import { Flex, Surface } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { Button } from '@dynatrace/strato-components/buttons';
import { Select } from '@dynatrace/strato-components/forms';
import api from '../api/client';
import type { QuoteSettingsDto } from '../types';

const INTERVALS = [
  { label: '15 min', value: 15 },
  { label: '30 min', value: 30 },
  { label: '1 hour', value: 60 },
  { label: '4 hours', value: 240 },
  { label: '12 hours', value: 720 },
  { label: '24 hours', value: 1440 },
];

export default function Settings() {
  const [settings, setSettings] = useState<QuoteSettingsDto | null>(null);
  const [selected, setSelected] = useState<number | null>(60);
  const [fetchStatus, setFetchStatus] = useState('');

  const load = () => {
    api.get<QuoteSettingsDto>('/quotes/settings').then(r => {
      setSettings(r.data);
      setSelected(r.data.intervalMinutes);
    });
  };

  useEffect(() => { load(); }, []);

  const saveInterval = async () => {
    if (selected == null) return;
    await api.put('/quotes/settings/interval', { intervalMinutes: selected });
    load();
  };

  const triggerFetch = async () => {
    setFetchStatus('Fetching...');
    try {
      const res = await api.post<{ status: string; fetchedCount?: number }>('/quotes/fetch');
      const count = res.data.fetchedCount;
      setFetchStatus(count != null ? `Fetched ${count} quotes` : res.data.status);
      load(); // refresh last fetch timestamp
    } catch {
      setFetchStatus('Fetch failed');
    }
    setTimeout(() => setFetchStatus(''), 5000);
  };

  if (!settings) return <Paragraph>Loading...</Paragraph>;

  return (
    <Flex flexDirection="column" gap={24}>
      <Heading level={1}>Settings</Heading>
      <Surface p={24} style={{ maxWidth: 480 }}>
        <Flex flexDirection="column" gap={16}>
          <Heading level={2}>Quote Fetch Interval</Heading>
          <label style={{ fontSize: 14, color: 'var(--dt-color-text-subdued)' }}>Interval</label>
          <Select<number> value={selected} onChange={val => setSelected(val)}>
            <Select.Content>
              {INTERVALS.map(i => (
                <Select.Option key={i.value} value={i.value}>{i.label}</Select.Option>
              ))}
            </Select.Content>
          </Select>
          <Flex gap={12} alignItems="center">
            <Button onClick={saveInterval} variant="accent">Save</Button>
          </Flex>
          <Flex gap={12} alignItems="center">
            <Button onClick={triggerFetch} variant="default">Fetch Now</Button>
            {fetchStatus && <Paragraph style={{ color: 'var(--dt-color-text-primary)' }}>{fetchStatus}</Paragraph>}
          </Flex>
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
            Last fetch: {settings.lastFetchAt ? new Date(settings.lastFetchAt).toLocaleString('de-DE') : 'Never'}
          </Paragraph>
        </Flex>
      </Surface>
    </Flex>
  );
}
import { useEffect, useState } from 'react';
import { Flex, Surface } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { Button } from '@dynatrace/strato-components/buttons';
import { Select, Switch, TextInput } from '@dynatrace/strato-components/forms';
import api from '../api/client';
import type { QuoteSettingsDto } from '../types';

const INTERVALS = [
  { label: '15 min', value: 15 },
  { label: '30 min', value: 30 },
  { label: '1 hour', value: 60 },
  { label: '4 hours', value: 240 },
  { label: '12 hours', value: 720 },
  { label: '24 hours', value: 1440 },
  { label: 'Custom', value: -1 },
];

const fmtDateTime = (iso: string): string => {
  const d = new Date(iso);
  const dd = String(d.getDate()).padStart(2, '0');
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const yyyy = d.getFullYear();
  const hh = String(d.getHours()).padStart(2, '0');
  const min = String(d.getMinutes()).padStart(2, '0');
  return `${dd}.${mm}.${yyyy} ${hh}:${min}`;
};

export default function Settings() {
  const [settings, setSettings] = useState<QuoteSettingsDto | null>(null);
  const [selected, setSelected] = useState<number | null>(60);
  const [customMinutes, setCustomMinutes] = useState('');
  const [fetchStatus, setFetchStatus] = useState('');

  const load = () => {
    api.get<QuoteSettingsDto>('/quotes/settings').then(r => {
      setSettings(r.data);
      const preset = INTERVALS.find(i => i.value === r.data.intervalMinutes && i.value !== -1);
      if (preset) {
        setSelected(r.data.intervalMinutes);
      } else {
        setSelected(-1);
        setCustomMinutes(String(r.data.intervalMinutes));
      }
    });
  };

  useEffect(() => { load(); }, []);

  const toggleEnabled = async (enabled: boolean) => {
    await api.put('/quotes/settings/enabled', { enabled });
    load();
  };

  const saveInterval = async () => {
    const minutes = selected === -1 ? parseInt(customMinutes, 10) : selected;
    if (minutes == null || isNaN(minutes) || minutes <= 0) return;
    await api.put('/quotes/settings/interval', { intervalMinutes: minutes });
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
          <Heading level={2}>Quote Fetching</Heading>
          <Flex gap={12} alignItems="center">
            <Switch value={settings.enabled} onChange={toggleEnabled} />
            <Paragraph>{settings.enabled ? 'Automatic quote fetching is enabled' : 'Automatic quote fetching is disabled'}</Paragraph>
          </Flex>
          <label style={{ fontSize: 14, color: 'var(--dt-color-text-subdued)' }}>Fetch Interval</label>
          <Select<number> value={selected} onChange={val => setSelected(val)}>
            <Select.Content>
              {INTERVALS.map(i => (
                <Select.Option key={i.value} value={i.value}>{i.label}</Select.Option>
              ))}
            </Select.Content>
          </Select>
          {selected === -1 && (
            <TextInput
              placeholder="Minutes"
              value={customMinutes}
              onChange={val => setCustomMinutes(val ?? '')}
            />
          )}
          <Flex gap={12} alignItems="center">
            <Button onClick={saveInterval} variant="accent">Save</Button>
          </Flex>
          <Flex gap={12} alignItems="center">
            <Button onClick={triggerFetch} variant="default">Fetch Now</Button>
            {fetchStatus && <Paragraph style={{ color: 'var(--dt-color-text-primary)' }}>{fetchStatus}</Paragraph>}
          </Flex>
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
            Last fetch: {settings.lastFetchAt ? fmtDateTime(settings.lastFetchAt) : '—'}
          </Paragraph>
        </Flex>
      </Surface>
    </Flex>
  );
}
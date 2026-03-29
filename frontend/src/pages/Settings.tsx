import { useEffect, useState } from 'react';
import { Flex, Surface } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { Button } from '@dynatrace/strato-components/buttons';
import { Select, Switch, TextInput } from '@dynatrace/strato-components/forms';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import api from '../api/client';
import type { QuoteSettingsDto } from '../types';

const INTERVAL_OPTIONS = [
  { label: '15 min', value: 15 },
  { label: '30 min', value: 30 },
  { label: '1 hour', value: 60 },
  { label: '4 hours', value: 240 },
  { label: '12 hours', value: 720 },
  { label: '24 hours', value: 1440 },
];

export default function Settings() {
  const [settings, setSettings] = useState<QuoteSettingsDto | null>(null);
  const [intervalSelect, setIntervalSelect] = useState('60');
  const [customInterval, setCustomInterval] = useState('');
  const [fetchStatus, setFetchStatus] = useState('');

  useEffect(() => {
    api.get<QuoteSettingsDto>('/quotes/settings').then(r => {
      setSettings(r.data);
      const preset = INTERVAL_OPTIONS.find(o => o.value === r.data.intervalMinutes);
      if (preset) {
        setIntervalSelect(String(preset.value));
      } else {
        setIntervalSelect('custom');
        setCustomInterval(String(r.data.intervalMinutes));
      }
    });
  }, []);

  const handleToggle = async (checked: boolean) => {
    await api.put('/quotes/settings/enabled', { enabled: checked });
    setSettings(s => s ? { ...s, enabled: checked } : s);
  };

  const handleSaveInterval = async () => {
    const minutes = intervalSelect === 'custom' ? parseInt(customInterval, 10) : parseInt(intervalSelect, 10);
    if (isNaN(minutes) || minutes < 1) return;
    await api.put('/quotes/settings/interval', null, { params: { minutes } });
    setSettings(s => s ? { ...s, intervalMinutes: minutes } : s);
  };

  const handleFetchNow = async () => {
    setFetchStatus('Fetching...');
    try {
      const r = await api.post<number>('/quotes/fetch');
      setFetchStatus(`Fetched ${r.data} quotes`);
      const updated = await api.get<QuoteSettingsDto>('/quotes/settings');
      setSettings(updated.data);
    } catch {
      setFetchStatus('Fetch failed');
    }
  };

  if (!settings) return <ProgressCircle aria-label="Loading settings" />;

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Settings</Heading>

      <Surface>
        <Heading level={3}>Quote Fetching</Heading>
        <Flex flexDirection="column" gap={12}>
          <Flex alignItems="center" gap={12}>
            <Switch checked={settings.enabled} onChange={handleToggle} />
            <Paragraph>Enable automatic quote fetching</Paragraph>
          </Flex>

          <Flex alignItems="center" gap={12}>
            <Select value={intervalSelect} onChange={(v: string) => setIntervalSelect(v)}>
              <Select.Content>
                {INTERVAL_OPTIONS.map(o => (
                  <Select.Option key={o.value} value={String(o.value)}>{o.label}</Select.Option>
                ))}
                <Select.Option value="custom">Custom</Select.Option>
              </Select.Content>
            </Select>
            {intervalSelect === 'custom' && (
              <TextInput placeholder="Minutes" value={customInterval}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setCustomInterval(e.target.value)} />
            )}
            <Button variant="emphasized" onClick={handleSaveInterval}>Save</Button>
          </Flex>

          <Flex alignItems="center" gap={12}>
            <Button variant="emphasized" onClick={handleFetchNow}>Fetch Now</Button>
            {fetchStatus && <Paragraph>{fetchStatus}</Paragraph>}
          </Flex>

          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
            Last fetch: {settings.lastFetchAt ?? '\u2014'}
          </Paragraph>
        </Flex>
      </Surface>
    </Flex>
  );
}
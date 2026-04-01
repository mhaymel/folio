import { useMemo } from 'react';
import { Select } from '@dynatrace/strato-components/forms';

interface MultiSelectProps {
  options: string[];
  selected: string[];
  onChange: (selected: string[]) => void;
  label: string;
  placeholder?: string;
}

const labelStyle: React.CSSProperties = {
  display: 'block',
  fontSize: 'var(--dt-typo-font-size-small, 12px)',
  fontWeight: 'var(--dt-typo-font-weight-default, 400)' as React.CSSProperties['fontWeight'],
  color: 'var(--dt-color-text-primary)',
  lineHeight: 1.4,
  whiteSpace: 'nowrap',
  textAlign: 'left',
  paddingLeft: 'var(--dt-spacings-size-12, 12px)',
};

export default function MultiSelect({ options, selected, onChange, label, placeholder }: MultiSelectProps) {
  const resolvedPlaceholder = placeholder ?? `Select ${label}`;
  const sortedOptions = useMemo(
    () => [...options].sort((a, b) => a.localeCompare(b)),
    [options],
  );

  const handleChange = (value: string[] | null) => {
    onChange(value ?? []);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 4, alignItems: 'flex-start' }}>
      <label style={labelStyle}>{label}</label>
      <Select
        name={resolvedPlaceholder}
        value={selected.length > 0 ? selected : null}
        onChange={handleChange}
        multiple
        clearable
      >
        <Select.Trigger placeholder={resolvedPlaceholder} />
        <Select.Content>
          {sortedOptions.map(opt => (
            <Select.Option key={opt} value={opt}>{opt}</Select.Option>
          ))}
        </Select.Content>
      </Select>
    </div>
  );
}

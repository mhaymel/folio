import { TextInput } from '@dynatrace/strato-components/forms';

interface LabeledInputProps {
  label: string;
  placeholder?: string;
  value: string;
  onChange: (value: string) => void;
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

export default function LabeledInput({ label, placeholder, value, onChange }: LabeledInputProps) {
  const resolvedPlaceholder = placeholder ?? `Filter ${label}...`;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 4, alignItems: 'flex-start' }}>
      <label style={labelStyle}>{label}</label>
      <TextInput placeholder={resolvedPlaceholder} value={value} onChange={onChange} />
    </div>
  );
}
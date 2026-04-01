import { useMemo } from 'react';
import { Select } from '@dynatrace/strato-components/forms';

interface MultiSelectProps {
  options: string[];
  selected: string[];
  onChange: (selected: string[]) => void;
  placeholder: string;
}

export default function MultiSelect({ options, selected, onChange, placeholder }: MultiSelectProps) {
  const sortedOptions = useMemo(
    () => [...options].sort((a, b) => a.localeCompare(b)),
    [options],
  );

  const handleChange = (value: string[] | null) => {
    onChange(value ?? []);
  };

  return (
    <Select
      name={placeholder}
      value={selected.length > 0 ? selected : null}
      onChange={handleChange}
      multiple
      clearable
      placeholder={placeholder}
    >
      <Select.Content>
        {sortedOptions.map(opt => (
          <Select.Option key={opt} value={opt}>{opt}</Select.Option>
        ))}
      </Select.Content>
    </Select>
  );
}

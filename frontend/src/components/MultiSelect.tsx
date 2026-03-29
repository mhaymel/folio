import { useState, useRef, useEffect } from 'react';

interface MultiSelectProps {
  options: string[];
  selected: string[];
  onChange: (selected: string[]) => void;
  placeholder: string;
}

export default function MultiSelect({ options, selected, onChange, placeholder }: MultiSelectProps) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const toggle = (value: string) => {
    if (selected.includes(value)) {
      onChange(selected.filter(s => s !== value));
    } else {
      onChange([...selected, value]);
    }
  };

  const label = selected.length === 0
    ? placeholder
    : selected.length <= 2
      ? selected.join(', ')
      : `${selected.length} selected`;

  return (
    <div ref={ref} style={{ position: 'relative', display: 'inline-block', minWidth: 140 }}>
      <button
        type="button"
        onClick={() => setOpen(!open)}
        style={{
          width: '100%',
          textAlign: 'left',
          padding: '6px 28px 6px 12px',
          fontSize: 14,
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
        }}
      >
        {label}
        <span style={{ position: 'absolute', right: 8, top: '50%', transform: 'translateY(-50%)' }}>
          {open ? '\u25B2' : '\u25BC'}
        </span>
      </button>
      {open && (
        <div
          style={{
            position: 'absolute',
            top: '100%',
            left: 0,
            right: 0,
            minWidth: 180,
            maxHeight: 240,
            overflowY: 'auto',
            background: 'var(--dt-color-background-container-neutral-subdued, #fff)',
            border: '1px solid var(--dt-color-border-default, #6b7280)',
            borderRadius: 4,
            zIndex: 1000,
            boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
          }}
        >
          <label
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              padding: '6px 12px',
              cursor: 'pointer',
              fontWeight: 600,
              fontSize: 14,
              borderBottom: '1px solid var(--dt-color-border-default, #e5e7eb)',
            }}
          >
            <input
              type="checkbox"
              checked={selected.length === 0}
              onChange={() => onChange([])}
            />
            {placeholder}
          </label>
          {options.map(opt => (
            <label
              key={opt}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 8,
                padding: '6px 12px',
                cursor: 'pointer',
                fontSize: 14,
              }}
            >
              <input
                type="checkbox"
                checked={selected.includes(opt)}
                onChange={() => toggle(opt)}
              />
              {opt}
            </label>
          ))}
        </div>
      )}
    </div>
  );
}

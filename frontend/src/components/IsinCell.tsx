import { CopyIcon, FilterIcon, FilterOutIcon } from '@dynatrace/strato-icons';
import { showToast } from '@dynatrace/strato-components/notifications';

interface IsinCellProps {
  isin: string;
  onFilter?: (isin: string) => void;
  activeFilter?: string;
  onDoubleClick?: () => void;
}

export default function IsinCell({ isin, onFilter, activeFilter, onDoubleClick }: IsinCellProps) {
  const isActive = activeFilter === isin;
  const handleCopy = async (e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await navigator.clipboard.writeText(isin);
      showToast({ title: `${isin} copied to clipboard`, type: 'success', lifespan: 2000 });
    } catch { /* ignore */ }
  };

  const handleFilter = (e: React.MouseEvent) => {
    e.stopPropagation();
    onFilter?.(isActive ? '' : isin);
  };

  return (
    <span
      onDoubleClick={onDoubleClick}
      style={{ paddingLeft: 10, display: 'flex', alignItems: 'center', gap: 4, overflow: 'hidden' }}
    >
      <span style={{ fontFamily: 'monospace', flexShrink: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
        {isin}
      </span>
      <span
        onClick={handleCopy}
        title="Copy ISIN to clipboard"
        style={{ display: 'inline-flex', alignItems: 'center', cursor: 'pointer', opacity: 0.5, flexShrink: 0 }}
        onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.opacity = '1'; }}
        onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.opacity = '0.5'; }}
      >
        <CopyIcon size={14} />
      </span>
      {onFilter && (
        <span
          onClick={handleFilter}
          title={isActive ? 'Remove ISIN filter' : 'Filter by ISIN'}
          style={{ display: 'inline-flex', alignItems: 'center', cursor: 'pointer', opacity: isActive ? 1 : 0.5, flexShrink: 0 }}
          onMouseEnter={(e) => { if (!isActive) (e.currentTarget as HTMLElement).style.opacity = '1'; }}
          onMouseLeave={(e) => { if (!isActive) (e.currentTarget as HTMLElement).style.opacity = '0.5'; }}
        >
          {isActive ? <FilterOutIcon size={14} /> : <FilterIcon size={14} />}
        </span>
      )}
    </span>
  );
}

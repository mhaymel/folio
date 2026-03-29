import { Flex } from '@dynatrace/strato-components/layouts';
import { Paragraph } from '@dynatrace/strato-components/typography';
import { Button } from '@dynatrace/strato-components/buttons';
import { Select } from '@dynatrace/strato-components/forms';

interface PaginationControlsProps {
  page: number;
  totalPages: number;
  pageSize: number;
  onPageChange: (page: number) => void;
  onPageSizeChange: (size: number) => void;
}

export default function PaginationControls({ page, totalPages, pageSize, onPageChange, onPageSizeChange }: PaginationControlsProps) {
  return (
    <Flex alignItems="center" justifyContent="space-between">
      <Flex gap={8} alignItems="center">
        <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
          Page {page} of {totalPages}
        </Paragraph>
        <Select value={String(pageSize)} onChange={(v: string) => { onPageSizeChange(Number(v)); }}>
          <Select.Content>
            <Select.Option value="10">10</Select.Option>
            <Select.Option value="20">20</Select.Option>
            <Select.Option value="50">50</Select.Option>
            <Select.Option value="100">100</Select.Option>
          </Select.Content>
        </Select>
      </Flex>
      <Flex gap={8} alignItems="center">
        <Button variant="emphasized" disabled={page <= 1} onClick={() => onPageChange(1)}>First</Button>
        <Button variant="emphasized" disabled={page <= 1} onClick={() => onPageChange(page - 1)}>Previous</Button>
        <Button variant="emphasized" disabled={page >= totalPages} onClick={() => onPageChange(page + 1)}>Next</Button>
        <Button variant="emphasized" disabled={page >= totalPages} onClick={() => onPageChange(totalPages)}>Last</Button>
      </Flex>
    </Flex>
  );
}
import { STATUS_COLORS } from '../../utils/constants';

export default function StatusBadge({ status }) {
  const color = STATUS_COLORS[status] || { bg: '#f3f4f6', text: '#6b7280' };
  const label = status?.replace(/_/g, ' ') || '—';

  return (
    <span
      style={{
        display: 'inline-block',
        padding: '2px 12px',
        borderRadius: '9999px',
        fontSize: '0.75rem',
        fontWeight: 600,
        letterSpacing: '0.025em',
        backgroundColor: color.bg,
        color: color.text,
        whiteSpace: 'nowrap',
        textTransform: 'capitalize',
      }}
    >
      {label.toLowerCase()}
    </span>
  );
}

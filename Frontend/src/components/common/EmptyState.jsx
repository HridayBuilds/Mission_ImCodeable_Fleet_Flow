import { Box, Typography } from '@mui/material';
import { FaInbox } from 'react-icons/fa6';

export default function EmptyState({ title = 'No data found', subtitle = '' }) {
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        py: 8,
        color: '#94a3b8',
      }}
    >
      <FaInbox size={48} style={{ marginBottom: 12 }} />
      <Typography variant="h6" sx={{ fontWeight: 600, color: '#64748b' }}>
        {title}
      </Typography>
      {subtitle && (
        <Typography variant="body2" sx={{ color: '#94a3b8', mt: 0.5 }}>
          {subtitle}
        </Typography>
      )}
    </Box>
  );
}

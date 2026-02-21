import { Typography, Button, Box } from '@mui/material';
import { FaPlus } from 'react-icons/fa6';

export default function PageHeader({ title, subtitle, actionLabel, onAction, icon }) {
  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        mb: 3,
      }}
    >
      <Box>
        <Typography variant="h5" sx={{ fontWeight: 700, color: '#1e293b' }}>
          {title}
        </Typography>
        {subtitle && (
          <Typography variant="body2" sx={{ color: '#64748b', mt: 0.5 }}>
            {subtitle}
          </Typography>
        )}
      </Box>
      {actionLabel && onAction && (
        <Button
          variant="contained"
          startIcon={icon || <FaPlus size={14} />}
          onClick={onAction}
          sx={{
            textTransform: 'none',
            borderRadius: '10px',
            px: 3,
            py: 1,
            fontWeight: 600,
            backgroundColor: '#2563eb',
            '&:hover': { backgroundColor: '#1d4ed8' },
          }}
        >
          {actionLabel}
        </Button>
      )}
    </Box>
  );
}

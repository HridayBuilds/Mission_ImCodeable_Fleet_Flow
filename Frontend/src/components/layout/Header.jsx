import { Box, Typography, IconButton, Avatar, Menu, MenuItem, Divider } from '@mui/material';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaArrowRightFromBracket, FaUser, FaChevronDown } from 'react-icons/fa6';
import { useAuth } from '../../hooks/useAuth';
import { ROLE_LABELS } from '../../utils/constants';
import { SIDEBAR_WIDTH } from './Sidebar';

export default function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = useState(null);

  const handleLogout = () => {
    setAnchorEl(null);
    logout();
    navigate('/login');
  };

  const initials = user?.name
    ? user.name
        .split(' ')
        .map((n) => n[0])
        .join('')
        .toUpperCase()
        .slice(0, 2)
    : 'U';

  return (
    <Box
      component="header"
      sx={{
        position: 'fixed',
        top: 0,
        left: SIDEBAR_WIDTH,
        right: 0,
        height: 64,
        backgroundColor: '#ffffff',
        borderBottom: '1px solid #e2e8f0',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'flex-end',
        px: 3,
        zIndex: 30,
      }}
    >
      <Box
        sx={{ display: 'flex', alignItems: 'center', gap: 1.5, cursor: 'pointer' }}
        onClick={(e) => setAnchorEl(e.currentTarget)}
      >
        <Avatar
          sx={{
            width: 36,
            height: 36,
            fontSize: '0.85rem',
            fontWeight: 600,
            backgroundColor: '#2563eb',
          }}
        >
          {initials}
        </Avatar>
        <Box sx={{ textAlign: 'right' }}>
          <Typography variant="body2" sx={{ fontWeight: 600, color: '#1e293b', lineHeight: 1.3 }}>
            {user?.name}
          </Typography>
          <Typography variant="caption" sx={{ color: '#64748b' }}>
            {ROLE_LABELS[user?.role] || user?.role}
          </Typography>
        </Box>
        <FaChevronDown size={12} color="#94a3b8" />
      </Box>

      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={() => setAnchorEl(null)}
        transformOrigin={{ horizontal: 'right', vertical: 'top' }}
        anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
        slotProps={{
          paper: {
            sx: {
              mt: 1,
              borderRadius: '10px',
              boxShadow: '0 4px 20px rgba(0,0,0,0.08)',
              minWidth: 180,
            },
          },
        }}
      >
        <MenuItem
          onClick={() => {
            setAnchorEl(null);
            navigate('/profile');
          }}
          sx={{ gap: 1.5, fontSize: '0.875rem' }}
        >
          <FaUser size={14} />
          Profile
        </MenuItem>
        <Divider />
        <MenuItem onClick={handleLogout} sx={{ gap: 1.5, fontSize: '0.875rem', color: '#ef4444' }}>
          <FaArrowRightFromBracket size={14} />
          Logout
        </MenuItem>
      </Menu>
    </Box>
  );
}

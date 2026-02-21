import { NavLink } from 'react-router-dom';
import { Box, Typography } from '@mui/material';
import {
  FaGauge,
  FaTruck,
  FaUsers,
  FaRoute,
  FaWrench,
  FaReceipt,
  FaGasPump,
  FaChartLine,
  FaUser,
} from 'react-icons/fa6';
import { useAuth } from '../../hooks/useAuth';
import { NAV_ITEMS } from '../../utils/constants';

const ICON_MAP = {
  dashboard: FaGauge,
  vehicles: FaTruck,
  drivers: FaUsers,
  trips: FaRoute,
  maintenance: FaWrench,
  expenses: FaReceipt,
  fuel: FaGasPump,
  analytics: FaChartLine,
  profile: FaUser,
};

const SIDEBAR_WIDTH = 260;

export default function Sidebar() {
  const { user } = useAuth();
  const role = user?.role;

  const visibleItems = NAV_ITEMS.filter((item) => item.roles.includes(role));

  return (
    <Box
      component="nav"
      sx={{
        width: SIDEBAR_WIDTH,
        minHeight: '100vh',
        backgroundColor: '#ffffff',
        borderRight: '1px solid #e2e8f0',
        display: 'flex',
        flexDirection: 'column',
        position: 'fixed',
        left: 0,
        top: 0,
        zIndex: 40,
      }}
    >
      {/* Logo */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1.5,
          px: 3,
          py: 2.5,
          borderBottom: '1px solid #e2e8f0',
        }}
      >
        <img
          src="/fleet-flow-logo.svg"
          alt="Fleet Flow"
          style={{ width: 36, height: 36 }}
        />
        <Typography
          variant="h6"
          sx={{
            fontWeight: 700,
            background: 'linear-gradient(135deg, #2563eb, #7c3aed)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
          }}
        >
          Fleet Flow
        </Typography>
      </Box>

      {/* Navigation */}
      <Box sx={{ flex: 1, py: 2, px: 1.5, display: 'flex', flexDirection: 'column', gap: 0.5 }}>
        {visibleItems.map((item) => {
          const Icon = ICON_MAP[item.icon] || FaGauge;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              style={{ textDecoration: 'none' }}
            >
              {({ isActive }) => (
                <Box
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 1.5,
                    px: 2,
                    py: 1.2,
                    borderRadius: '10px',
                    fontSize: '0.875rem',
                    fontWeight: isActive ? 600 : 500,
                    color: isActive ? '#2563eb' : '#64748b',
                    backgroundColor: isActive ? '#eff6ff' : 'transparent',
                    transition: 'all 0.15s ease',
                    '&:hover': {
                      backgroundColor: isActive ? '#eff6ff' : '#f8fafc',
                      color: isActive ? '#2563eb' : '#334155',
                    },
                  }}
                >
                  <Icon size={16} />
                  {item.label}
                </Box>
              )}
            </NavLink>
          );
        })}
      </Box>

      {/* Footer */}
      <Box
        sx={{
          px: 3,
          py: 2,
          borderTop: '1px solid #e2e8f0',
        }}
      >
        <Typography variant="caption" sx={{ color: '#94a3b8' }}>
          Fleet Flow v1.0
        </Typography>
      </Box>
    </Box>
  );
}

export { SIDEBAR_WIDTH };

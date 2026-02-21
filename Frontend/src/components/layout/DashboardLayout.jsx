import { Outlet } from 'react-router-dom';
import { Box } from '@mui/material';
import Sidebar, { SIDEBAR_WIDTH } from './Sidebar';
import Header from './Header';

export default function DashboardLayout() {
  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', backgroundColor: '#f8fafc' }}>
      <Sidebar />
      <Box
        component="main"
        sx={{
          flex: 1,
          ml: `${SIDEBAR_WIDTH}px`,
          mt: '64px',
          p: 3,
          minHeight: 'calc(100vh - 64px)',
        }}
      >
        <Header />
        <Outlet />
      </Box>
    </Box>
  );
}

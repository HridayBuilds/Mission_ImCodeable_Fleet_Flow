import { useEffect, useState } from 'react';
import { Box, Grid, Typography, Paper } from '@mui/material';
import {
  FaTruck,
  FaRoute,
  FaUsers,
  FaGear,
  FaBolt,
  FaCircleCheck,
  FaWarehouse,
  FaBox,
} from 'react-icons/fa6';
import { getDashboard } from '../../api/dashboardApi';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import StatusBadge from '../../components/common/StatusBadge';
import dayjs from 'dayjs';

const STAT_CARDS = [
  { key: 'totalVehicles', label: 'Total Vehicles', icon: FaTruck, color: '#2563eb', bg: '#eff6ff' },
  { key: 'activeFleet', label: 'Active Fleet', icon: FaBolt, color: '#16a34a', bg: '#f0fdf4' },
  { key: 'inShopVehicles', label: 'In Shop', icon: FaGear, color: '#f59e0b', bg: '#fffbeb' },
  { key: 'totalDrivers', label: 'Total Drivers', icon: FaUsers, color: '#7c3aed', bg: '#f5f3ff' },
  { key: 'availableDrivers', label: 'Available Drivers', icon: FaCircleCheck, color: '#0d9488', bg: '#f0fdfa' },
  { key: 'activeTrips', label: 'Active Trips', icon: FaRoute, color: '#2563eb', bg: '#eff6ff' },
  { key: 'pendingCargo', label: 'Pending Cargo', icon: FaBox, color: '#ea580c', bg: '#fff7ed' },
];

export default function DashboardPage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getDashboard()
      .then((res) => setData(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner />;

  return (
    <Box>
      <Typography variant="h5" sx={{ fontWeight: 700, color: '#1e293b', mb: 3 }}>
        Dashboard
      </Typography>

      {/* Stat cards */}
      <Grid container spacing={2.5} sx={{ mb: 4 }}>
        {STAT_CARDS.map((card) => {
          const Icon = card.icon;
          return (
            <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={card.key}>
              <Paper
                elevation={0}
                sx={{
                  p: 2.5,
                  borderRadius: '14px',
                  border: '1px solid #e2e8f0',
                  display: 'flex',
                  alignItems: 'center',
                  gap: 2,
                }}
              >
                <Box
                  sx={{
                    width: 48,
                    height: 48,
                    borderRadius: '12px',
                    backgroundColor: card.bg,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <Icon size={22} color={card.color} />
                </Box>
                <Box>
                  <Typography variant="h5" sx={{ fontWeight: 700, color: '#1e293b', lineHeight: 1.2 }}>
                    {data?.[card.key] ?? 0}
                  </Typography>
                  <Typography variant="caption" sx={{ color: '#64748b' }}>
                    {card.label}
                  </Typography>
                </Box>
              </Paper>
            </Grid>
          );
        })}
      </Grid>

      {/* Recent trips */}
      <Paper
        elevation={0}
        sx={{
          borderRadius: '14px',
          border: '1px solid #e2e8f0',
          overflow: 'hidden',
        }}
      >
        <Box sx={{ px: 3, py: 2, borderBottom: '1px solid #e2e8f0' }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 600, color: '#1e293b' }}>
            Recent Trips
          </Typography>
        </Box>
        <Box sx={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.875rem' }}>
            <thead>
              <tr style={{ backgroundColor: '#f8fafc' }}>
                {['#', 'Vehicle', 'Driver', 'Route', 'Status', 'Created'].map((h) => (
                  <th
                    key={h}
                    style={{
                      padding: '10px 16px',
                      textAlign: 'left',
                      fontWeight: 600,
                      color: '#64748b',
                      fontSize: '0.75rem',
                      textTransform: 'uppercase',
                      letterSpacing: '0.05em',
                      borderBottom: '1px solid #e2e8f0',
                    }}
                  >
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {data?.recentTrips?.length === 0 && (
                <tr>
                  <td
                    colSpan={6}
                    style={{ padding: '32px 16px', textAlign: 'center', color: '#94a3b8' }}
                  >
                    No recent trips
                  </td>
                </tr>
              )}
              {data?.recentTrips?.map((trip) => (
                <tr
                  key={trip.id}
                  style={{ borderBottom: '1px solid #f1f5f9' }}
                >
                  <td style={{ padding: '10px 16px', color: '#334155' }}>
                    {trip.tripNumber}
                  </td>
                  <td style={{ padding: '10px 16px', color: '#334155' }}>
                    {trip.vehicleName || trip.vehicleLicensePlate}
                  </td>
                  <td style={{ padding: '10px 16px', color: '#334155' }}>
                    {trip.driverName}
                  </td>
                  <td style={{ padding: '10px 16px', color: '#334155' }}>
                    {trip.origin} → {trip.destination}
                  </td>
                  <td style={{ padding: '10px 16px' }}>
                    <StatusBadge status={trip.status} />
                  </td>
                  <td style={{ padding: '10px 16px', color: '#94a3b8', fontSize: '0.8rem' }}>
                    {dayjs(trip.createdAt).format('MMM DD, HH:mm')}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Box>
      </Paper>
    </Box>
  );
}

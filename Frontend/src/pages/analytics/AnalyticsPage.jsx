import { useEffect, useState } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  TextField,
  MenuItem,
} from '@mui/material';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
  LineChart,
  Line,
} from 'recharts';
import { FaBolt } from 'react-icons/fa6';
import toast from 'react-hot-toast';

import PageHeader from '../../components/common/PageHeader';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { useAuth } from '../../hooks/useAuth';
import { ROLE_PERMISSIONS } from '../../utils/constants';
import {
  getFinancialSummaries,
  getVehicleCosts,
  getTopCostliestVehicles,
  getFleetSummary,
  generateCurrentSummary,
} from '../../api/analyticsApi';

const COLORS = ['#2563eb', '#7c3aed', '#f59e0b', '#ef4444', '#10b981', '#06b6d4'];

const MONTHS = [
  '', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
  'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec',
];

/* Format large numbers for axis ticks: 113000 → "1.1L", 25000 → "25K" */
const formatAxis = (value) => {
  const abs = Math.abs(value);
  if (abs >= 100000) return `${(value / 100000).toFixed(1)}L`;
  if (abs >= 1000) return `${(value / 1000).toFixed(0)}K`;
  return value;
};

/* Format currency for tooltips */
const fmtCurrency = (v) => `₹${Number(v).toLocaleString('en-IN')}`;

export default function AnalyticsPage() {
  const { user } = useAuth();
  const canGenerate = ROLE_PERMISSIONS[user?.role]?.canWriteAnalytics;

  const [loading, setLoading] = useState(true);
  const [financial, setFinancial] = useState([]);
  const [vehicleCosts, setVehicleCosts] = useState([]);
  const [topCostliest, setTopCostliest] = useState([]);
  const [fleetSummary, setFleetSummary] = useState(null);
  const [year, setYear] = useState(new Date().getFullYear());

  const fetchAll = async () => {
    setLoading(true);
    try {
      const [fRes, vcRes, tcRes, fsRes] = await Promise.all([
        getFinancialSummaries({ year }),
        getVehicleCosts(),
        getTopCostliestVehicles(5),
        getFleetSummary(),
      ]);
      setFinancial(fRes.data);
      setVehicleCosts(vcRes.data);
      setTopCostliest(tcRes.data);
      setFleetSummary(fsRes.data);
    } catch {
      toast.error('Failed to load analytics');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAll();
  }, [year]);

  const handleGenerate = async () => {
    try {
      await generateCurrentSummary();
      toast.success('Summary generated for current month');
      fetchAll();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to generate summary');
    }
  };

  if (loading) return <LoadingSpinner />;

  // Prepare chart data
  const monthlyData = financial.map((f) => ({
    month: MONTHS[f.month] || f.month,
    Revenue: Number(f.revenue) || 0,
    'Fuel Cost': Number(f.fuelCost) || 0,
    Maintenance: Number(f.maintenanceCost) || 0,
    'Net Profit': Number(f.netProfit) || 0,
  }));

  const costPieData = topCostliest.map((v) => ({
    name: v.vehicleName || v.vehicleLicensePlate || 'Unknown',
    value: Number(v.totalOperationalCost) || 0,
  }));

  return (
    <Box>
      <PageHeader
        title="Analytics"
        subtitle="Fleet performance overview"
        actionLabel={canGenerate ? 'Generate Current Summary' : undefined}
        onAction={canGenerate ? handleGenerate : undefined}
        icon={<FaBolt size={14} />}
      />

      {/* Fleet summary cards */}
      {fleetSummary && (
        <Grid container spacing={2} sx={{ mb: 4 }}>
          {[
            { label: 'Total Revenue', value: fmtCurrency(fleetSummary.totalRevenue || 0), color: '#16a34a' },
            { label: 'Total Fuel Cost', value: fmtCurrency(fleetSummary.totalFuelCost || 0), color: '#f59e0b' },
            { label: 'Total Expenses', value: fmtCurrency(fleetSummary.totalExpenses || 0), color: '#ef4444' },
            { label: 'Net Profit', value: fmtCurrency(fleetSummary.netProfit || 0), color: Number(fleetSummary.netProfit) >= 0 ? '#16a34a' : '#ef4444' },
            { label: 'Fleet ROI', value: `${Number(fleetSummary.fleetROI || 0).toFixed(1)}%`, color: '#2563eb' },
            { label: 'Utilization Rate', value: `${Number(fleetSummary.utilizationRate || 0).toFixed(1)}%`, color: '#7c3aed' },
          ].map((card) => (
            <Grid size={{ xs: 6, md: 4, lg: 2 }} key={card.label}>
              <Paper
                elevation={0}
                sx={{
                  p: 2,
                  borderRadius: '12px',
                  border: '1px solid #e2e8f0',
                  textAlign: 'center',
                  minHeight: 90,
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'center',
                }}
              >
                <Typography
                  variant="caption"
                  sx={{ color: '#64748b', display: 'block', lineHeight: 1.3, mb: 0.5 }}
                >
                  {card.label}
                </Typography>
                <Typography
                  variant="subtitle1"
                  sx={{
                    fontWeight: 700,
                    color: card.color,
                    fontSize: '1rem',
                    lineHeight: 1.3,
                    wordBreak: 'break-word',
                  }}
                >
                  {card.value}
                </Typography>
              </Paper>
            </Grid>
          ))}
        </Grid>
      )}

      {/* Year filter */}
      <Box sx={{ display: 'flex', gap: 2, mb: 3, alignItems: 'center' }}>
        <TextField
          select
          size="small"
          label="Year"
          value={year}
          onChange={(e) => setYear(Number(e.target.value))}
          sx={{ minWidth: 120 }}
        >
          {[2023, 2024, 2025, 2026].map((y) => (
            <MenuItem key={y} value={y}>
              {y}
            </MenuItem>
          ))}
        </TextField>
      </Box>

      <Grid container spacing={3}>
        {/* Revenue vs Costs chart */}
        <Grid size={{ xs: 12, lg: 8 }}>
          <Paper
            elevation={0}
            sx={{ p: 3, borderRadius: '14px', border: '1px solid #e2e8f0', overflow: 'hidden' }}
          >
            <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2, color: '#1e293b' }}>
              Monthly Revenue vs Costs ({year})
            </Typography>
            {monthlyData.length === 0 ? (
              <Typography variant="body2" sx={{ color: '#94a3b8', py: 4, textAlign: 'center' }}>
                No financial data for {year}
              </Typography>
            ) : (
              <ResponsiveContainer width="100%" height={350}>
                <BarChart data={monthlyData} margin={{ top: 10, right: 10, left: 10, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                  <XAxis dataKey="month" tick={{ fontSize: 12 }} tickLine={false} />
                  <YAxis tick={{ fontSize: 11 }} tickFormatter={formatAxis} width={55} tickLine={false} />
                  <Tooltip
                    formatter={(v) => fmtCurrency(v)}
                    contentStyle={{ fontSize: 13, borderRadius: 8, border: '1px solid #e2e8f0' }}
                  />
                  <Legend
                    wrapperStyle={{ fontSize: 12, paddingTop: 8 }}
                    iconType="circle"
                    iconSize={8}
                  />
                  <Bar dataKey="Revenue" fill="#16a34a" radius={[4, 4, 0, 0]} maxBarSize={40} />
                  <Bar dataKey="Fuel Cost" fill="#f59e0b" radius={[4, 4, 0, 0]} maxBarSize={40} />
                  <Bar dataKey="Maintenance" fill="#ef4444" radius={[4, 4, 0, 0]} maxBarSize={40} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </Paper>
        </Grid>

        {/* Top costliest vehicles pie */}
        <Grid size={{ xs: 12, lg: 4 }}>
          <Paper
            elevation={0}
            sx={{ p: 3, borderRadius: '14px', border: '1px solid #e2e8f0', overflow: 'hidden' }}
          >
            <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2, color: '#1e293b' }}>
              Top Costliest Vehicles
            </Typography>
            {costPieData.length === 0 ? (
              <Typography variant="body2" sx={{ color: '#94a3b8', py: 4, textAlign: 'center' }}>
                No vehicle cost data
              </Typography>
            ) : (
              <ResponsiveContainer width="100%" height={350}>
                <PieChart>
                  <Pie
                    data={costPieData}
                    cx="50%"
                    cy="45%"
                    innerRadius={55}
                    outerRadius={90}
                    paddingAngle={3}
                    dataKey="value"
                    label={false}
                  >
                    {costPieData.map((_, i) => (
                      <Cell key={i} fill={COLORS[i % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip
                    formatter={(v) => fmtCurrency(v)}
                    contentStyle={{ fontSize: 13, borderRadius: 8, border: '1px solid #e2e8f0' }}
                  />
                  <Legend
                    layout="horizontal"
                    align="center"
                    verticalAlign="bottom"
                    wrapperStyle={{ fontSize: 11, paddingTop: 12 }}
                    iconType="circle"
                    iconSize={8}
                    formatter={(value) =>
                      value.length > 18 ? value.slice(0, 16) + '…' : value
                    }
                  />
                </PieChart>
              </ResponsiveContainer>
            )}
          </Paper>
        </Grid>

        {/* Net Profit trend */}
        <Grid size={{ xs: 12 }}>
          <Paper
            elevation={0}
            sx={{ p: 3, borderRadius: '14px', border: '1px solid #e2e8f0', overflow: 'hidden' }}
          >
            <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2, color: '#1e293b' }}>
              Net Profit Trend ({year})
            </Typography>
            {monthlyData.length === 0 ? (
              <Typography variant="body2" sx={{ color: '#94a3b8', py: 4, textAlign: 'center' }}>
                No data
              </Typography>
            ) : (
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={monthlyData} margin={{ top: 10, right: 20, left: 10, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                  <XAxis dataKey="month" tick={{ fontSize: 12 }} tickLine={false} />
                  <YAxis tick={{ fontSize: 11 }} tickFormatter={formatAxis} width={55} tickLine={false} />
                  <Tooltip
                    formatter={(v) => fmtCurrency(v)}
                    contentStyle={{ fontSize: 13, borderRadius: 8, border: '1px solid #e2e8f0' }}
                  />
                  <Legend
                    wrapperStyle={{ fontSize: 12, paddingTop: 8 }}
                    iconType="circle"
                    iconSize={8}
                  />
                  <Line
                    type="monotone"
                    dataKey="Net Profit"
                    stroke="#2563eb"
                    strokeWidth={2.5}
                    dot={{ r: 4, fill: '#2563eb', strokeWidth: 0 }}
                    activeDot={{ r: 6, strokeWidth: 0 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            )}
          </Paper>
        </Grid>

        {/* Vehicle cost table */}
        <Grid size={{ xs: 12 }}>
          <Paper
            elevation={0}
            sx={{ p: 3, borderRadius: '14px', border: '1px solid #e2e8f0' }}
          >
            <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2, color: '#1e293b' }}>
              Vehicle Cost Breakdown
            </Typography>
            <Box sx={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem', minWidth: 700 }}>
                <thead>
                  <tr style={{ backgroundColor: '#f8fafc' }}>
                    {['Vehicle', 'Fuel', 'Maintenance', 'Expenses', 'Total Op. Cost', 'Distance (km)', 'Fuel Eff.', 'Cost/km'].map((h) => (
                      <th
                        key={h}
                        style={{
                          padding: '10px 12px',
                          textAlign: 'left',
                          fontWeight: 600,
                          color: '#64748b',
                          fontSize: '0.7rem',
                          textTransform: 'uppercase',
                          borderBottom: '1px solid #e2e8f0',
                          whiteSpace: 'nowrap',
                        }}
                      >
                        {h}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {vehicleCosts.length === 0 ? (
                    <tr>
                      <td colSpan={8} style={{ padding: 24, textAlign: 'center', color: '#94a3b8' }}>
                        No vehicle cost data
                      </td>
                    </tr>
                  ) : (
                    vehicleCosts.map((v) => (
                      <tr key={v.vehicleId} style={{ borderBottom: '1px solid #f1f5f9' }}>
                        <td style={{ padding: '10px 12px', fontWeight: 500, whiteSpace: 'nowrap' }}>
                          {v.vehicleName || v.vehicleLicensePlate}
                        </td>
                        <td style={{ padding: '10px 12px', whiteSpace: 'nowrap' }}>{fmtCurrency(v.totalFuelCost || 0)}</td>
                        <td style={{ padding: '10px 12px', whiteSpace: 'nowrap' }}>{fmtCurrency(v.totalMaintenanceCost || 0)}</td>
                        <td style={{ padding: '10px 12px', whiteSpace: 'nowrap' }}>{fmtCurrency(v.totalExpenseCost || 0)}</td>
                        <td style={{ padding: '10px 12px', fontWeight: 600, whiteSpace: 'nowrap' }}>{fmtCurrency(v.totalOperationalCost || 0)}</td>
                        <td style={{ padding: '10px 12px', whiteSpace: 'nowrap' }}>{(v.totalDistance || 0).toLocaleString()}</td>
                        <td style={{ padding: '10px 12px', whiteSpace: 'nowrap' }}>{(v.fuelEfficiency || 0).toFixed(2)} km/L</td>
                        <td style={{ padding: '10px 12px', whiteSpace: 'nowrap' }}>{fmtCurrency(v.costPerKm || 0)}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}

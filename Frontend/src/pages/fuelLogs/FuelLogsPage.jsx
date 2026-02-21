import { useEffect, useState, useMemo } from 'react';
import {
  Box,
  TextField,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  IconButton,
  Tooltip,
} from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { FaTrash } from 'react-icons/fa6';
import toast from 'react-hot-toast';
import dayjs from 'dayjs';

import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { useAuth } from '../../hooks/useAuth';
import { ROLE_PERMISSIONS } from '../../utils/constants';
import { CreateFuelLogSchema } from '../../utils/validators';
import { getFuelLogs, createFuelLog, deleteFuelLog } from '../../api/fuelLogApi';
import { getVehicles } from '../../api/vehicleApi';
import { getTrips } from '../../api/tripApi';

export default function FuelLogsPage() {
  const { user } = useAuth();
  const canWrite = ROLE_PERMISSIONS[user?.role]?.canWriteFuelLogs;

  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [vehicles, setVehicles] = useState([]);
  const [trips, setTrips] = useState([]);

  const [deleteDialog, setDeleteDialog] = useState({ open: false, log: null });
  const [deleting, setDeleting] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({ resolver: zodResolver(CreateFuelLogSchema) });

  const fetchLogs = async () => {
    setLoading(true);
    try {
      const res = await getFuelLogs();
      setLogs(res.data);
    } catch {
      toast.error('Failed to load fuel logs');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  const openCreate = async () => {
    reset({ vehicleId: '', tripId: '', liters: '', cost: '', odometerAtFill: '', fillDate: '' });
    try {
      const [vRes, tRes] = await Promise.all([getVehicles(), getTrips()]);
      setVehicles(vRes.data);
      setTrips(tRes.data);
    } catch {}
    setDialogOpen(true);
  };

  const onSubmit = async (data) => {
    setSaving(true);
    try {
      const payload = { ...data };
      if (!payload.tripId) delete payload.tripId;
      // Convert fillDate to ISO LocalDateTime
      if (payload.fillDate && !payload.fillDate.includes('T')) {
        payload.fillDate = payload.fillDate + 'T00:00:00';
      }
      await createFuelLog(payload);
      toast.success('Fuel log created');
      setDialogOpen(false);
      fetchLogs();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create fuel log');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await deleteFuelLog(deleteDialog.log.id);
      toast.success('Fuel log deleted');
      setDeleteDialog({ open: false, log: null });
      fetchLogs();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Delete failed');
    } finally {
      setDeleting(false);
    }
  };

  const columns = useMemo(
    () => [
      { accessorKey: 'vehicleName', header: 'Vehicle', cell: ({ row }) => row.original.vehicleName || row.original.vehicleLicensePlate || '—' },
      { accessorKey: 'tripNumber', header: 'Trip #', cell: ({ getValue }) => getValue() ? `#${getValue()}` : '—' },
      {
        accessorKey: 'liters',
        header: 'Liters',
        cell: ({ getValue }) => getValue()?.toFixed(2) ?? '—',
      },
      {
        accessorKey: 'cost',
        header: 'Cost',
        cell: ({ getValue }) => (getValue() != null ? `₹${Number(getValue()).toLocaleString()}` : '—'),
      },
      {
        accessorKey: 'odometerAtFill',
        header: 'Odometer',
        cell: ({ getValue }) => getValue()?.toLocaleString() ?? '—',
      },
      {
        accessorKey: 'fillDate',
        header: 'Fill Date',
        cell: ({ getValue }) => dayjs(getValue()).format('MMM DD, YYYY HH:mm'),
      },
      { accessorKey: 'recordedByName', header: 'Recorded By', cell: ({ getValue }) => getValue() || '—' },
      ...(canWrite
        ? [
            {
              id: 'actions',
              header: 'Actions',
              cell: ({ row }) => (
                <Tooltip title="Delete">
                  <IconButton
                    size="small"
                    onClick={() => setDeleteDialog({ open: true, log: row.original })}
                  >
                    <FaTrash size={14} color="#ef4444" />
                  </IconButton>
                </Tooltip>
              ),
            },
          ]
        : []),
    ],
    [canWrite],
  );

  if (loading && logs.length === 0) return <LoadingSpinner />;

  return (
    <Box>
      <PageHeader
        title="Fuel Logs"
        subtitle={`${logs.length} records`}
        actionLabel={canWrite ? 'Add Fuel Log' : undefined}
        onAction={canWrite ? openCreate : undefined}
      />

      <DataTable data={logs} columns={columns} searchPlaceholder="Search fuel logs..." />

      {/* Create */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>Add Fuel Log</DialogTitle>
        <form onSubmit={handleSubmit(onSubmit)}>
          <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <TextField
              label="Vehicle *"
              select
              size="small"
              defaultValue=""
              {...register('vehicleId')}
              error={!!errors.vehicleId}
              helperText={errors.vehicleId?.message}
            >
              {vehicles.map((v) => (
                <MenuItem key={v.id} value={v.id}>
                  {v.name || v.licensePlate} — {v.licensePlate}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Trip (optional)"
              select
              size="small"
              defaultValue=""
              {...register('tripId')}
            >
              <MenuItem value="">None</MenuItem>
              {trips.map((t) => (
                <MenuItem key={t.id} value={t.id}>
                  #{t.tripNumber} — {t.origin} → {t.destination}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Liters *"
              type="number"
              size="small"
              {...register('liters')}
              error={!!errors.liters}
              helperText={errors.liters?.message}
            />
            <TextField
              label="Cost *"
              type="number"
              size="small"
              {...register('cost')}
              error={!!errors.cost}
              helperText={errors.cost?.message}
            />
            <TextField
              label="Odometer at Fill"
              type="number"
              size="small"
              {...register('odometerAtFill')}
              error={!!errors.odometerAtFill}
              helperText={errors.odometerAtFill?.message}
            />
            <TextField
              label="Fill Date *"
              type="datetime-local"
              size="small"
              {...register('fillDate')}
              error={!!errors.fillDate}
              helperText={errors.fillDate?.message}
              slotProps={{ inputLabel: { shrink: true } }}
            />
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 2 }}>
            <Button onClick={() => setDialogOpen(false)} sx={{ textTransform: 'none' }}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="contained"
              disabled={saving}
              sx={{
                textTransform: 'none',
                borderRadius: '8px',
                backgroundColor: '#2563eb',
                '&:hover': { backgroundColor: '#1d4ed8' },
              }}
            >
              {saving ? 'Creating...' : 'Create'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      <ConfirmDialog
        open={deleteDialog.open}
        title="Delete Fuel Log"
        message="Are you sure you want to delete this fuel log?"
        confirmLabel="Delete"
        onConfirm={handleDelete}
        onCancel={() => setDeleteDialog({ open: false, log: null })}
        loading={deleting}
      />
    </Box>
  );
}

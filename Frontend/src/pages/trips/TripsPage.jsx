import { useEffect, useState, useMemo } from 'react';
import {
  Box,
  MenuItem,
  TextField,
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
import { FaPlay, FaTruckMoving, FaCircleCheck, FaBan } from 'react-icons/fa6';
import toast from 'react-hot-toast';
import dayjs from 'dayjs';

import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { useAuth } from '../../hooks/useAuth';
import { TRIP_STATUS, ROLE_PERMISSIONS } from '../../utils/constants';
import { CreateTripSchema, CompleteTripSchema, CancelTripSchema } from '../../utils/validators';
import {
  getTrips,
  createTrip,
  dispatchTrip,
  markInTransit,
  completeTrip,
  cancelTrip,
} from '../../api/tripApi';
import { getAvailableVehicles } from '../../api/vehicleApi';
import { getAvailableDrivers } from '../../api/driverApi';

export default function TripsPage() {
  const { user } = useAuth();
  const canWrite = ROLE_PERMISSIONS[user?.role]?.canWriteTrips;

  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState('');

  // Create dialog
  const [createOpen, setCreateOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [availableVehicles, setAvailableVehicles] = useState([]);
  const [availableDrivers, setAvailableDrivers] = useState([]);

  // Complete dialog
  const [completeDialog, setCompleteDialog] = useState({ open: false, trip: null });
  // Cancel dialog
  const [cancelDialog, setCancelDialog] = useState({ open: false, trip: null });
  // Simple action confirm
  const [actionDialog, setActionDialog] = useState({ open: false, trip: null, action: '' });

  const createForm = useForm({ resolver: zodResolver(CreateTripSchema) });
  const completeForm = useForm({ resolver: zodResolver(CompleteTripSchema) });
  const cancelForm = useForm({ resolver: zodResolver(CancelTripSchema) });

  const fetchTrips = async () => {
    setLoading(true);
    try {
      const params = {};
      if (filterStatus) params.status = filterStatus;
      const res = await getTrips(params);
      setTrips(res.data);
    } catch {
      toast.error('Failed to load trips');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTrips();
  }, [filterStatus]);

  const openCreate = async () => {
    createForm.reset({ vehicleId: '', driverId: '', cargoWeight: '', origin: '', destination: '', estimatedFuelCost: '' });
    try {
      const [vRes, dRes] = await Promise.all([getAvailableVehicles(), getAvailableDrivers()]);
      setAvailableVehicles(vRes.data);
      setAvailableDrivers(dRes.data);
    } catch {
      toast.error('Failed to load available resources');
    }
    setCreateOpen(true);
  };

  const onCreateSubmit = async (data) => {
    setSaving(true);
    try {
      await createTrip(data);
      toast.success('Trip created');
      setCreateOpen(false);
      fetchTrips();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create trip');
    } finally {
      setSaving(false);
    }
  };

  const handleAction = async () => {
    const { trip, action } = actionDialog;
    try {
      if (action === 'dispatch') await dispatchTrip(trip.id);
      else if (action === 'in-transit') await markInTransit(trip.id);
      toast.success(`Trip ${action === 'dispatch' ? 'dispatched' : 'marked in transit'}`);
      setActionDialog({ open: false, trip: null, action: '' });
      fetchTrips();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Action failed');
    }
  };

  const onCompleteSubmit = async (data) => {
    try {
      await completeTrip(completeDialog.trip.id, data);
      toast.success('Trip completed');
      setCompleteDialog({ open: false, trip: null });
      fetchTrips();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to complete trip');
    }
  };

  const onCancelSubmit = async (data) => {
    try {
      await cancelTrip(cancelDialog.trip.id, data);
      toast.success('Trip cancelled');
      setCancelDialog({ open: false, trip: null });
      fetchTrips();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to cancel trip');
    }
  };

  const columns = useMemo(
    () => [
      { accessorKey: 'tripNumber', header: '#', cell: ({ getValue }) => <strong>{getValue()}</strong> },
      { accessorKey: 'vehicleName', header: 'Vehicle', cell: ({ row }) => row.original.vehicleName || row.original.vehicleLicensePlate || '—' },
      { accessorKey: 'driverName', header: 'Driver' },
      {
        id: 'route',
        header: 'Route',
        cell: ({ row }) => `${row.original.origin} → ${row.original.destination}`,
      },
      {
        accessorKey: 'cargoWeight',
        header: 'Cargo (kg)',
        cell: ({ getValue }) => getValue()?.toLocaleString() ?? '—',
      },
      {
        accessorKey: 'revenue',
        header: 'Revenue',
        cell: ({ getValue }) => (getValue() != null ? `₹${Number(getValue()).toLocaleString()}` : '—'),
      },
      {
        accessorKey: 'status',
        header: 'Status',
        cell: ({ getValue }) => <StatusBadge status={getValue()} />,
      },
      {
        accessorKey: 'createdAt',
        header: 'Created',
        cell: ({ getValue }) => dayjs(getValue()).format('MMM DD, YYYY'),
      },
      ...(canWrite
        ? [
            {
              id: 'actions',
              header: 'Actions',
              cell: ({ row }) => {
                const t = row.original;
                return (
                  <Box sx={{ display: 'flex', gap: 0.5 }}>
                    {t.status === 'DRAFT' && (
                      <Tooltip title="Dispatch">
                        <IconButton
                          size="small"
                          onClick={() => setActionDialog({ open: true, trip: t, action: 'dispatch' })}
                        >
                          <FaPlay size={13} color="#2563eb" />
                        </IconButton>
                      </Tooltip>
                    )}
                    {t.status === 'DISPATCHED' && (
                      <Tooltip title="Mark In Transit">
                        <IconButton
                          size="small"
                          onClick={() => setActionDialog({ open: true, trip: t, action: 'in-transit' })}
                        >
                          <FaTruckMoving size={14} color="#f59e0b" />
                        </IconButton>
                      </Tooltip>
                    )}
                    {t.status === 'IN_TRANSIT' && (
                      <Tooltip title="Complete">
                        <IconButton
                          size="small"
                          onClick={() => {
                            completeForm.reset({ endOdometer: '', revenue: '' });
                            setCompleteDialog({ open: true, trip: t });
                          }}
                        >
                          <FaCircleCheck size={14} color="#16a34a" />
                        </IconButton>
                      </Tooltip>
                    )}
                    {['DRAFT', 'DISPATCHED'].includes(t.status) && (
                      <Tooltip title="Cancel">
                        <IconButton
                          size="small"
                          onClick={() => {
                            cancelForm.reset({ cancellationReason: '' });
                            setCancelDialog({ open: true, trip: t });
                          }}
                        >
                          <FaBan size={14} color="#ef4444" />
                        </IconButton>
                      </Tooltip>
                    )}
                  </Box>
                );
              },
            },
          ]
        : []),
    ],
    [canWrite],
  );

  if (loading && trips.length === 0) return <LoadingSpinner />;

  return (
    <Box>
      <PageHeader
        title="Trips"
        subtitle={`${trips.length} trips`}
        actionLabel={canWrite ? 'Create Trip' : undefined}
        onAction={canWrite ? openCreate : undefined}
      />

      <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
        <TextField
          select
          size="small"
          label="Status"
          value={filterStatus}
          onChange={(e) => setFilterStatus(e.target.value)}
          sx={{ minWidth: 160 }}
        >
          <MenuItem value="">All</MenuItem>
          {TRIP_STATUS.map((s) => (
            <MenuItem key={s} value={s}>
              {s.replace(/_/g, ' ')}
            </MenuItem>
          ))}
        </TextField>
      </Box>

      <DataTable data={trips} columns={columns} searchPlaceholder="Search trips..." />

      {/* Create Trip */}
      <Dialog open={createOpen} onClose={() => setCreateOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>Create Trip</DialogTitle>
        <form onSubmit={createForm.handleSubmit(onCreateSubmit)}>
          <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <TextField
              label="Vehicle *"
              select
              size="small"
              defaultValue=""
              {...createForm.register('vehicleId')}
              error={!!createForm.formState.errors.vehicleId}
              helperText={createForm.formState.errors.vehicleId?.message}
            >
              {availableVehicles.map((v) => (
                <MenuItem key={v.id} value={v.id}>
                  {v.name || v.licensePlate} — {v.licensePlate}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Driver *"
              select
              size="small"
              defaultValue=""
              {...createForm.register('driverId')}
              error={!!createForm.formState.errors.driverId}
              helperText={createForm.formState.errors.driverId?.message}
            >
              {availableDrivers.map((d) => (
                <MenuItem key={d.id} value={d.id}>
                  {d.name} — {d.licenseNumber}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Cargo Weight (kg) *"
              type="number"
              size="small"
              {...createForm.register('cargoWeight')}
              error={!!createForm.formState.errors.cargoWeight}
              helperText={createForm.formState.errors.cargoWeight?.message}
            />
            <TextField
              label="Origin *"
              size="small"
              {...createForm.register('origin')}
              error={!!createForm.formState.errors.origin}
              helperText={createForm.formState.errors.origin?.message}
            />
            <TextField
              label="Destination *"
              size="small"
              {...createForm.register('destination')}
              error={!!createForm.formState.errors.destination}
              helperText={createForm.formState.errors.destination?.message}
            />
            <TextField
              label="Estimated Fuel Cost"
              type="number"
              size="small"
              {...createForm.register('estimatedFuelCost')}
              error={!!createForm.formState.errors.estimatedFuelCost}
              helperText={createForm.formState.errors.estimatedFuelCost?.message}
            />
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 2 }}>
            <Button onClick={() => setCreateOpen(false)} sx={{ textTransform: 'none' }}>
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
              {saving ? 'Creating...' : 'Create Trip'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Dispatch / In Transit confirm */}
      <ConfirmDialog
        open={actionDialog.open}
        title={actionDialog.action === 'dispatch' ? 'Dispatch Trip' : 'Mark In Transit'}
        message={`${actionDialog.action === 'dispatch' ? 'Dispatch' : 'Mark in transit'} trip #${actionDialog.trip?.tripNumber}?`}
        confirmLabel="Confirm"
        confirmColor="primary"
        onConfirm={handleAction}
        onCancel={() => setActionDialog({ open: false, trip: null, action: '' })}
      />

      {/* Complete Trip */}
      <Dialog open={completeDialog.open} onClose={() => setCompleteDialog({ open: false, trip: null })} maxWidth="xs" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>Complete Trip #{completeDialog.trip?.tripNumber}</DialogTitle>
        <form onSubmit={completeForm.handleSubmit(onCompleteSubmit)}>
          <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <TextField
              label="End Odometer *"
              type="number"
              size="small"
              {...completeForm.register('endOdometer')}
              error={!!completeForm.formState.errors.endOdometer}
              helperText={completeForm.formState.errors.endOdometer?.message}
            />
            <TextField
              label="Revenue"
              type="number"
              size="small"
              {...completeForm.register('revenue')}
              error={!!completeForm.formState.errors.revenue}
              helperText={completeForm.formState.errors.revenue?.message}
            />
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 2 }}>
            <Button onClick={() => setCompleteDialog({ open: false, trip: null })} sx={{ textTransform: 'none' }}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="contained"
              color="success"
              sx={{ textTransform: 'none', borderRadius: '8px' }}
            >
              Complete
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Cancel Trip */}
      <Dialog open={cancelDialog.open} onClose={() => setCancelDialog({ open: false, trip: null })} maxWidth="xs" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>Cancel Trip #{cancelDialog.trip?.tripNumber}</DialogTitle>
        <form onSubmit={cancelForm.handleSubmit(onCancelSubmit)}>
          <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <TextField
              label="Cancellation Reason"
              multiline
              rows={3}
              size="small"
              {...cancelForm.register('cancellationReason')}
              error={!!cancelForm.formState.errors.cancellationReason}
              helperText={cancelForm.formState.errors.cancellationReason?.message}
            />
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 2 }}>
            <Button onClick={() => setCancelDialog({ open: false, trip: null })} sx={{ textTransform: 'none' }}>
              Back
            </Button>
            <Button
              type="submit"
              variant="contained"
              color="error"
              sx={{ textTransform: 'none', borderRadius: '8px' }}
            >
              Cancel Trip
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
}

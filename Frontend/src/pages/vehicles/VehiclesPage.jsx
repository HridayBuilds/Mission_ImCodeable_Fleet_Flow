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
import { FaPen, FaTrash, FaArrowsRotate } from 'react-icons/fa6';
import toast from 'react-hot-toast';
import dayjs from 'dayjs';

import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { useAuth } from '../../hooks/useAuth';
import { VEHICLE_TYPES, VEHICLE_STATUS, ROLE_PERMISSIONS } from '../../utils/constants';
import { CreateVehicleSchema, UpdateVehicleSchema } from '../../utils/validators';
import {
  getVehicles,
  createVehicle,
  updateVehicle,
  updateVehicleStatus,
  deleteVehicle,
} from '../../api/vehicleApi';

export default function VehiclesPage() {
  const { user } = useAuth();
  const canWrite = ROLE_PERMISSIONS[user?.role]?.canWriteVehicles;

  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState('');
  const [filterType, setFilterType] = useState('');

  // Dialog state
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingVehicle, setEditingVehicle] = useState(null);
  const [saving, setSaving] = useState(false);

  // Status change dialog
  const [statusDialog, setStatusDialog] = useState({ open: false, vehicle: null, newStatus: '' });

  // Delete dialog
  const [deleteDialog, setDeleteDialog] = useState({ open: false, vehicle: null });
  const [deleting, setDeleting] = useState(false);

  const isEditing = !!editingVehicle;
  const schema = isEditing ? UpdateVehicleSchema : CreateVehicleSchema;
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({ resolver: zodResolver(schema) });

  const fetchVehicles = async () => {
    setLoading(true);
    try {
      const params = {};
      if (filterStatus) params.status = filterStatus;
      if (filterType) params.type = filterType;
      const res = await getVehicles(params);
      setVehicles(res.data);
    } catch {
      toast.error('Failed to load vehicles');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVehicles();
  }, [filterStatus, filterType]);

  const openCreate = () => {
    setEditingVehicle(null);
    reset({ licensePlate: '', name: '', model: '', type: '', maxLoadCapacity: '', odometer: '', acquisitionCost: '' });
    setDialogOpen(true);
  };

  const openEdit = (vehicle) => {
    setEditingVehicle(vehicle);
    reset({
      name: vehicle.name || '',
      model: vehicle.model || '',
      type: vehicle.type || '',
      maxLoadCapacity: vehicle.maxLoadCapacity || '',
      acquisitionCost: vehicle.acquisitionCost || '',
    });
    setDialogOpen(true);
  };

  const onSubmit = async (data) => {
    setSaving(true);
    try {
      if (isEditing) {
        await updateVehicle(editingVehicle.id, data);
        toast.success('Vehicle updated');
      } else {
        await createVehicle(data);
        toast.success('Vehicle created');
      }
      setDialogOpen(false);
      fetchVehicles();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Operation failed');
    } finally {
      setSaving(false);
    }
  };

  const handleStatusChange = async () => {
    try {
      await updateVehicleStatus(statusDialog.vehicle.id, statusDialog.newStatus);
      toast.success('Status updated');
      setStatusDialog({ open: false, vehicle: null, newStatus: '' });
      fetchVehicles();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Status update failed');
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await deleteVehicle(deleteDialog.vehicle.id);
      toast.success('Vehicle deleted');
      setDeleteDialog({ open: false, vehicle: null });
      fetchVehicles();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Delete failed');
    } finally {
      setDeleting(false);
    }
  };

  const columns = useMemo(
    () => [
      { accessorKey: 'licensePlate', header: 'License Plate', cell: ({ getValue }) => <strong>{getValue()}</strong> },
      { accessorKey: 'name', header: 'Name', cell: ({ getValue }) => getValue() || '—' },
      { accessorKey: 'model', header: 'Model', cell: ({ getValue }) => getValue() || '—' },
      { accessorKey: 'type', header: 'Type', cell: ({ getValue }) => <StatusBadge status={getValue()} /> },
      {
        accessorKey: 'maxLoadCapacity',
        header: 'Capacity (kg)',
        cell: ({ getValue }) => getValue()?.toLocaleString() ?? '—',
      },
      {
        accessorKey: 'odometer',
        header: 'Odometer (km)',
        cell: ({ getValue }) => getValue()?.toLocaleString() ?? '—',
      },
      {
        accessorKey: 'status',
        header: 'Status',
        cell: ({ row }) => {
          const v = row.original;
          if (!canWrite) return <StatusBadge status={v.status} />;
          return (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <StatusBadge status={v.status} />
              <TextField
                select
                size="small"
                value=""
                onChange={(e) =>
                  setStatusDialog({ open: true, vehicle: v, newStatus: e.target.value })
                }
                sx={{ minWidth: 40, '& .MuiSelect-select': { py: 0.3, fontSize: '0.75rem' } }}
                displayEmpty
              >
                <MenuItem value="" disabled>
                  <FaArrowsRotate size={12} />
                </MenuItem>
                {VEHICLE_STATUS.filter((s) => s !== v.status).map((s) => (
                  <MenuItem key={s} value={s} sx={{ fontSize: '0.8rem' }}>
                    {s.replace(/_/g, ' ')}
                  </MenuItem>
                ))}
              </TextField>
            </Box>
          );
        },
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
              cell: ({ row }) => (
                <Box sx={{ display: 'flex', gap: 0.5 }}>
                  <Tooltip title="Edit">
                    <IconButton size="small" onClick={() => openEdit(row.original)}>
                      <FaPen size={14} color="#2563eb" />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Delete">
                    <IconButton
                      size="small"
                      onClick={() => setDeleteDialog({ open: true, vehicle: row.original })}
                    >
                      <FaTrash size={14} color="#ef4444" />
                    </IconButton>
                  </Tooltip>
                </Box>
              ),
            },
          ]
        : []),
    ],
    [canWrite],
  );

  if (loading && vehicles.length === 0) return <LoadingSpinner />;

  return (
    <Box>
      <PageHeader
        title="Vehicles"
        subtitle={`${vehicles.length} vehicles`}
        actionLabel={canWrite ? 'Add Vehicle' : undefined}
        onAction={canWrite ? openCreate : undefined}
      />

      {/* Filters */}
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
          {VEHICLE_STATUS.map((s) => (
            <MenuItem key={s} value={s}>
              {s.replace(/_/g, ' ')}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          select
          size="small"
          label="Type"
          value={filterType}
          onChange={(e) => setFilterType(e.target.value)}
          sx={{ minWidth: 160 }}
        >
          <MenuItem value="">All</MenuItem>
          {VEHICLE_TYPES.map((t) => (
            <MenuItem key={t} value={t}>
              {t.replace(/_/g, ' ')}
            </MenuItem>
          ))}
        </TextField>
      </Box>

      <DataTable data={vehicles} columns={columns} searchPlaceholder="Search vehicles..." />

      {/* Create / Edit dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>
          {isEditing ? 'Edit Vehicle' : 'Add Vehicle'}
        </DialogTitle>
        <form onSubmit={handleSubmit(onSubmit)}>
          <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            {!isEditing && (
              <TextField
                label="License Plate *"
                size="small"
                {...register('licensePlate')}
                error={!!errors.licensePlate}
                helperText={errors.licensePlate?.message}
              />
            )}
            <TextField
              label="Name"
              size="small"
              {...register('name')}
              error={!!errors.name}
              helperText={errors.name?.message}
            />
            <TextField
              label="Model"
              size="small"
              {...register('model')}
              error={!!errors.model}
              helperText={errors.model?.message}
            />
            <TextField
              label="Type *"
              select
              size="small"
              defaultValue={editingVehicle?.type || ''}
              {...register('type')}
              error={!!errors.type}
              helperText={errors.type?.message}
            >
              {VEHICLE_TYPES.map((t) => (
                <MenuItem key={t} value={t}>
                  {t.replace(/_/g, ' ')}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Max Load Capacity (kg) *"
              type="number"
              size="small"
              {...register('maxLoadCapacity')}
              error={!!errors.maxLoadCapacity}
              helperText={errors.maxLoadCapacity?.message}
            />
            {!isEditing && (
              <TextField
                label="Odometer (km)"
                type="number"
                size="small"
                {...register('odometer')}
                error={!!errors.odometer}
                helperText={errors.odometer?.message}
              />
            )}
            <TextField
              label="Acquisition Cost"
              type="number"
              size="small"
              {...register('acquisitionCost')}
              error={!!errors.acquisitionCost}
              helperText={errors.acquisitionCost?.message}
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
              {saving ? 'Saving...' : isEditing ? 'Update' : 'Create'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Status confirm */}
      <ConfirmDialog
        open={statusDialog.open}
        title="Change Vehicle Status"
        message={`Change status to "${statusDialog.newStatus?.replace(/_/g, ' ')}"?`}
        confirmLabel="Confirm"
        confirmColor="primary"
        onConfirm={handleStatusChange}
        onCancel={() => setStatusDialog({ open: false, vehicle: null, newStatus: '' })}
      />

      {/* Delete confirm */}
      <ConfirmDialog
        open={deleteDialog.open}
        title="Delete Vehicle"
        message={`Are you sure you want to delete "${deleteDialog.vehicle?.licensePlate}"?`}
        confirmLabel="Delete"
        onConfirm={handleDelete}
        onCancel={() => setDeleteDialog({ open: false, vehicle: null })}
        loading={deleting}
      />
    </Box>
  );
}

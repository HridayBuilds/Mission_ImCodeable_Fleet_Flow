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
  Chip,
} from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { FaPen, FaTrash, FaArrowsRotate, FaTriangleExclamation } from 'react-icons/fa6';
import toast from 'react-hot-toast';
import dayjs from 'dayjs';

import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { useAuth } from '../../hooks/useAuth';
import { DRIVER_STATUS, ROLE_PERMISSIONS } from '../../utils/constants';
import { CreateDriverSchema, UpdateDriverSchema } from '../../utils/validators';
import {
  getDrivers,
  createDriver,
  updateDriver,
  updateDriverStatus,
  fileComplaint,
  deleteDriver,
} from '../../api/driverApi';

export default function DriversPage() {
  const { user } = useAuth();
  const canWrite = ROLE_PERMISSIONS[user?.role]?.canWriteDrivers;

  const [drivers, setDrivers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState('');

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingDriver, setEditingDriver] = useState(null);
  const [saving, setSaving] = useState(false);

  const [statusDialog, setStatusDialog] = useState({ open: false, driver: null, newStatus: '' });
  const [deleteDialog, setDeleteDialog] = useState({ open: false, driver: null });
  const [deleting, setDeleting] = useState(false);
  const [complaintDialog, setComplaintDialog] = useState({ open: false, driver: null });

  const isEditing = !!editingDriver;
  const schema = isEditing ? UpdateDriverSchema : CreateDriverSchema;
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({ resolver: zodResolver(schema) });

  const fetchDrivers = async () => {
    setLoading(true);
    try {
      const params = {};
      if (filterStatus) params.status = filterStatus;
      const res = await getDrivers(params);
      setDrivers(res.data);
    } catch {
      toast.error('Failed to load drivers');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDrivers();
  }, [filterStatus]);

  const openCreate = () => {
    setEditingDriver(null);
    reset({ name: '', licenseNumber: '', licenseExpiryDate: '', licenseCategory: '', phone: '' });
    setDialogOpen(true);
  };

  const openEdit = (driver) => {
    setEditingDriver(driver);
    reset({
      name: driver.name || '',
      licenseExpiryDate: driver.licenseExpiryDate || '',
      licenseCategory: driver.licenseCategory || '',
      phone: driver.phone || '',
    });
    setDialogOpen(true);
  };

  const onSubmit = async (data) => {
    setSaving(true);
    try {
      // Clean empty strings
      const cleaned = Object.fromEntries(
        Object.entries(data).filter(([, v]) => v !== ''),
      );
      if (isEditing) {
        await updateDriver(editingDriver.id, cleaned);
        toast.success('Driver updated');
      } else {
        await createDriver(cleaned);
        toast.success('Driver created');
      }
      setDialogOpen(false);
      fetchDrivers();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Operation failed');
    } finally {
      setSaving(false);
    }
  };

  const handleStatusChange = async () => {
    try {
      await updateDriverStatus(statusDialog.driver.id, statusDialog.newStatus);
      toast.success('Status updated');
      setStatusDialog({ open: false, driver: null, newStatus: '' });
      fetchDrivers();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Status update failed');
    }
  };

  const handleComplaint = async () => {
    try {
      await fileComplaint(complaintDialog.driver.id);
      toast.success('Complaint filed');
      setComplaintDialog({ open: false, driver: null });
      fetchDrivers();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to file complaint');
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await deleteDriver(deleteDialog.driver.id);
      toast.success('Driver deleted');
      setDeleteDialog({ open: false, driver: null });
      fetchDrivers();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Delete failed');
    } finally {
      setDeleting(false);
    }
  };

  const columns = useMemo(
    () => [
      { accessorKey: 'name', header: 'Name', cell: ({ getValue }) => <strong>{getValue()}</strong> },
      { accessorKey: 'licenseNumber', header: 'License #' },
      {
        accessorKey: 'licenseExpiryDate',
        header: 'License Exp.',
        cell: ({ row }) => {
          const d = row.original;
          const expired = d.licenseExpired;
          return (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              {dayjs(d.licenseExpiryDate).format('MMM DD, YYYY')}
              {expired && (
                <Chip
                  label="Expired"
                  size="small"
                  sx={{
                    height: 20,
                    fontSize: '0.65rem',
                    backgroundColor: '#fee2e2',
                    color: '#991b1b',
                  }}
                />
              )}
            </Box>
          );
        },
      },
      { accessorKey: 'phone', header: 'Phone', cell: ({ getValue }) => getValue() || '—' },
      {
        accessorKey: 'safetyScore',
        header: 'Safety Score',
        cell: ({ getValue }) => {
          const v = getValue();
          if (v == null) return '—';
          const color = v >= 80 ? '#16a34a' : v >= 50 ? '#f59e0b' : '#ef4444';
          return <span style={{ fontWeight: 600, color }}>{v.toFixed(1)}</span>;
        },
      },
      {
        accessorKey: 'complaints',
        header: 'Complaints',
        cell: ({ getValue }) => getValue() ?? 0,
      },
      {
        accessorKey: 'completionRate',
        header: 'Completion %',
        cell: ({ getValue }) => (getValue() != null ? `${getValue().toFixed(1)}%` : '—'),
      },
      {
        accessorKey: 'status',
        header: 'Status',
        cell: ({ row }) => {
          const d = row.original;
          if (!canWrite) return <StatusBadge status={d.status} />;
          return (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <StatusBadge status={d.status} />
              <TextField
                select
                size="small"
                value=""
                onChange={(e) =>
                  setStatusDialog({ open: true, driver: d, newStatus: e.target.value })
                }
                sx={{ minWidth: 40, '& .MuiSelect-select': { py: 0.3, fontSize: '0.75rem' } }}
                displayEmpty
              >
                <MenuItem value="" disabled>
                  <FaArrowsRotate size={12} />
                </MenuItem>
                {DRIVER_STATUS.filter((s) => s !== d.status).map((s) => (
                  <MenuItem key={s} value={s} sx={{ fontSize: '0.8rem' }}>
                    {s.replace(/_/g, ' ')}
                  </MenuItem>
                ))}
              </TextField>
            </Box>
          );
        },
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
                  <Tooltip title="File Complaint">
                    <IconButton
                      size="small"
                      onClick={() => setComplaintDialog({ open: true, driver: row.original })}
                    >
                      <FaTriangleExclamation size={14} color="#f59e0b" />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Delete">
                    <IconButton
                      size="small"
                      onClick={() => setDeleteDialog({ open: true, driver: row.original })}
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

  if (loading && drivers.length === 0) return <LoadingSpinner />;

  return (
    <Box>
      <PageHeader
        title="Drivers"
        subtitle={`${drivers.length} drivers`}
        actionLabel={canWrite ? 'Add Driver' : undefined}
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
          {DRIVER_STATUS.map((s) => (
            <MenuItem key={s} value={s}>
              {s.replace(/_/g, ' ')}
            </MenuItem>
          ))}
        </TextField>
      </Box>

      <DataTable data={drivers} columns={columns} searchPlaceholder="Search drivers..." />

      {/* Create / Edit */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>
          {isEditing ? 'Edit Driver' : 'Add Driver'}
        </DialogTitle>
        <form onSubmit={handleSubmit(onSubmit)}>
          <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <TextField
              label="Full Name *"
              size="small"
              {...register('name')}
              error={!!errors.name}
              helperText={errors.name?.message}
            />
            {!isEditing && (
              <TextField
                label="License Number *"
                size="small"
                {...register('licenseNumber')}
                error={!!errors.licenseNumber}
                helperText={errors.licenseNumber?.message}
              />
            )}
            <TextField
              label="License Expiry Date *"
              type="date"
              size="small"
              {...register('licenseExpiryDate')}
              error={!!errors.licenseExpiryDate}
              helperText={errors.licenseExpiryDate?.message}
              slotProps={{ inputLabel: { shrink: true } }}
            />
            <TextField
              label="License Category"
              size="small"
              {...register('licenseCategory')}
              error={!!errors.licenseCategory}
              helperText={errors.licenseCategory?.message}
            />
            <TextField
              label="Phone"
              size="small"
              {...register('phone')}
              error={!!errors.phone}
              helperText={errors.phone?.message}
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
        title="Change Driver Status"
        message={`Change status to "${statusDialog.newStatus?.replace(/_/g, ' ')}"?`}
        confirmLabel="Confirm"
        confirmColor="primary"
        onConfirm={handleStatusChange}
        onCancel={() => setStatusDialog({ open: false, driver: null, newStatus: '' })}
      />

      {/* Complaint confirm */}
      <ConfirmDialog
        open={complaintDialog.open}
        title="File Complaint"
        message={`File a complaint against "${complaintDialog.driver?.name}"? This will increase their complaint count and lower their safety score.`}
        confirmLabel="File Complaint"
        confirmColor="warning"
        onConfirm={handleComplaint}
        onCancel={() => setComplaintDialog({ open: false, driver: null })}
      />

      {/* Delete confirm */}
      <ConfirmDialog
        open={deleteDialog.open}
        title="Delete Driver"
        message={`Are you sure you want to delete "${deleteDialog.driver?.name}"?`}
        confirmLabel="Delete"
        onConfirm={handleDelete}
        onCancel={() => setDeleteDialog({ open: false, driver: null })}
        loading={deleting}
      />
    </Box>
  );
}

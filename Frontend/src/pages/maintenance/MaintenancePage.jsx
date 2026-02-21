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
import { MAINTENANCE_STATUS, ROLE_PERMISSIONS } from '../../utils/constants';
import { CreateMaintenanceSchema, UpdateMaintenanceSchema } from '../../utils/validators';
import {
  getMaintenanceLogs,
  createMaintenanceLog,
  updateMaintenanceLog,
  updateMaintenanceStatus,
  deleteMaintenanceLog,
} from '../../api/maintenanceApi';
import { getVehicles } from '../../api/vehicleApi';

export default function MaintenancePage() {
  const { user } = useAuth();
  const canWrite = ROLE_PERMISSIONS[user?.role]?.canWriteMaintenance;

  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState('');

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingLog, setEditingLog] = useState(null);
  const [saving, setSaving] = useState(false);
  const [vehicles, setVehicles] = useState([]);

  const [statusDialog, setStatusDialog] = useState({ open: false, log: null, newStatus: '' });
  const [deleteDialog, setDeleteDialog] = useState({ open: false, log: null });
  const [deleting, setDeleting] = useState(false);

  const isEditing = !!editingLog;
  const schema = isEditing ? UpdateMaintenanceSchema : CreateMaintenanceSchema;
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({ resolver: zodResolver(schema) });

  const fetchLogs = async () => {
    setLoading(true);
    try {
      const params = {};
      if (filterStatus) params.status = filterStatus;
      const res = await getMaintenanceLogs(params);
      setLogs(res.data);
    } catch {
      toast.error('Failed to load maintenance logs');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, [filterStatus]);

  const openCreate = async () => {
    setEditingLog(null);
    reset({ vehicleId: '', serviceName: '', issueDescription: '', serviceDate: '', cost: '' });
    try {
      const res = await getVehicles();
      setVehicles(res.data);
    } catch {}
    setDialogOpen(true);
  };

  const openEdit = (log) => {
    setEditingLog(log);
    reset({
      serviceName: log.serviceName || '',
      issueDescription: log.issueDescription || '',
      serviceDate: log.serviceDate || '',
      cost: log.cost || '',
    });
    setDialogOpen(true);
  };

  const onSubmit = async (data) => {
    setSaving(true);
    try {
      const cleaned = Object.fromEntries(
        Object.entries(data).filter(([, v]) => v !== ''),
      );
      if (isEditing) {
        await updateMaintenanceLog(editingLog.id, cleaned);
        toast.success('Maintenance log updated');
      } else {
        await createMaintenanceLog(cleaned);
        toast.success('Maintenance log created');
      }
      setDialogOpen(false);
      fetchLogs();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Operation failed');
    } finally {
      setSaving(false);
    }
  };

  const handleStatusChange = async () => {
    try {
      await updateMaintenanceStatus(statusDialog.log.id, { status: statusDialog.newStatus });
      toast.success('Status updated');
      setStatusDialog({ open: false, log: null, newStatus: '' });
      fetchLogs();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Status update failed');
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await deleteMaintenanceLog(deleteDialog.log.id);
      toast.success('Maintenance log deleted');
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
      { accessorKey: 'serviceName', header: 'Service', cell: ({ getValue }) => getValue() || '—' },
      { accessorKey: 'issueDescription', header: 'Issue', cell: ({ getValue }) => {
        const v = getValue();
        return v ? (v.length > 50 ? v.slice(0, 50) + '…' : v) : '—';
      }},
      { accessorKey: 'serviceDate', header: 'Date', cell: ({ getValue }) => dayjs(getValue()).format('MMM DD, YYYY') },
      {
        accessorKey: 'cost',
        header: 'Cost',
        cell: ({ getValue }) => (getValue() != null ? `₹${Number(getValue()).toLocaleString()}` : '—'),
      },
      {
        accessorKey: 'status',
        header: 'Status',
        cell: ({ row }) => {
          const l = row.original;
          if (!canWrite) return <StatusBadge status={l.status} />;
          return (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <StatusBadge status={l.status} />
              <TextField
                select
                size="small"
                value=""
                onChange={(e) =>
                  setStatusDialog({ open: true, log: l, newStatus: e.target.value })
                }
                sx={{ minWidth: 40, '& .MuiSelect-select': { py: 0.3, fontSize: '0.75rem' } }}
                displayEmpty
              >
                <MenuItem value="" disabled>
                  <FaArrowsRotate size={12} />
                </MenuItem>
                {MAINTENANCE_STATUS.filter((s) => s !== l.status).map((s) => (
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
                  <Tooltip title="Delete">
                    <IconButton
                      size="small"
                      onClick={() => setDeleteDialog({ open: true, log: row.original })}
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

  if (loading && logs.length === 0) return <LoadingSpinner />;

  return (
    <Box>
      <PageHeader
        title="Maintenance"
        subtitle={`${logs.length} logs`}
        actionLabel={canWrite ? 'Add Log' : undefined}
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
          {MAINTENANCE_STATUS.map((s) => (
            <MenuItem key={s} value={s}>
              {s.replace(/_/g, ' ')}
            </MenuItem>
          ))}
        </TextField>
      </Box>

      <DataTable data={logs} columns={columns} searchPlaceholder="Search maintenance logs..." />

      {/* Create / Edit */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>
          {isEditing ? 'Edit Maintenance Log' : 'Add Maintenance Log'}
        </DialogTitle>
        <form onSubmit={handleSubmit(onSubmit)}>
          <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            {!isEditing && (
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
            )}
            <TextField
              label="Service Name"
              size="small"
              {...register('serviceName')}
              error={!!errors.serviceName}
              helperText={errors.serviceName?.message}
            />
            <TextField
              label="Issue Description"
              multiline
              rows={3}
              size="small"
              {...register('issueDescription')}
              error={!!errors.issueDescription}
              helperText={errors.issueDescription?.message}
            />
            <TextField
              label="Service Date *"
              type="date"
              size="small"
              {...register('serviceDate')}
              error={!!errors.serviceDate}
              helperText={errors.serviceDate?.message}
              slotProps={{ inputLabel: { shrink: true } }}
            />
            <TextField
              label="Cost"
              type="number"
              size="small"
              {...register('cost')}
              error={!!errors.cost}
              helperText={errors.cost?.message}
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

      <ConfirmDialog
        open={statusDialog.open}
        title="Change Status"
        message={`Change maintenance status to "${statusDialog.newStatus?.replace(/_/g, ' ')}"?`}
        confirmLabel="Confirm"
        confirmColor="primary"
        onConfirm={handleStatusChange}
        onCancel={() => setStatusDialog({ open: false, log: null, newStatus: '' })}
      />

      <ConfirmDialog
        open={deleteDialog.open}
        title="Delete Maintenance Log"
        message="Are you sure you want to delete this maintenance log?"
        confirmLabel="Delete"
        onConfirm={handleDelete}
        onCancel={() => setDeleteDialog({ open: false, log: null })}
        loading={deleting}
      />
    </Box>
  );
}

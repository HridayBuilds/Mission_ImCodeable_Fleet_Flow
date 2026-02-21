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
import { EXPENSE_STATUS, ROLE_PERMISSIONS } from '../../utils/constants';
import { CreateExpenseSchema, UpdateExpenseSchema } from '../../utils/validators';
import {
  getExpenses,
  createExpense,
  updateExpense,
  updateExpenseStatus,
  deleteExpense,
} from '../../api/expenseApi';
import { getTrips } from '../../api/tripApi';

export default function ExpensesPage() {
  const { user } = useAuth();
  const canWrite = ROLE_PERMISSIONS[user?.role]?.canWriteExpenses;

  const [expenses, setExpenses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState('');

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingExpense, setEditingExpense] = useState(null);
  const [saving, setSaving] = useState(false);
  const [trips, setTrips] = useState([]);

  const [statusDialog, setStatusDialog] = useState({ open: false, expense: null, newStatus: '' });
  const [deleteDialog, setDeleteDialog] = useState({ open: false, expense: null });
  const [deleting, setDeleting] = useState(false);

  const isEditing = !!editingExpense;
  const schema = isEditing ? UpdateExpenseSchema : CreateExpenseSchema;
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({ resolver: zodResolver(schema) });

  const fetchExpenses = async () => {
    setLoading(true);
    try {
      const params = {};
      if (filterStatus) params.status = filterStatus;
      const res = await getExpenses(params);
      setExpenses(res.data);
    } catch {
      toast.error('Failed to load expenses');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchExpenses();
  }, [filterStatus]);

  const openCreate = async () => {
    setEditingExpense(null);
    reset({ tripId: '', distance: '', fuelCost: '', miscExpense: '' });
    try {
      const res = await getTrips();
      setTrips(res.data);
    } catch {}
    setDialogOpen(true);
  };

  const openEdit = (expense) => {
    setEditingExpense(expense);
    reset({
      distance: expense.distance || '',
      fuelCost: expense.fuelCost || '',
      miscExpense: expense.miscExpense || '',
    });
    setDialogOpen(true);
  };

  const onSubmit = async (data) => {
    setSaving(true);
    try {
      if (isEditing) {
        await updateExpense(editingExpense.id, data);
        toast.success('Expense updated');
      } else {
        await createExpense(data);
        toast.success('Expense created');
      }
      setDialogOpen(false);
      fetchExpenses();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Operation failed');
    } finally {
      setSaving(false);
    }
  };

  const handleStatusChange = async () => {
    try {
      await updateExpenseStatus(statusDialog.expense.id, statusDialog.newStatus);
      toast.success('Status updated');
      setStatusDialog({ open: false, expense: null, newStatus: '' });
      fetchExpenses();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Status update failed');
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await deleteExpense(deleteDialog.expense.id);
      toast.success('Expense deleted');
      setDeleteDialog({ open: false, expense: null });
      fetchExpenses();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Delete failed');
    } finally {
      setDeleting(false);
    }
  };

  const columns = useMemo(
    () => [
      { accessorKey: 'tripNumber', header: 'Trip #', cell: ({ getValue }) => <strong>{getValue()}</strong> },
      { accessorKey: 'vehicleName', header: 'Vehicle', cell: ({ row }) => row.original.vehicleName || row.original.vehicleLicensePlate || '—' },
      { accessorKey: 'driverName', header: 'Driver', cell: ({ getValue }) => getValue() || '—' },
      {
        accessorKey: 'distance',
        header: 'Distance (km)',
        cell: ({ getValue }) => getValue()?.toLocaleString() ?? '—',
      },
      {
        accessorKey: 'fuelCost',
        header: 'Fuel Cost',
        cell: ({ getValue }) => (getValue() != null ? `₹${Number(getValue()).toLocaleString()}` : '—'),
      },
      {
        accessorKey: 'miscExpense',
        header: 'Misc.',
        cell: ({ getValue }) => (getValue() != null ? `₹${Number(getValue()).toLocaleString()}` : '—'),
      },
      {
        accessorKey: 'totalCost',
        header: 'Total',
        cell: ({ getValue }) => (
          <strong style={{ color: '#1e293b' }}>
            ₹{Number(getValue()).toLocaleString()}
          </strong>
        ),
      },
      {
        accessorKey: 'status',
        header: 'Status',
        cell: ({ row }) => {
          const e = row.original;
          if (!canWrite) return <StatusBadge status={e.status} />;
          return (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <StatusBadge status={e.status} />
              <TextField
                select
                size="small"
                value=""
                onChange={(ev) =>
                  setStatusDialog({ open: true, expense: e, newStatus: ev.target.value })
                }
                sx={{ minWidth: 40, '& .MuiSelect-select': { py: 0.3, fontSize: '0.75rem' } }}
                displayEmpty
              >
                <MenuItem value="" disabled>
                  <FaArrowsRotate size={12} />
                </MenuItem>
                {EXPENSE_STATUS.filter((s) => s !== e.status).map((s) => (
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
                      onClick={() => setDeleteDialog({ open: true, expense: row.original })}
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

  if (loading && expenses.length === 0) return <LoadingSpinner />;

  return (
    <Box>
      <PageHeader
        title="Expenses"
        subtitle={`${expenses.length} expenses`}
        actionLabel={canWrite ? 'Add Expense' : undefined}
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
          {EXPENSE_STATUS.map((s) => (
            <MenuItem key={s} value={s}>
              {s.replace(/_/g, ' ')}
            </MenuItem>
          ))}
        </TextField>
      </Box>

      <DataTable data={expenses} columns={columns} searchPlaceholder="Search expenses..." />

      {/* Create / Edit */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>
          {isEditing ? 'Edit Expense' : 'Add Expense'}
        </DialogTitle>
        <form onSubmit={handleSubmit(onSubmit)}>
          <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            {!isEditing && (
              <TextField
                label="Trip *"
                select
                size="small"
                defaultValue=""
                {...register('tripId')}
                error={!!errors.tripId}
                helperText={errors.tripId?.message}
              >
                {trips.map((t) => (
                  <MenuItem key={t.id} value={t.id}>
                    #{t.tripNumber} — {t.origin} → {t.destination}
                  </MenuItem>
                ))}
              </TextField>
            )}
            <TextField
              label="Distance (km)"
              type="number"
              size="small"
              {...register('distance')}
              error={!!errors.distance}
              helperText={errors.distance?.message}
            />
            <TextField
              label="Fuel Cost *"
              type="number"
              size="small"
              {...register('fuelCost')}
              error={!!errors.fuelCost}
              helperText={errors.fuelCost?.message}
            />
            <TextField
              label="Miscellaneous Expense"
              type="number"
              size="small"
              {...register('miscExpense')}
              error={!!errors.miscExpense}
              helperText={errors.miscExpense?.message}
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
        title="Change Expense Status"
        message={`Change status to "${statusDialog.newStatus?.replace(/_/g, ' ')}"?`}
        confirmLabel="Confirm"
        confirmColor="primary"
        onConfirm={handleStatusChange}
        onCancel={() => setStatusDialog({ open: false, expense: null, newStatus: '' })}
      />

      <ConfirmDialog
        open={deleteDialog.open}
        title="Delete Expense"
        message="Are you sure you want to delete this expense?"
        confirmLabel="Delete"
        onConfirm={handleDelete}
        onCancel={() => setDeleteDialog({ open: false, expense: null })}
        loading={deleting}
      />
    </Box>
  );
}

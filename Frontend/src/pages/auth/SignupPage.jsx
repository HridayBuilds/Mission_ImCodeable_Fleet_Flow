import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Box,
  TextField,
  Button,
  Typography,
  Paper,
  MenuItem,
  IconButton,
  InputAdornment,
} from '@mui/material';
import { FaEye, FaEyeSlash } from 'react-icons/fa6';
import toast from 'react-hot-toast';
import { signup } from '../../api/authApi';
import { SignupSchema } from '../../utils/validators';
import { ROLES, ROLE_LABELS } from '../../utils/constants';

export default function SignupPage() {
  const navigate = useNavigate();
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(SignupSchema) });

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      const res = await signup(data);
      toast.success(res.data?.message || 'Account created! Please check your email to verify.');
      navigate('/login');
    } catch (err) {
      const msg =
        err.response?.data?.message || err.response?.data?.error || 'Signup failed';
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #eff6ff 0%, #f8fafc 50%, #ede9fe 100%)',
        p: 2,
      }}
    >
      <Paper
        elevation={0}
        sx={{
          width: '100%',
          maxWidth: 480,
          p: 5,
          borderRadius: '16px',
          border: '1px solid #e2e8f0',
        }}
      >
        <Box sx={{ textAlign: 'center', mb: 4 }}>
          <img
            src="/fleet-flow-logo.svg"
            alt="Fleet Flow"
            style={{ width: 48, height: 48, marginBottom: 12 }}
          />
          <Typography variant="h5" sx={{ fontWeight: 700, color: '#1e293b' }}>
            Create account
          </Typography>
          <Typography variant="body2" sx={{ color: '#64748b', mt: 0.5 }}>
            Join Fleet Flow to manage your fleet
          </Typography>
        </Box>

        <form onSubmit={handleSubmit(onSubmit)}>
          <TextField
            label="Full Name"
            fullWidth
            {...register('name')}
            error={!!errors.name}
            helperText={errors.name?.message}
            sx={{ mb: 2 }}
            size="small"
          />

          <TextField
            label="Email"
            fullWidth
            {...register('email')}
            error={!!errors.email}
            helperText={errors.email?.message}
            sx={{ mb: 2 }}
            size="small"
          />

          <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
            <TextField
              label="Employee ID"
              fullWidth
              {...register('employeeId')}
              error={!!errors.employeeId}
              helperText={errors.employeeId?.message}
              size="small"
            />
            <TextField
              label="Phone"
              fullWidth
              {...register('phone')}
              error={!!errors.phone}
              helperText={errors.phone?.message}
              size="small"
            />
          </Box>

          <TextField
            label="Role"
            select
            fullWidth
            defaultValue=""
            {...register('role')}
            error={!!errors.role}
            helperText={errors.role?.message}
            sx={{ mb: 2 }}
            size="small"
          >
            {Object.entries(ROLES).map(([key, value]) => (
              <MenuItem key={key} value={value}>
                {ROLE_LABELS[key]}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            label="Password"
            type={showPw ? 'text' : 'password'}
            fullWidth
            {...register('password')}
            error={!!errors.password}
            helperText={errors.password?.message}
            sx={{ mb: 3 }}
            size="small"
            slotProps={{
              input: {
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton size="small" onClick={() => setShowPw(!showPw)}>
                      {showPw ? <FaEyeSlash size={16} /> : <FaEye size={16} />}
                    </IconButton>
                  </InputAdornment>
                ),
              },
            }}
          />

          <Button
            type="submit"
            variant="contained"
            fullWidth
            disabled={loading}
            sx={{
              textTransform: 'none',
              borderRadius: '10px',
              py: 1.3,
              fontWeight: 600,
              fontSize: '0.95rem',
              backgroundColor: '#2563eb',
              '&:hover': { backgroundColor: '#1d4ed8' },
            }}
          >
            {loading ? 'Creating account...' : 'Create Account'}
          </Button>
        </form>

        <Typography
          variant="body2"
          sx={{ textAlign: 'center', mt: 3, color: '#64748b' }}
        >
          Already have an account?{' '}
          <Link to="/login" style={{ color: '#2563eb', fontWeight: 600, textDecoration: 'none' }}>
            Sign In
          </Link>
        </Typography>
      </Paper>
    </Box>
  );
}

import { useState, useEffect } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Box,
  TextField,
  Button,
  Typography,
  Paper,
  IconButton,
  InputAdornment,
} from '@mui/material';
import { FaEye, FaEyeSlash } from 'react-icons/fa6';
import toast from 'react-hot-toast';
import { resetPassword, validateResetToken } from '../../api/authApi';
import { ResetPasswordSchema } from '../../utils/validators';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token') || '';
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);
  const [validating, setValidating] = useState(true);
  const [tokenValid, setTokenValid] = useState(false);

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(ResetPasswordSchema),
    defaultValues: { token },
  });

  useEffect(() => {
    const validate = async () => {
      if (!token) {
        setValidating(false);
        return;
      }
      try {
        await validateResetToken(token);
        setTokenValid(true);
        setValue('token', token);
      } catch {
        setTokenValid(false);
      } finally {
        setValidating(false);
      }
    };
    validate();
  }, [token, setValue]);

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      await resetPassword({ token: data.token, newPassword: data.newPassword });
      toast.success('Password reset successful! Please log in.');
      navigate('/login');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to reset password');
    } finally {
      setLoading(false);
    }
  };

  if (validating) return <LoadingSpinner fullScreen message="Validating token..." />;

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
          maxWidth: 440,
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
            Reset password
          </Typography>
        </Box>

        {!tokenValid ? (
          <Box sx={{ textAlign: 'center' }}>
            <Typography variant="body1" sx={{ color: '#991b1b', mb: 2 }}>
              Invalid or expired reset link.
            </Typography>
            <Link
              to="/forgot-password"
              style={{ color: '#2563eb', fontWeight: 600, textDecoration: 'none' }}
            >
              Request a new link
            </Link>
          </Box>
        ) : (
          <form onSubmit={handleSubmit(onSubmit)}>
            <input type="hidden" {...register('token')} />

            <TextField
              label="New Password"
              type={showPw ? 'text' : 'password'}
              fullWidth
              {...register('newPassword')}
              error={!!errors.newPassword}
              helperText={errors.newPassword?.message}
              sx={{ mb: 2 }}
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

            <TextField
              label="Confirm Password"
              type="password"
              fullWidth
              {...register('confirmPassword')}
              error={!!errors.confirmPassword}
              helperText={errors.confirmPassword?.message}
              sx={{ mb: 3 }}
              size="small"
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
                backgroundColor: '#2563eb',
                '&:hover': { backgroundColor: '#1d4ed8' },
              }}
            >
              {loading ? 'Resetting...' : 'Reset Password'}
            </Button>
          </form>
        )}
      </Paper>
    </Box>
  );
}

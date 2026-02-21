import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Box, TextField, Button, Typography, Paper } from '@mui/material';
import toast from 'react-hot-toast';
import { forgotPassword } from '../../api/authApi';
import { ForgotPasswordSchema } from '../../utils/validators';

export default function ForgotPasswordPage() {
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(ForgotPasswordSchema) });

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      await forgotPassword(data);
      setSent(true);
      toast.success('Password reset link sent to your email!');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to send reset link');
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
            Forgot password?
          </Typography>
          <Typography variant="body2" sx={{ color: '#64748b', mt: 0.5 }}>
            Enter your email and we'll send you a reset link
          </Typography>
        </Box>

        {sent ? (
          <Box sx={{ textAlign: 'center' }}>
            <Typography variant="body1" sx={{ color: '#166534', mb: 2 }}>
              ✓ Check your email for the reset link.
            </Typography>
            <Link to="/login" style={{ color: '#2563eb', fontWeight: 600, textDecoration: 'none' }}>
              Back to Sign In
            </Link>
          </Box>
        ) : (
          <form onSubmit={handleSubmit(onSubmit)}>
            <TextField
              label="Email"
              fullWidth
              {...register('email')}
              error={!!errors.email}
              helperText={errors.email?.message}
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
              {loading ? 'Sending...' : 'Send Reset Link'}
            </Button>

            <Typography
              variant="body2"
              sx={{ textAlign: 'center', mt: 3, color: '#64748b' }}
            >
              <Link to="/login" style={{ color: '#2563eb', fontWeight: 600, textDecoration: 'none' }}>
                Back to Sign In
              </Link>
            </Typography>
          </form>
        )}
      </Paper>
    </Box>
  );
}

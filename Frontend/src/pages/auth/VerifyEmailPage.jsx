import { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { Box, Typography, Paper } from '@mui/material';
import { verifyEmail } from '../../api/authApi';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';
  const [status, setStatus] = useState('loading'); // loading | success | error
  const [message, setMessage] = useState('');

  useEffect(() => {
    const verify = async () => {
      if (!token) {
        setStatus('error');
        setMessage('No verification token provided.');
        return;
      }
      try {
        const res = await verifyEmail(token);
        setStatus('success');
        setMessage(res.data?.message || 'Email verified successfully!');
      } catch (err) {
        setStatus('error');
        setMessage(err.response?.data?.message || 'Verification failed.');
      }
    };
    verify();
  }, [token]);

  if (status === 'loading') {
    return <LoadingSpinner fullScreen message="Verifying your email..." />;
  }

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
          textAlign: 'center',
        }}
      >
        <img
          src="/fleet-flow-logo.svg"
          alt="Fleet Flow"
          style={{ width: 48, height: 48, marginBottom: 16 }}
        />

        {status === 'success' ? (
          <>
            <Typography variant="h5" sx={{ fontWeight: 700, color: '#166534', mb: 1 }}>
              ✓ Email Verified
            </Typography>
            <Typography variant="body2" sx={{ color: '#64748b', mb: 3 }}>
              {message}
            </Typography>
          </>
        ) : (
          <>
            <Typography variant="h5" sx={{ fontWeight: 700, color: '#991b1b', mb: 1 }}>
              Verification Failed
            </Typography>
            <Typography variant="body2" sx={{ color: '#64748b', mb: 3 }}>
              {message}
            </Typography>
          </>
        )}

        <Link
          to="/login"
          style={{
            color: '#2563eb',
            fontWeight: 600,
            textDecoration: 'none',
            fontSize: '0.95rem',
          }}
        >
          Go to Sign In
        </Link>
      </Paper>
    </Box>
  );
}

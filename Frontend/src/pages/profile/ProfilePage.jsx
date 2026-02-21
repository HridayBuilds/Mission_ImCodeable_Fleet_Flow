import { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  TextField,
  Button,
  Avatar,
  Chip,
  Divider,
  IconButton,
  InputAdornment,
  Grid,
} from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { FaEye, FaEyeSlash } from 'react-icons/fa6';
import toast from 'react-hot-toast';
import dayjs from 'dayjs';

import { useAuth } from '../../hooks/useAuth';
import { ROLE_LABELS } from '../../utils/constants';
import { UpdateProfileSchema, ChangePasswordSchema } from '../../utils/validators';
import { getMe, updateProfile, changePassword } from '../../api/userApi';

export default function ProfilePage() {
  const { user, updateUser } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loadingProfile, setLoadingProfile] = useState(true);
  const [showPw, setShowPw] = useState(false);
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);

  const profileForm = useForm({
    resolver: zodResolver(UpdateProfileSchema),
    defaultValues: { name: '', phone: '' },
  });

  const passwordForm = useForm({
    resolver: zodResolver(ChangePasswordSchema),
    defaultValues: { currentPassword: '', newPassword: '', confirmPassword: '' },
  });

  // Fetch full profile from /users/me on mount (includes phone, createdAt, isActive)
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await getMe();
        const data = res.data;
        setProfile(data);
        updateUser(data); // sync context so sidebar/header also gets full data
        profileForm.reset({ name: data.name || '', phone: data.phone || '' });
      } catch {
        // fallback to context user
        if (user) {
          setProfile(user);
          profileForm.reset({ name: user.name || '', phone: user.phone || '' });
        }
      } finally {
        setLoadingProfile(false);
      }
    };
    fetchProfile();
  }, []);

  const onProfileSubmit = async (data) => {
    setSavingProfile(true);
    try {
      const cleaned = Object.fromEntries(
        Object.entries(data).filter(([, v]) => v !== ''),
      );
      const res = await updateProfile(cleaned);
      const updated = res.data;
      setProfile(updated);
      updateUser(updated);
      toast.success('Profile updated');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update profile');
    } finally {
      setSavingProfile(false);
    }
  };

  const onPasswordSubmit = async (data) => {
    setSavingPassword(true);
    try {
      await changePassword({
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
      });
      toast.success('Password changed successfully');
      passwordForm.reset();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to change password');
    } finally {
      setSavingPassword(false);
    }
  };

  const displayUser = profile || user;

  const initials = displayUser?.name
    ? displayUser.name
        .split(' ')
        .map((n) => n[0])
        .join('')
        .toUpperCase()
        .slice(0, 2)
    : 'U';

  if (loadingProfile) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', pt: 10 }}>
        <Typography variant="body2" sx={{ color: '#94a3b8' }}>Loading profile...</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ maxWidth: 640, mx: 'auto' }}>
      <Typography variant="h5" sx={{ fontWeight: 700, color: '#1e293b', mb: 3 }}>
        Profile
      </Typography>

      {/* Profile info */}
      <Paper
        elevation={0}
        sx={{ p: 4, borderRadius: '14px', border: '1px solid #e2e8f0', mb: 3 }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
          <Avatar
            sx={{
              width: 64,
              height: 64,
              fontSize: '1.5rem',
              fontWeight: 700,
              backgroundColor: '#2563eb',
            }}
          >
            {initials}
          </Avatar>
          <Box sx={{ flex: 1 }}>
            <Typography variant="h6" sx={{ fontWeight: 600, color: '#1e293b' }}>
              {displayUser?.name}
            </Typography>
            <Typography variant="body2" sx={{ color: '#64748b' }}>
              {ROLE_LABELS[displayUser?.role] || displayUser?.role}
            </Typography>
          </Box>
          {displayUser?.isActive !== undefined && (
            <Chip
              label={displayUser.isActive ? 'Active' : 'Inactive'}
              size="small"
              sx={{
                fontWeight: 600,
                fontSize: '0.7rem',
                backgroundColor: displayUser.isActive ? '#dcfce7' : '#fee2e2',
                color: displayUser.isActive ? '#16a34a' : '#ef4444',
              }}
            />
          )}
        </Box>

        {/* Details grid */}
        <Grid container spacing={2} sx={{ mb: 3 }}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="caption" sx={{ color: '#94a3b8', display: 'block' }}>
              Email
            </Typography>
            <Typography variant="body2" sx={{ fontWeight: 500, color: '#1e293b' }}>
              {displayUser?.email || '—'}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="caption" sx={{ color: '#94a3b8', display: 'block' }}>
              Employee ID
            </Typography>
            <Typography variant="body2" sx={{ fontWeight: 500, color: '#1e293b' }}>
              {displayUser?.employeeId || '—'}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="caption" sx={{ color: '#94a3b8', display: 'block' }}>
              Phone
            </Typography>
            <Typography variant="body2" sx={{ fontWeight: 500, color: '#1e293b' }}>
              {displayUser?.phone || 'Not set'}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="caption" sx={{ color: '#94a3b8', display: 'block' }}>
              Member Since
            </Typography>
            <Typography variant="body2" sx={{ fontWeight: 500, color: '#1e293b' }}>
              {displayUser?.createdAt
                ? dayjs(displayUser.createdAt).format('MMM D, YYYY')
                : '—'}
            </Typography>
          </Grid>
        </Grid>

        <Typography variant="subtitle2" sx={{ fontWeight: 600, mb: 2, color: '#1e293b' }}>
          Update Profile
        </Typography>
        <form onSubmit={profileForm.handleSubmit(onProfileSubmit)}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              label="Full Name"
              size="small"
              {...profileForm.register('name')}
              error={!!profileForm.formState.errors.name}
              helperText={profileForm.formState.errors.name?.message}
            />
            <TextField
              label="Phone"
              size="small"
              {...profileForm.register('phone')}
              error={!!profileForm.formState.errors.phone}
              helperText={profileForm.formState.errors.phone?.message}
            />
            <Box sx={{ textAlign: 'right' }}>
              <Button
                type="submit"
                variant="contained"
                disabled={savingProfile}
                sx={{
                  textTransform: 'none',
                  borderRadius: '8px',
                  px: 3,
                  backgroundColor: '#2563eb',
                  '&:hover': { backgroundColor: '#1d4ed8' },
                }}
              >
                {savingProfile ? 'Saving...' : 'Save Changes'}
              </Button>
            </Box>
          </Box>
        </form>
      </Paper>

      {/* Change password */}
      <Paper
        elevation={0}
        sx={{ p: 4, borderRadius: '14px', border: '1px solid #e2e8f0' }}
      >
        <Typography variant="subtitle2" sx={{ fontWeight: 600, mb: 2, color: '#1e293b' }}>
          Change Password
        </Typography>
        <form onSubmit={passwordForm.handleSubmit(onPasswordSubmit)}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              label="Current Password"
              type={showPw ? 'text' : 'password'}
              size="small"
              {...passwordForm.register('currentPassword')}
              error={!!passwordForm.formState.errors.currentPassword}
              helperText={passwordForm.formState.errors.currentPassword?.message}
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
              label="New Password"
              type="password"
              size="small"
              {...passwordForm.register('newPassword')}
              error={!!passwordForm.formState.errors.newPassword}
              helperText={passwordForm.formState.errors.newPassword?.message}
            />
            <TextField
              label="Confirm New Password"
              type="password"
              size="small"
              {...passwordForm.register('confirmPassword')}
              error={!!passwordForm.formState.errors.confirmPassword}
              helperText={passwordForm.formState.errors.confirmPassword?.message}
            />
            <Box sx={{ textAlign: 'right' }}>
              <Button
                type="submit"
                variant="contained"
                disabled={savingPassword}
                sx={{
                  textTransform: 'none',
                  borderRadius: '8px',
                  px: 3,
                  backgroundColor: '#2563eb',
                  '&:hover': { backgroundColor: '#1d4ed8' },
                }}
              >
                {savingPassword ? 'Changing...' : 'Change Password'}
              </Button>
            </Box>
          </Box>
        </form>
      </Paper>
    </Box>
  );
}

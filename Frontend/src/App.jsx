import { Routes, Route, Navigate } from 'react-router-dom';

import PublicRoute from './components/auth/PublicRoute';
import ProtectedRoute from './components/auth/ProtectedRoute';
import DashboardLayout from './components/layout/DashboardLayout';

// Auth pages
import LoginPage from './pages/auth/LoginPage';
import SignupPage from './pages/auth/SignupPage';
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage';
import ResetPasswordPage from './pages/auth/ResetPasswordPage';
import VerifyEmailPage from './pages/auth/VerifyEmailPage';

// Feature pages
import DashboardPage from './pages/dashboard/DashboardPage';
import VehiclesPage from './pages/vehicles/VehiclesPage';
import DriversPage from './pages/drivers/DriversPage';
import TripsPage from './pages/trips/TripsPage';
import MaintenancePage from './pages/maintenance/MaintenancePage';
import ExpensesPage from './pages/expenses/ExpensesPage';
import FuelLogsPage from './pages/fuelLogs/FuelLogsPage';
import AnalyticsPage from './pages/analytics/AnalyticsPage';
import ProfilePage from './pages/profile/ProfilePage';

const ALL_ROLES = ['FLEET_MANAGER', 'DISPATCHER', 'SAFETY_OFFICER', 'FINANCIAL_ANALYST'];

export default function App() {
  return (
    <Routes>
      {/* ── Public routes ───────────────── */}
      <Route path="/login" element={<PublicRoute><LoginPage /></PublicRoute>} />
      <Route path="/signup" element={<PublicRoute><SignupPage /></PublicRoute>} />
      <Route path="/forgot-password" element={<PublicRoute><ForgotPasswordPage /></PublicRoute>} />
      <Route path="/reset-password" element={<PublicRoute><ResetPasswordPage /></PublicRoute>} />
      <Route path="/verify-email" element={<VerifyEmailPage />} />

      {/* ── Protected routes (dashboard shell) ── */}
      <Route
        element={
          <ProtectedRoute allowedRoles={ALL_ROLES}>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/vehicles" element={
          <ProtectedRoute allowedRoles={['FLEET_MANAGER', 'DISPATCHER', 'SAFETY_OFFICER']}>
            <VehiclesPage />
          </ProtectedRoute>
        } />
        <Route path="/drivers" element={
          <ProtectedRoute allowedRoles={['FLEET_MANAGER', 'DISPATCHER', 'SAFETY_OFFICER']}>
            <DriversPage />
          </ProtectedRoute>
        } />
        <Route path="/trips" element={
          <ProtectedRoute allowedRoles={['FLEET_MANAGER', 'DISPATCHER']}>
            <TripsPage />
          </ProtectedRoute>
        } />
        <Route path="/maintenance" element={
          <ProtectedRoute allowedRoles={['FLEET_MANAGER', 'SAFETY_OFFICER', 'FINANCIAL_ANALYST']}>
            <MaintenancePage />
          </ProtectedRoute>
        } />
        <Route path="/expenses" element={
          <ProtectedRoute allowedRoles={['FLEET_MANAGER', 'FINANCIAL_ANALYST']}>
            <ExpensesPage />
          </ProtectedRoute>
        } />
        <Route path="/fuel-logs" element={
          <ProtectedRoute allowedRoles={['FLEET_MANAGER', 'FINANCIAL_ANALYST']}>
            <FuelLogsPage />
          </ProtectedRoute>
        } />
        <Route path="/analytics" element={
          <ProtectedRoute allowedRoles={['FLEET_MANAGER', 'FINANCIAL_ANALYST']}>
            <AnalyticsPage />
          </ProtectedRoute>
        } />
        <Route path="/profile" element={<ProfilePage />} />
      </Route>

      {/* ── Fallback ── */}
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

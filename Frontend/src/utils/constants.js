/* ── Enum values (mirrors backend enums) ──────────────────── */

export const ROLES = {
  FLEET_MANAGER: 'FLEET_MANAGER',
  DISPATCHER: 'DISPATCHER',
  SAFETY_OFFICER: 'SAFETY_OFFICER',
  FINANCIAL_ANALYST: 'FINANCIAL_ANALYST',
};

export const ROLE_LABELS = {
  FLEET_MANAGER: 'Fleet Manager',
  DISPATCHER: 'Dispatcher',
  SAFETY_OFFICER: 'Safety Officer',
  FINANCIAL_ANALYST: 'Financial Analyst',
};

export const VEHICLE_TYPES = ['TRUCK', 'VAN', 'BIKE', 'MINI', 'TRAILER_TRUCK'];

export const VEHICLE_STATUS = ['AVAILABLE', 'ON_TRIP', 'IN_SHOP', 'RETIRED'];

export const DRIVER_STATUS = ['ON_DUTY', 'OFF_DUTY', 'ON_TRIP', 'SUSPENDED'];

export const TRIP_STATUS = ['DRAFT', 'DISPATCHED', 'IN_TRANSIT', 'COMPLETED', 'CANCELLED'];

export const MAINTENANCE_STATUS = ['NEW', 'IN_PROGRESS', 'RESOLVED'];

export const EXPENSE_STATUS = ['PENDING', 'APPROVED', 'DONE'];

/* ── Status → pill colour mapping ─────────────────────────── */

export const STATUS_COLORS = {
  // Vehicle
  AVAILABLE: { bg: '#dcfce7', text: '#166534' },
  ON_TRIP: { bg: '#dbeafe', text: '#1e40af' },
  IN_SHOP: { bg: '#fef9c3', text: '#854d0e' },
  RETIRED: { bg: '#f3f4f6', text: '#6b7280' },

  // Driver
  ON_DUTY: { bg: '#dcfce7', text: '#166534' },
  OFF_DUTY: { bg: '#f3f4f6', text: '#6b7280' },
  SUSPENDED: { bg: '#fee2e2', text: '#991b1b' },

  // Trip
  DRAFT: { bg: '#f3f4f6', text: '#6b7280' },
  DISPATCHED: { bg: '#dbeafe', text: '#1e40af' },
  IN_TRANSIT: { bg: '#fef9c3', text: '#854d0e' },
  COMPLETED: { bg: '#dcfce7', text: '#166534' },
  CANCELLED: { bg: '#fee2e2', text: '#991b1b' },

  // Maintenance
  NEW: { bg: '#dbeafe', text: '#1e40af' },
  IN_PROGRESS: { bg: '#fef9c3', text: '#854d0e' },
  RESOLVED: { bg: '#dcfce7', text: '#166534' },

  // Expense
  PENDING: { bg: '#fef9c3', text: '#854d0e' },
  APPROVED: { bg: '#dcfce7', text: '#166534' },
  DONE: { bg: '#f3f4f6', text: '#6b7280' },
};

/* ── Sidebar navigation config per role ───────────────────── */

export const NAV_ITEMS = [
  { label: 'Dashboard', path: '/dashboard', icon: 'dashboard', roles: ['FLEET_MANAGER', 'DISPATCHER', 'SAFETY_OFFICER', 'FINANCIAL_ANALYST'] },
  { label: 'Vehicles', path: '/vehicles', icon: 'vehicles', roles: ['FLEET_MANAGER', 'DISPATCHER', 'SAFETY_OFFICER'] },
  { label: 'Drivers', path: '/drivers', icon: 'drivers', roles: ['FLEET_MANAGER', 'DISPATCHER', 'SAFETY_OFFICER'] },
  { label: 'Trips', path: '/trips', icon: 'trips', roles: ['FLEET_MANAGER', 'DISPATCHER'] },
  { label: 'Maintenance', path: '/maintenance', icon: 'maintenance', roles: ['FLEET_MANAGER', 'SAFETY_OFFICER', 'FINANCIAL_ANALYST'] },
  { label: 'Expenses', path: '/expenses', icon: 'expenses', roles: ['FLEET_MANAGER', 'FINANCIAL_ANALYST'] },
  { label: 'Fuel Logs', path: '/fuel-logs', icon: 'fuel', roles: ['FLEET_MANAGER', 'FINANCIAL_ANALYST'] },
  { label: 'Analytics', path: '/analytics', icon: 'analytics', roles: ['FLEET_MANAGER', 'FINANCIAL_ANALYST'] },
  { label: 'Profile', path: '/profile', icon: 'profile', roles: ['FLEET_MANAGER', 'DISPATCHER', 'SAFETY_OFFICER', 'FINANCIAL_ANALYST'] },
];

/* ── Role → writable resources (for showing add/edit buttons) ─ */

export const ROLE_PERMISSIONS = {
  FLEET_MANAGER: {
    canWriteVehicles: true,
    canWriteDrivers: false,
    canWriteTrips: false,
    canWriteMaintenance: true,
    canWriteExpenses: false,
    canWriteFuelLogs: false,
    canWriteAnalytics: false,
  },
  DISPATCHER: {
    canWriteVehicles: false,
    canWriteDrivers: false,
    canWriteTrips: true,
    canWriteMaintenance: false,
    canWriteExpenses: false,
    canWriteFuelLogs: false,
    canWriteAnalytics: false,
  },
  SAFETY_OFFICER: {
    canWriteVehicles: false,
    canWriteDrivers: true,
    canWriteTrips: false,
    canWriteMaintenance: false,
    canWriteExpenses: false,
    canWriteFuelLogs: false,
    canWriteAnalytics: false,
  },
  FINANCIAL_ANALYST: {
    canWriteVehicles: false,
    canWriteDrivers: false,
    canWriteTrips: false,
    canWriteMaintenance: false,
    canWriteExpenses: true,
    canWriteFuelLogs: true,
    canWriteAnalytics: true,
  },
};

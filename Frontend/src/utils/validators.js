import { z } from 'zod';

const passwordSchema = z
  .string()
  .min(8, 'Password must be at least 8 characters')
  .max(100)
  .regex(/[0-9]/, 'Must contain a digit')
  .regex(/[a-z]/, 'Must contain a lowercase letter')
  .regex(/[A-Z]/, 'Must contain an uppercase letter')
  .regex(/[!@#$%^&*(),.?":{}|<>]/, 'Must contain a special character');

const emailSchema = z
  .string()
  .min(1, 'Email is required')
  .email('Invalid email address');

const phoneRegex = /^[+]?[\d\s\-().]{7,20}$/;

/* ── Auth schemas ─────────────────────────────────────────── */

export const SignupSchema = z.object({
  name: z.string().min(2, 'Min 2 characters').max(100),
  role: z.string().min(1, 'Role is required'),
  employeeId: z.string().min(1, 'Employee ID is required').max(50),
  phone: z.string().regex(phoneRegex, 'Invalid phone number'),
  email: emailSchema,
  password: passwordSchema,
});

export const LoginSchema = z.object({
  email: emailSchema,
  password: z.string().min(1, 'Password is required'),
});

export const ForgotPasswordSchema = z.object({
  email: emailSchema,
});

export const ResetPasswordSchema = z.object({
  token: z.string().min(1),
  newPassword: passwordSchema,
  confirmPassword: z.string().min(1, 'Please confirm password'),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: 'Passwords do not match',
  path: ['confirmPassword'],
});

export const ChangePasswordSchema = z.object({
  currentPassword: z.string().min(1, 'Current password is required'),
  newPassword: passwordSchema,
  confirmPassword: z.string().min(1, 'Please confirm password'),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: 'Passwords do not match',
  path: ['confirmPassword'],
});

export const UpdateProfileSchema = z.object({
  name: z.string().min(2, 'Min 2 characters').max(100).optional().or(z.literal('')),
  phone: z.string().regex(phoneRegex, 'Invalid phone').optional().or(z.literal('')),
});

/* ── Vehicle schemas ──────────────────────────────────────── */

export const CreateVehicleSchema = z.object({
  licensePlate: z.string().min(1, 'License plate is required').max(30),
  name: z.string().max(100).optional().or(z.literal('')),
  model: z.string().max(100).optional().or(z.literal('')),
  type: z.string().min(1, 'Vehicle type is required'),
  maxLoadCapacity: z.coerce.number().positive('Must be positive'),
  odometer: z.coerce.number().min(0, 'Cannot be negative').optional(),
  acquisitionCost: z.coerce.number().min(0, 'Cannot be negative').optional(),
});

export const UpdateVehicleSchema = z.object({
  name: z.string().max(100).optional().or(z.literal('')),
  model: z.string().max(100).optional().or(z.literal('')),
  type: z.string().optional(),
  maxLoadCapacity: z.coerce.number().min(0).optional(),
  acquisitionCost: z.coerce.number().min(0).optional(),
});

/* ── Driver schemas ───────────────────────────────────────── */

export const CreateDriverSchema = z.object({
  name: z.string().min(2, 'Min 2 characters').max(100),
  licenseNumber: z.string().min(1, 'License number is required').max(50),
  licenseExpiryDate: z.string().min(1, 'Expiry date is required'),
  licenseCategory: z.string().max(30).optional().or(z.literal('')),
  phone: z.string().regex(phoneRegex, 'Invalid phone').optional().or(z.literal('')),
});

export const UpdateDriverSchema = z.object({
  name: z.string().min(2).max(100).optional().or(z.literal('')),
  licenseExpiryDate: z.string().optional().or(z.literal('')),
  licenseCategory: z.string().max(30).optional().or(z.literal('')),
  phone: z.string().regex(phoneRegex, 'Invalid phone').optional().or(z.literal('')),
});

/* ── Trip schemas ─────────────────────────────────────────── */

export const CreateTripSchema = z.object({
  vehicleId: z.string().min(1, 'Vehicle is required'),
  driverId: z.string().min(1, 'Driver is required'),
  cargoWeight: z.coerce.number().positive('Must be positive'),
  origin: z.string().min(1, 'Origin is required').max(255),
  destination: z.string().min(1, 'Destination is required').max(255),
  estimatedFuelCost: z.coerce.number().min(0).optional(),
});

export const CompleteTripSchema = z.object({
  endOdometer: z.coerce.number().positive('Must be positive'),
  revenue: z.coerce.number().min(0, 'Cannot be negative').optional(),
});

export const CancelTripSchema = z.object({
  cancellationReason: z.string().max(500).optional().or(z.literal('')),
});

/* ── Maintenance schemas ──────────────────────────────────── */

export const CreateMaintenanceSchema = z.object({
  vehicleId: z.string().min(1, 'Vehicle is required'),
  serviceName: z.string().max(200).optional().or(z.literal('')),
  issueDescription: z.string().max(1000).optional().or(z.literal('')),
  serviceDate: z.string().min(1, 'Service date is required'),
  cost: z.coerce.number().min(0, 'Cannot be negative').optional(),
});

export const UpdateMaintenanceSchema = z.object({
  serviceName: z.string().max(200).optional().or(z.literal('')),
  issueDescription: z.string().max(1000).optional().or(z.literal('')),
  serviceDate: z.string().optional().or(z.literal('')),
  cost: z.coerce.number().min(0).optional(),
});

/* ── Expense schemas ──────────────────────────────────────── */

export const CreateExpenseSchema = z.object({
  tripId: z.string().min(1, 'Trip is required'),
  distance: z.coerce.number().min(0).optional(),
  fuelCost: z.coerce.number().min(0, 'Fuel cost is required'),
  miscExpense: z.coerce.number().min(0).optional(),
});

export const UpdateExpenseSchema = z.object({
  distance: z.coerce.number().min(0).optional(),
  fuelCost: z.coerce.number().min(0).optional(),
  miscExpense: z.coerce.number().min(0).optional(),
});

/* ── Fuel Log schemas ─────────────────────────────────────── */

export const CreateFuelLogSchema = z.object({
  vehicleId: z.string().min(1, 'Vehicle is required'),
  tripId: z.string().optional().or(z.literal('')),
  liters: z.coerce.number().positive('Must be positive'),
  cost: z.coerce.number().min(0, 'Cost is required'),
  odometerAtFill: z.coerce.number().min(0).optional(),
  fillDate: z.string().min(1, 'Fill date is required'),
});

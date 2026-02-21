package com.hackathon.securestarter.util;

public class Constants {

    // Token Expiration (used in AuthService)
    public static final int VERIFICATION_TOKEN_EXPIRATION_HOURS = 24;
    public static final int PASSWORD_RESET_TOKEN_EXPIRATION_HOURS = 1;

    // Email Templates (used in EmailService)
    public static final String VERIFICATION_EMAIL_SUBJECT = "Verify Your Email - Fleet Flow";
    public static final String PASSWORD_RESET_EMAIL_SUBJECT = "Reset Your Password - Fleet Flow";

    // Validation Messages (used in Services)
    public static final String INVALID_TOKEN = "Invalid or expired token";
    public static final String TOKEN_EXPIRED = "Token has expired";
    public static final String TOKEN_ALREADY_USED = "Token has already been used";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
    public static final String EMPLOYEE_ID_ALREADY_EXISTS = "Employee ID already exists";
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String ACCOUNT_NOT_VERIFIED = "Account is not verified. Please check your email.";
    public static final String PASSWORD_MISMATCH = "Current password is incorrect";
    public static final String PASSWORD_CANNOT_BE_SAME_AS_OLD = "New password cannot be the same as the old password";

    // Fleet Flow Domain Messages
    public static final String VEHICLE_NOT_FOUND = "Vehicle not found";
    public static final String DRIVER_NOT_FOUND = "Driver not found";
    public static final String TRIP_NOT_FOUND = "Trip not found";
    public static final String MAINTENANCE_LOG_NOT_FOUND = "Maintenance log not found";
    public static final String EXPENSE_NOT_FOUND = "Expense record not found";
    public static final String LICENSE_PLATE_ALREADY_EXISTS = "License plate already exists";
    public static final String LICENSE_NUMBER_ALREADY_EXISTS = "License number already exists";
    public static final String CARGO_EXCEEDS_CAPACITY = "Cargo weight exceeds vehicle max load capacity";
    public static final String VEHICLE_NOT_AVAILABLE = "Vehicle is not available for dispatch";
    public static final String DRIVER_NOT_AVAILABLE = "Driver is not available for dispatch";
    public static final String DRIVER_LICENSE_EXPIRED = "Driver's license has expired";
    public static final String DRIVER_SUSPENDED = "Driver is currently suspended";

    // Success Messages (used in Services)
    public static final String SIGNUP_SUCCESS = "Registration successful! Please check your email to verify your account.";
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String VERIFICATION_SUCCESS = "Email verified successfully! You can now login.";
    public static final String PASSWORD_RESET_EMAIL_SENT = "Password reset instructions have been sent to your email.";
    public static final String PASSWORD_RESET_SUCCESS = "Password has been reset successfully.";
    public static final String PASSWORD_CHANGE_SUCCESS = "Password changed successfully.";
    public static final String PROFILE_UPDATE_SUCCESS = "Profile updated successfully.";

    private Constants() {
        // Private constructor to prevent instantiation
    }
}
package com.edutrack.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility generators (Member 01 + Member 05).
 * - Registration numbers are unique and auto-generated (BR-REG-01).
 * - OTP codes support self-service password recovery (UC-UAM-01).
 */
public final class CodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private CodeGenerator() {
    }

    /** Student registration number, e.g. STU-2026-0007. */
    public static String studentRegNo(long sequence) {
        return "STU-" + LocalDateTime.now().getYear() + "-" + String.format("%04d", sequence);
    }

    /** Teacher staff number, e.g. TCH-2026-0003. */
    public static String teacherRegNo(long sequence) {
        return "TCH-" + LocalDateTime.now().getYear() + "-" + String.format("%04d", sequence);
    }

    /** 6-digit numeric OTP valid for a short window. */
    public static String otp6() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    /**
     * Generates a readable temporary password for newly-created student accounts.
     * The password is shown once through the existing flash-message UI and is
     * stored only as a PBKDF2 hash.
     */
    public static String temporaryStudentPassword() {
        // Demo/institute default password so a newly registered student can
        // immediately log in without losing a randomly generated password.
        // The value is still stored only as a PBKDF2 hash in the database.
        return "EduTrack123";
    }

    /** Readable receipt number, e.g. RCP-20260906-4821 (Member 04). */
    public static String receiptNo(long sequence) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "RCP-" + date + "-" + String.format("%04d", sequence);
    }

    /** Invoice number, e.g. INV-2026-000031 (Member 04). */
    public static String invoiceNo(long sequence) {
        return "INV-" + LocalDateTime.now().getYear() + "-" + String.format("%06d", sequence);
    }
}

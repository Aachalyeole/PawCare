// service/OtpService.java
package com.pawcare.Pawcare_Backend.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private static final SecureRandom random = new SecureRandom();

    public String generateOtp(String email) {
        String otp = String.format("%06d", random.nextInt(1000000));
        otpStorage.put(email, new OtpData(otp, System.currentTimeMillis()));
        System.out.println("📧 OTP generated for " + email + ": " + otp); // For debugging
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        OtpData otpData = otpStorage.get(email);

        if (otpData == null) {
            System.out.println("❌ No OTP found for: " + email);
            return false;
        }

        // Check if OTP is expired (10 minutes = 600000 milliseconds)
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - otpData.timestamp;

        if (timeElapsed > 600000) {
            otpStorage.remove(email);
            System.out.println("⏰ OTP expired for: " + email + " (elapsed: " + timeElapsed + "ms)");
            return false;
        }

        boolean isValid = otpData.otp.equals(otp);

        if (isValid) {
            otpStorage.remove(email);
            System.out.println("✅ OTP verified successfully for: " + email);
        } else {
            System.out.println("❌ Invalid OTP for: " + email + " | Expected: " + otpData.otp + " | Got: " + otp);
        }

        return isValid;
    }

    // Method to resend OTP
    public String resendOtp(String email) {
        // Remove existing OTP if any
        otpStorage.remove(email);
        // Generate new OTP
        return generateOtp(email);
    }

    // Method to check if OTP exists and is valid
    public boolean hasValidOtp(String email) {
        OtpData otpData = otpStorage.get(email);
        if (otpData == null) {
            return false;
        }
        long timeElapsed = System.currentTimeMillis() - otpData.timestamp;
        return timeElapsed <= 600000; // Valid if less than 10 minutes
    }

    // Inner class to store OTP with timestamp
    private static class OtpData {
        String otp;
        long timestamp;

        OtpData(String otp, long timestamp) {
            this.otp = otp;
            this.timestamp = timestamp;
        }
    }
}
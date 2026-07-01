
package com.pawcare.Pawcare_Backend.controller;

import com.pawcare.Pawcare_Backend.dto.*;
import com.pawcare.Pawcare_Backend.model.NGODetails;
import com.pawcare.Pawcare_Backend.model.User;
import com.pawcare.Pawcare_Backend.repository.NGODetailsRepository;
import com.pawcare.Pawcare_Backend.repository.UserRepository;
import com.pawcare.Pawcare_Backend.service.EmailService;
import com.pawcare.Pawcare_Backend.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NGODetailsRepository ngoDetailsRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    // Test endpoint
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of("message", "Auth API is working! 🐾"));
    }

    // Send OTP for email verification
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }

        String otp = otpService.generateOtp(email);
        emailService.sendOtpEmail(email, otp);

        return ResponseEntity.ok(Map.of(
                "message", "OTP sent successfully to " + email,
                "email", email
        ));
    }

    // Verify OTP
    // controller/AuthController.java - Fix verify-otp endpoint

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequest request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());

        if (isValid) {
            // Check if user exists (for signup case)
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setIsEmailVerified(true);
                userRepository.save(user);
                System.out.println("✅ Email verified for existing user: " + user.getEmail());
            } else {
                // For new signup - we'll store that email is verified for later signup
                // You can store in a temporary cache if needed
                System.out.println("✅ OTP verified for email: " + request.getEmail() + " (user will be created during signup)");
            }

            return ResponseEntity.ok(Map.of(
                    "verified", true,
                    "message", "Email verified successfully. You can now login."
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "verified", false,
                    "error", "Invalid or expired OTP"
            ));
        }
    }

    // User Signup
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }

            if (userRepository.existsByPhone(request.getPhone())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Phone already exists"));
            }

            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setAddress(request.getAddress() != null ? request.getAddress() : "");
            user.setLocation(request.getLocation() != null ? request.getLocation() : "");
            user.setRole(User.Role.USER);
            user.setIsEmailVerified(true);
            user.setIsNGOVerified(false);

            User savedUser = userRepository.save(user);
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());

            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully. Please verify your email with OTP.");
            response.put("email", savedUser.getEmail());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    // User Login - FIXED with SecurityContext
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());

            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            User user = userOpt.get();

            // Check if email is verified
            if (!user.getIsEmailVerified()) {
                String otp = otpService.generateOtp(user.getEmail());
                emailService.sendOtpEmail(user.getEmail(), otp);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Email not verified. New OTP sent to your email."));
            }

            // Check password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid password"));
            }

            // Check NGO verification if role is NGO
            if (user.getRole() == User.Role.NGO && !user.getIsNGOVerified()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Your NGO account is pending admin verification."));
            }

            // ✅ IMPORTANT: Create Authentication and set SecurityContext
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Create session and set security context
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // Return user data
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getUserId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("phone", user.getPhone());
            response.put("role", user.getRole().toString());
            response.put("isEmailVerified", user.getIsEmailVerified());
            response.put("isNGOVerified", user.getIsNGOVerified());
            response.put("message", "Login successful!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // Get current user from session
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in"));
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getUserId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("role", user.getRole().toString());
        response.put("isEmailVerified", user.getIsEmailVerified());
        response.put("isNGOVerified", user.getIsNGOVerified());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup-with-otp")
    public ResponseEntity<?> signupWithOtp(@RequestBody SignupWithOtpRequest request) {
        try {
            boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());
            if (!isValid) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired OTP"));
            }

            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }
            if (userRepository.existsByPhone(request.getPhone())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Phone already exists"));
            }

            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setAddress(request.getAddress());
            user.setLocation(request.getLocation());
            user.setRole(User.Role.USER);
            user.setIsEmailVerified(true);
            user.setIsNGOVerified(false);

            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully! Please login."
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/ngo/signup")
    public ResponseEntity<?> ngoSignup(@RequestBody NGOSignupRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }

            if (userRepository.existsByPhone(request.getPhone())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Phone already exists"));
            }

            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setAddress(request.getAddress() != null ? request.getAddress() : "");
            user.setLocation(request.getLocation() != null ? request.getLocation() : "");
            user.setRole(User.Role.NGO);
            user.setIsEmailVerified(true);
            user.setIsNGOVerified(false);

            User savedUser = userRepository.save(user);

            NGODetails ngoDetails = new NGODetails();
            ngoDetails.setUser(savedUser);
            ngoDetails.setNgoName(request.getNgoName());
            ngoDetails.setRegistrationNumber(request.getRegistrationNumber());
            ngoDetails.setDescription(request.getDescription() != null ? request.getDescription() : "");
            ngoDetails.setLatitude(request.getLatitude() != null ? request.getLatitude() : 0.0);
            ngoDetails.setLongitude(request.getLongitude() != null ? request.getLongitude() : 0.0);
            ngoDetails.setCity(request.getCity() != null ? request.getCity() : "");
            ngoDetails.setState(request.getState() != null ? request.getState() : "");
            ngoDetails.setPincode(request.getPincode() != null ? request.getPincode() : "");
            ngoDetails.setWebsite(request.getWebsite() != null ? request.getWebsite() : "");
            ngoDetails.setEmergencyContact(request.getEmergencyContact() != null ? request.getEmergencyContact() : "");

            ngoDetailsRepository.save(ngoDetails);

            String otp = otpService.generateOtp(request.getEmail());
            emailService.sendOtpEmail(request.getEmail(), otp);

            Map<String, String> response = new HashMap<>();
            response.put("message", "NGO registered successfully. Please verify your email with OTP. Your account will be reviewed by admin.");
            response.put("email", request.getEmail());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "NGO registration failed: " + e.getMessage()));
        }
    }
}
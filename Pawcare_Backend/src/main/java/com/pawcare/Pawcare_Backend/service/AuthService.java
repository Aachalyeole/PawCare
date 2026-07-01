// service/AuthService.java - COMPLETE WORKING VERSION
package com.pawcare.Pawcare_Backend.service;

import com.pawcare.Pawcare_Backend.dto.*;
import com.pawcare.Pawcare_Backend.model.NGODetails;
import com.pawcare.Pawcare_Backend.model.User;
import com.pawcare.Pawcare_Backend.repository.NGODetailsRepository;
import com.pawcare.Pawcare_Backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NGODetailsRepository ngoDetailsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Transactional
    public void registerUser(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAddress(request.getAddress());
        user.setLocation(request.getLocation());
        user.setRole(User.Role.USER);
        user.setIsEmailVerified(false);
        user.setIsNGOVerified(false);

        userRepository.save(user);
    }

    @Transactional
    public void registerNGO(NGOSignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone already registered");
        }
        if (ngoDetailsRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new RuntimeException("Registration number already exists");
        }

        // Create user with NGO role
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAddress(request.getAddress());
        user.setLocation(request.getLocation());
        user.setRole(User.Role.NGO);
        user.setIsEmailVerified(false);
        user.setIsNGOVerified(false); // Needs admin verification

        User savedUser = userRepository.save(user);

        // Create NGO details
        NGODetails ngoDetails = new NGODetails();
        ngoDetails.setUser(savedUser);
        ngoDetails.setNgoName(request.getNgoName());
        ngoDetails.setRegistrationNumber(request.getRegistrationNumber());
        ngoDetails.setDescription(request.getDescription());
        ngoDetails.setLatitude(request.getLatitude());
        ngoDetails.setLongitude(request.getLongitude());
        ngoDetails.setCity(request.getCity());
        ngoDetails.setState(request.getState());
        ngoDetails.setPincode(request.getPincode());
        ngoDetails.setWebsite(request.getWebsite());
        ngoDetails.setEmergencyContact(request.getEmergencyContact());

        ngoDetailsRepository.save(ngoDetails);
    }

    public LoginResponse authenticateUser(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if email is verified
        if (!user.getIsEmailVerified()) {
            // Resend OTP
            String otp = otpService.generateOtp(user.getEmail());
            emailService.sendOtpEmail(user.getEmail(), otp);
            throw new RuntimeException("Please verify your email first. A new OTP has been sent to your email.");
        }

        // For NGO, check if verified by admin
        if (user.getRole() == User.Role.NGO && !user.getIsNGOVerified()) {
            throw new RuntimeException("Your NGO account is pending verification by admin. You will be notified once verified.");
        }

        // Create response
        LoginResponse response = new LoginResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().toString(),
                user.getIsEmailVerified(),
                user.getIsNGOVerified()
        );

        // Add NGO details if user is NGO and has NGO details
        if (user.getRole() == User.Role.NGO && user.getNgoDetails() != null) {
            NGODetails ngo = user.getNgoDetails();
            NGODetailsDTO ngoDetailsDTO = new NGODetailsDTO(
                    ngo.getNgoId(),
                    ngo.getNgoName(),
                    ngo.getRegistrationNumber(),
                    ngo.getDescription(),
                    ngo.getLatitude(),
                    ngo.getLongitude(),
                    ngo.getCity(),
                    ngo.getState(),
                    ngo.getEmergencyContact(),
                    user.getIsNGOVerified()
            );
            response.setNgoDetails(ngoDetailsDTO);
        }

        return response;
    }
}
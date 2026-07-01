
package com.pawcare.Pawcare_Backend.controller;

import com.pawcare.Pawcare_Backend.dto.ReportResponse;
import com.pawcare.Pawcare_Backend.model.NGODetails;
import com.pawcare.Pawcare_Backend.model.User;
import com.pawcare.Pawcare_Backend.repository.AnimalReportRepository;
import com.pawcare.Pawcare_Backend.repository.NGODetailsRepository;
import com.pawcare.Pawcare_Backend.repository.UserRepository;
import com.pawcare.Pawcare_Backend.service.AnimalRescueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NGODetailsRepository ngoDetailsRepository;

    @Autowired
    private AnimalRescueService rescueService;

    @Autowired
    private AnimalReportRepository reportRepository;

    // Get dashboard statistics
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
            }

            String email = authentication.getName();
            User admin = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            if (admin.getRole() != User.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin only"));
            }

            long totalUsers = userRepository.count();
            long totalNGOs = userRepository.findByRole(User.Role.NGO).size();
            long pendingNGOs = userRepository.findByRoleAndIsNGOVerifiedFalse(User.Role.NGO).size();
            long totalReports = reportRepository.count();
            long pendingReports = reportRepository.countByStatus("PENDING");
            long completedReports = reportRepository.countByStatus("COMPLETED") + reportRepository.countByStatus("RESCUED");

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("totalNGOs", totalNGOs);
            stats.put("pendingNGOs", pendingNGOs);
            stats.put("totalReports", totalReports);
            stats.put("pendingReports", pendingReports);
            stats.put("completedReports", completedReports);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get all pending NGOs
    @GetMapping("/pending-ngos")
    public ResponseEntity<?> getPendingNGOs(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
            }

            User admin = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            if (admin.getRole() != User.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. Admin only."));
            }

            List<User> pendingNGOs = userRepository.findByRoleAndIsNGOVerifiedFalse(User.Role.NGO);

            List<Map<String, Object>> response = pendingNGOs.stream().map(ngo -> {
                Map<String, Object> ngoData = new HashMap<>();
                ngoData.put("userId", ngo.getUserId());
                ngoData.put("name", ngo.getName());
                ngoData.put("email", ngo.getEmail());
                ngoData.put("phone", ngo.getPhone());
                ngoData.put("location", ngo.getLocation());

                if (ngo.getNgoDetails() != null) {
                    NGODetails details = ngo.getNgoDetails();
                    ngoData.put("ngoName", details.getNgoName());
                    ngoData.put("registrationNumber", details.getRegistrationNumber());
                    ngoData.put("city", details.getCity());
                    ngoData.put("state", details.getState());
                    ngoData.put("emergencyContact", details.getEmergencyContact());
                }
                return ngoData;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Verify NGO
    @PutMapping("/verify-ngo/{userId}")
    public ResponseEntity<?> verifyNGO(@PathVariable Long userId, Authentication authentication) {
        try {
            User admin = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            if (admin.getRole() != User.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. Admin only."));
            }

            User ngo = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("NGO not found"));

            ngo.setIsNGOVerified(true);
            userRepository.save(ngo);

            return ResponseEntity.ok(Map.of("message", "NGO verified successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get all users
    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
            }

            User admin = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            if (admin.getRole() != User.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. Admin only."));
            }

            List<User> users = userRepository.findAll();

            List<Map<String, Object>> response = users.stream().map(user -> {
                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", user.getUserId());
                userData.put("name", user.getName());
                userData.put("email", user.getEmail());
                userData.put("phone", user.getPhone());
                userData.put("role", user.getRole().toString());
                userData.put("isEmailVerified", user.getIsEmailVerified());
                userData.put("isNGOVerified", user.getIsNGOVerified());
                userData.put("location", user.getLocation());
                return userData;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get all reports
    @GetMapping("/all-reports")
    public ResponseEntity<?> getAllReports(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
            }

            User admin = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            if (admin.getRole() != User.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. Admin only."));
            }

            List<ReportResponse> reports = rescueService.getAllReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
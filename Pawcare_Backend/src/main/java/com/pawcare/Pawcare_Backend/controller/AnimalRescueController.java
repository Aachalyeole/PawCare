// controller/AnimalRescueController.java
package com.pawcare.Pawcare_Backend.controller;

import com.pawcare.Pawcare_Backend.dto.*;
import com.pawcare.Pawcare_Backend.model.AnimalReport;
import com.pawcare.Pawcare_Backend.model.NGODetails;
import com.pawcare.Pawcare_Backend.model.User;
import com.pawcare.Pawcare_Backend.repository.AnimalReportRepository;
import com.pawcare.Pawcare_Backend.repository.NGODetailsRepository;
import com.pawcare.Pawcare_Backend.repository.UserRepository;
import com.pawcare.Pawcare_Backend.service.AnimalRescueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/rescue")
public class AnimalRescueController {

    @Autowired
    private AnimalRescueService rescueService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NGODetailsRepository ngoDetailsRepository;

    @Autowired
    private AnimalReportRepository reportRepository;

    // 1. Report an injured animal (with optional photos)
    // controller/AnimalRescueController.java - Check this method
    // controller/AnimalRescueController.java - Just the fixed report method
    @PostMapping(value = "/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> reportAnimal(
            @RequestPart("report") ReportRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        try {
            // Get user by email from request (since Authentication is null)
            User user = userRepository.findByEmail(request.getUserEmail())
                    .orElseThrow(() -> new RuntimeException("User not found. Please login again."));

            AnimalReport report = rescueService.createReport(request, user.getUserId(), images);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Report submitted successfully");
            response.put("reportId", report.getReportId());
            response.put("status", report.getStatus());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to submit report: " + e.getMessage()));
        }
    }
    // 2. Get my reports (for normal users)
    // controller/AnimalRescueController.java - Update this method

    // 2. Get my reports (for normal users) - FIXED VERSION
    @GetMapping("/my-reports")
    public ResponseEntity<?> getMyReports(@RequestParam String userEmail) {
        try {
            // Get user by email from request parameter
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<ReportResponse> reports = rescueService.getReportsByUser(user.getUserId());
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // 3. Get pending reports (for NGOs to claim)
    @GetMapping("/pending-reports")
    public ResponseEntity<?> getPendingReports() {
        try {
            List<ReportResponse> reports = rescueService.getPendingReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // 4. Get nearby pending reports (based on NGO location)
    @PostMapping("/nearby-reports")
    public ResponseEntity<?> getNearbyReports(@RequestBody NearbyNGORequest request) {
        try {
            List<ReportResponse> reports = rescueService.getNearbyPendingReports(
                    request.getLatitude(),
                    request.getLongitude(),
                    request.getRadius() != null ? request.getRadius() : 10.0
            );
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // 5. Get reports assigned to NGO
    @GetMapping("/ngo/assigned-reports")
    public ResponseEntity<?> getAssignedReports(Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getNgoDetails() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Not an NGO user"));
            }

            List<ReportResponse> reports = rescueService.getReportsByNGO(user.getNgoDetails().getNgoId());
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // 6. Update report status (for NGOs)
    @PutMapping("/report/{reportId}/status")
    public ResponseEntity<?> updateReportStatus(
            @PathVariable Long reportId,
            @RequestBody UpdateStatusRequest request,
            Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Long ngoId = user.getNgoDetails() != null ? user.getNgoDetails().getNgoId() : null;

            AnimalReport report = rescueService.updateReportStatus(reportId, request.getStatus(), ngoId, request.getNotes());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Report status updated successfully");
            response.put("status", report.getStatus());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // 7. Claim a report (for NGOs)
    @PostMapping("/report/{reportId}/claim")
    public ResponseEntity<?> claimReport(@PathVariable Long reportId, Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getNgoDetails() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only NGOs can claim reports"));
            }

            AnimalReport report = rescueService.claimReport(reportId, user.getNgoDetails().getNgoId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Report claimed successfully");
            response.put("status", report.getStatus());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // 8. Auto-assign nearest NGO for a report (for emergencies)
    @PostMapping("/report/{reportId}/assign-nearest")
    public ResponseEntity<?> assignNearestNGO(@PathVariable Long reportId) {
        try {
            AnimalReport report = rescueService.assignNearestNGO(reportId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Nearest NGO assigned successfully");
            response.put("assignedNGO", report.getAssignedNGO() != null ? report.getAssignedNGO().getNgoName() : "No NGO found nearby");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // 9. Get report details by ID
    // controller/AnimalRescueController.java - Add this method

    // 9. Get report by ID
    @GetMapping("/report/{reportId}")
    public ResponseEntity<?> getReportById(@PathVariable Long reportId, @RequestParam String userEmail) {
        try {
            // Verify user exists
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            AnimalReport report = rescueService.getReportById(reportId);
            ReportResponse response = rescueService.convertToResponse(report);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get nearby NGOs based on location
    @PostMapping("/nearby-ngos")
    public ResponseEntity<?> getNearbyNGOs(@RequestBody NearbyNGORequest request) {
        try {
            double radius = request.getRadius() != null ? request.getRadius() : 10.0; // Default 10km
            List<NearbyNGOResponse> ngos = rescueService.getNearbyNGOs(
                    request.getLatitude(),
                    request.getLongitude(),
                    radius
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Found " + ngos.size() + " NGOs nearby");
            response.put("ngos", ngos);
            response.put("count", ngos.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // controller/AnimalRescueController.java - Add this endpoint

    @PutMapping("/ngo/update-location")
    public ResponseEntity<?> updateNGOLocation(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            Long userId = ((Number) request.get("userId")).longValue();
            Double latitude = ((Number) request.get("latitude")).doubleValue();
            Double longitude = ((Number) request.get("longitude")).doubleValue();
            String city = (String) request.get("city");
            String state = (String) request.get("state");
            String emergencyContact = (String) request.get("emergencyContact");

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getRole() != User.Role.NGO) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only NGOs can update location"));
            }

            NGODetails ngoDetails = user.getNgoDetails();
            if (ngoDetails == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "NGO details not found"));
            }

            ngoDetails.setLatitude(latitude);
            ngoDetails.setLongitude(longitude);
            ngoDetails.setCity(city);
            ngoDetails.setState(state);
            ngoDetails.setEmergencyContact(emergencyContact);
            ngoDetails.setUpdatedAt(LocalDateTime.now());

            ngoDetailsRepository.save(ngoDetails);

            return ResponseEntity.ok(Map.of(
                    "message", "Location updated successfully",
                    "latitude", latitude,
                    "longitude", longitude
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
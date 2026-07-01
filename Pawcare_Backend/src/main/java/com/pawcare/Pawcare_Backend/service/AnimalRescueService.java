// service/AnimalRescueService.java - FINAL FIXED VERSION
package com.pawcare.Pawcare_Backend.service;

import com.pawcare.Pawcare_Backend.dto.NearbyNGOResponse;
import com.pawcare.Pawcare_Backend.dto.ReportRequest;
import com.pawcare.Pawcare_Backend.dto.ReportResponse;
import com.pawcare.Pawcare_Backend.model.AnimalReport;
import com.pawcare.Pawcare_Backend.model.NGODetails;
import com.pawcare.Pawcare_Backend.model.User;
import com.pawcare.Pawcare_Backend.repository.AnimalReportRepository;
import com.pawcare.Pawcare_Backend.repository.NGODetailsRepository;
import com.pawcare.Pawcare_Backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AnimalRescueService {

    @Autowired
    private AnimalReportRepository reportRepository;

    @Autowired
    private NGODetailsRepository ngoDetailsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    @Transactional
    public AnimalReport createReport(ReportRequest request, Long userId, List<MultipartFile> images) throws IOException {
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AnimalReport report = new AnimalReport();
        report.setReporter(reporter);
        report.setAnimalType(request.getAnimalType());
        report.setCondition(request.getCondition());
        report.setDescription(request.getDescription());
        report.setLatitude(request.getLatitude());
        report.setLongitude(request.getLongitude());
        report.setLocationAddress(request.getLocationAddress());
        report.setIsEmergency(request.getIsEmergency() != null ? request.getIsEmergency() : false);
        report.setContactPhone(request.getContactPhone());
        report.setContactName(request.getContactName());
        report.setStatus("PENDING");

        // Save images
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile image : images) {
                String filename = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + "_" + image.getOriginalFilename();
                Path path = Paths.get(uploadDir + "reports/");
                Files.createDirectories(path);
                Path filePath = path.resolve(filename);
                Files.write(filePath, image.getBytes());
                imageUrls.add("/uploads/reports/" + filename);
            }
            report.setImageUrls(imageUrls);
        }

        AnimalReport savedReport = reportRepository.save(report);

        // Auto-assign to nearest NGO for emergency cases
        if (report.getIsEmergency()) {
            assignNearestNGO(savedReport.getReportId());
        }

        // Send email notification to reporter
        emailService.sendReportConfirmationEmail(reporter.getEmail(), savedReport);

        return savedReport;
    }

    @Transactional
    public AnimalReport assignNearestNGO(Long reportId) {
        AnimalReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        List<NearbyNGOResponse> nearbyNGOs = getNearbyNGOs(report.getLatitude(), report.getLongitude(), 20.0);

        if (!nearbyNGOs.isEmpty()) {
            NGODetails nearestNGO = ngoDetailsRepository.findById(nearbyNGOs.get(0).getNgoId())
                    .orElse(null);

            if (nearestNGO != null) {
                report.setAssignedNGO(nearestNGO);
                report.setStatus("ASSIGNED");
                report.setUpdatedAt(LocalDateTime.now());

                AnimalReport updatedReport = reportRepository.save(report);

                // Send email notifications
                emailService.sendRescueAssignmentEmailToNGO(nearestNGO.getUser().getEmail(), updatedReport);
                emailService.sendRescueAssignmentEmailToReporter(report.getReporter().getEmail(), updatedReport, nearestNGO);

                return updatedReport;
            }
        }

        return report;
    }

    public List<NearbyNGOResponse> getNearbyNGOs(double lat, double lng, double radius) {
        List<NGODetails> allNGOs = ngoDetailsRepository.findAll();
        List<NearbyNGOResponse> nearbyNGOs = new ArrayList<>();

        for (NGODetails ngo : allNGOs) {
            // Only include verified NGOs with valid coordinates
            if (ngo.getUser().getIsNGOVerified() &&
                    ngo.getLatitude() != null && ngo.getLongitude() != null) {

                double distance = calculateDistance(lat, lng, ngo.getLatitude(), ngo.getLongitude());

                if (distance <= radius) {
                    NearbyNGOResponse response = new NearbyNGOResponse();
                    response.setNgoId(ngo.getNgoId());
                    response.setNgoName(ngo.getNgoName());
                    response.setName(ngo.getUser().getName());
                    response.setEmail(ngo.getUser().getEmail());
                    response.setPhone(ngo.getUser().getPhone());
                    response.setDescription(ngo.getDescription());
                    response.setLatitude(ngo.getLatitude());
                    response.setLongitude(ngo.getLongitude());
                    response.setAddress(ngo.getUser().getAddress());
                    response.setCity(ngo.getCity());
                    response.setState(ngo.getState());
                    response.setDistance(distance);
                    response.setIsVerified(ngo.getUser().getIsNGOVerified());
                    response.setEmergencyContact(ngo.getEmergencyContact());

                    nearbyNGOs.add(response);
                }
            }
        }

        // Sort by distance
        nearbyNGOs.sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()));

        return nearbyNGOs;
    }

    public List<ReportResponse> getNearbyPendingReports(double lat, double lng, double radius) {
        List<AnimalReport> allReports = reportRepository.findByStatus("PENDING");
        List<ReportResponse> nearbyReports = new ArrayList<>();

        for (AnimalReport report : allReports) {
            if (report.getLatitude() != null && report.getLongitude() != null) {
                double distance = calculateDistance(lat, lng, report.getLatitude(), report.getLongitude());

                if (distance <= radius) {
                    nearbyReports.add(convertToResponse(report));
                }
            }
        }

        // Sort by distance
        nearbyReports.sort((r1, r2) -> {
            AnimalReport rep1 = reportRepository.findById(r1.getReportId()).orElse(null);
            AnimalReport rep2 = reportRepository.findById(r2.getReportId()).orElse(null);

            if (rep1 == null || rep2 == null) return 0;
            if (rep1.getLatitude() == null || rep1.getLongitude() == null) return 0;
            if (rep2.getLatitude() == null || rep2.getLongitude() == null) return 0;

            double dist1 = calculateDistance(lat, lng, rep1.getLatitude(), rep1.getLongitude());
            double dist2 = calculateDistance(lat, lng, rep2.getLatitude(), rep2.getLongitude());

            return Double.compare(dist1, dist2);
        });

        return nearbyReports;
    }

    public List<ReportResponse> getReportsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<AnimalReport> reports = reportRepository.findByReporterOrderByCreatedAtDesc(user);
        return reports.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public List<ReportResponse> getReportsByNGO(Long ngoId) {
        NGODetails ngo = ngoDetailsRepository.findById(ngoId)
                .orElseThrow(() -> new RuntimeException("NGO not found"));

        List<AnimalReport> reports = reportRepository.findByAssignedNGOOrderByCreatedAtDesc(ngo);
        return reports.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public List<ReportResponse> getPendingReports() {
        List<AnimalReport> reports = reportRepository.findByStatus("PENDING");
        return reports.stream()
                .sorted((a, b) -> {
                    if (a.getIsEmergency() && !b.getIsEmergency()) return -1;
                    if (!a.getIsEmergency() && b.getIsEmergency()) return 1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnimalReport updateReportStatus(Long reportId, String status, Long ngoId, String notes) {
        AnimalReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        // Verify NGO is assigned to this report
        if (ngoId != null && (report.getAssignedNGO() == null || !report.getAssignedNGO().getNgoId().equals(ngoId))) {
            throw new RuntimeException("NGO not authorized to update this report");
        }

        report.setStatus(status);
        report.setUpdatedAt(LocalDateTime.now());

        if ("RESCUED".equals(status) || "COMPLETED".equals(status)) {
            report.setResolvedAt(LocalDateTime.now());
        }

        AnimalReport updatedReport = reportRepository.save(report);

        // Send status update email
        emailService.sendReportStatusUpdateEmail(report.getReporter().getEmail(), updatedReport, status, notes);

        return updatedReport;
    }

    @Transactional
    public AnimalReport claimReport(Long reportId, Long ngoId) {
        AnimalReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        if (!"PENDING".equals(report.getStatus())) {
            throw new RuntimeException("Report is no longer available for claiming");
        }

        NGODetails ngo = ngoDetailsRepository.findById(ngoId)
                .orElseThrow(() -> new RuntimeException("NGO not found"));

        report.setAssignedNGO(ngo);
        report.setStatus("ASSIGNED");
        report.setUpdatedAt(LocalDateTime.now());

        AnimalReport updatedReport = reportRepository.save(report);

        // Send notifications
        emailService.sendReportClaimedEmail(report.getReporter().getEmail(), updatedReport, ngo);
        emailService.sendRescueAssignmentEmailToNGO(ngo.getUser().getEmail(), updatedReport);

        return updatedReport;
    }



    public AnimalReport getReportById(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Earth's radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }


    public List<ReportResponse> getAllReports() {
        List<AnimalReport> allReports = reportRepository.findAll();
        return allReports.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ReportResponse convertToResponse(AnimalReport report) {
        ReportResponse response = new ReportResponse();
        response.setReportId(report.getReportId());
        response.setAnimalType(report.getAnimalType());
        response.setCondition(report.getCondition());
        response.setDescription(report.getDescription());
        response.setLatitude(report.getLatitude());
        response.setLongitude(report.getLongitude());
        response.setLocationAddress(report.getLocationAddress());
        response.setStatus(report.getStatus());
        response.setImageUrls(report.getImageUrls());
        response.setIsEmergency(report.getIsEmergency());
        response.setCreatedAt(report.getCreatedAt());
        response.setUpdatedAt(report.getUpdatedAt());
        response.setContactPhone(report.getContactPhone());
        response.setContactName(report.getContactName());

        if (report.getReporter() != null) {
            response.setReporterName(report.getReporter().getName());
            response.setReporterEmail(report.getReporter().getEmail());
            response.setReporterPhone(report.getReporter().getPhone());
        }

        if (report.getAssignedNGO() != null) {
            response.setAssignedNGOName(report.getAssignedNGO().getNgoName());
            response.setAssignedNGOId(report.getAssignedNGO().getNgoId());
        }

        return response;
    }
}
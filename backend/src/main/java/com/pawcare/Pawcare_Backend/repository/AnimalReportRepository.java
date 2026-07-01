
package com.pawcare.Pawcare_Backend.repository;

import com.pawcare.Pawcare_Backend.model.AnimalReport;
import com.pawcare.Pawcare_Backend.model.NGODetails;
import com.pawcare.Pawcare_Backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnimalReportRepository extends JpaRepository<AnimalReport, Long> {

    // Find reports by reporter
    List<AnimalReport> findByReporterOrderByCreatedAtDesc(User reporter);

    // Find reports by assigned NGO
    List<AnimalReport> findByAssignedNGOOrderByCreatedAtDesc(NGODetails ngo);

    // Find reports by status
    List<AnimalReport> findByStatusOrderByCreatedAtDesc(String status);

    // Find reports by status (without order - for the error)
    List<AnimalReport> findByStatus(String status);

    // Find emergency reports
    List<AnimalReport> findByIsEmergencyTrueOrderByCreatedAtDesc();

    // Find pending reports (for NGOs to claim)
    List<AnimalReport> findByStatusAndAssignedNGONullOrderByIsEmergencyDescCreatedAtAsc(String status);

    // Count reports by status for dashboard
    long countByStatus(String status);

    // Count reports by reporter
    long countByReporter(User reporter);

    // Count reports by assigned NGO
    long countByAssignedNGO(NGODetails ngo);

    // Find reports by status and within radius using native query
    @Query(value = "SELECT r.* FROM animal_reports r " +
            "WHERE r.status = :status " +
            "AND (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) * " +
            "cos(radians(r.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(r.latitude)))) <= :radius " +
            "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) * " +
            "cos(radians(r.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(r.latitude))))",
            nativeQuery = true)
    List<AnimalReport> findNearbyPendingReports(@Param("lat") double lat,
                                                @Param("lng") double lng,
                                                @Param("radius") double radius,
                                                @Param("status") String status);
}
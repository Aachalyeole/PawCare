
package com.pawcare.Pawcare_Backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "animal_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnimalReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(nullable = false)
    private String animalType; // Dog, Cat, Cow, Bird, Other


    @Column(name = "animal_condition", nullable = false)
    private String condition; // Injured, Abandoned, Sick, Stray, Emergency

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String locationAddress;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, ASSIGNED, IN_PROGRESS, RESCUED, COMPLETED, CANCELLED

    @ElementCollection
    @CollectionTable(name = "report_images", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "assigned_ngo_id")
    private NGODetails assignedNGO;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    private Boolean isEmergency = false;

    private String contactPhone;
    private String contactName;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
// model/NGODetails.java
package com.pawcare.Pawcare_Backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ngo_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NGODetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ngoId;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false)
    private String ngoName;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    @Column(length = 1000)
    private String description;

    private Double latitude;
    private Double longitude;
    private String city;
    private String state;
    private String pincode;

    private String website;
    private String emergencyContact;

    private String certificateUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Transient
    private Double distance;

//    public Double getDistance() {
//        return distance;
//    }
//
//    public void setDistance(Double distance) {
//        this.distance = distance;
//    }
}
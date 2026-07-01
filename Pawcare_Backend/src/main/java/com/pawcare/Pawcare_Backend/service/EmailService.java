// service/EmailService.java - SIMPLIFIED VERSION (No mail dependency)
package com.pawcare.Pawcare_Backend.service;

import com.pawcare.Pawcare_Backend.model.AnimalReport;
import com.pawcare.Pawcare_Backend.model.NGODetails;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendOtpEmail(String to, String otp) {
        // Print to console for development
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📧 EMAIL VERIFICATION OTP");
        System.out.println("=".repeat(60));
        System.out.println("To: " + to);
        System.out.println("OTP: " + otp);
        System.out.println("Valid for: 10 minutes");
        System.out.println("=".repeat(60) + "\n");
    }

    public void sendWelcomeEmail(String to, String name) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎉 WELCOME TO PAWCARE! 🎉");
        System.out.println("=".repeat(60));
        System.out.println("To: " + to);
        System.out.println("Welcome " + name + "! Thank you for joining PawCare! 🐾");
        System.out.println("=".repeat(60) + "\n");
    }
    // Add these methods to EmailService.java

    public void sendReportConfirmationEmail(String to, AnimalReport report) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📧 REPORT CONFIRMATION");
        System.out.println("=".repeat(60));
        System.out.println("To: " + to);
        System.out.println("Your report has been submitted!");
        System.out.println("Report ID: " + report.getReportId());
        System.out.println("Animal: " + report.getAnimalType());
        System.out.println("Condition: " + report.getCondition());
        System.out.println("Location: " + report.getLocationAddress());
        System.out.println("Status: " + report.getStatus());
        System.out.println("=".repeat(60) + "\n");
    }

    public void sendRescueAssignmentEmailToNGO(String to, AnimalReport report) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📧 NEW RESCUE ASSIGNMENT FOR NGO");
        System.out.println("=".repeat(60));
        System.out.println("To: " + to);
        System.out.println("A new rescue has been assigned to you!");
        System.out.println("Report ID: " + report.getReportId());
        System.out.println("Animal: " + report.getAnimalType());
        System.out.println("Location: " + report.getLocationAddress());
        System.out.println("Contact: " + report.getContactName() + " (" + report.getContactPhone() + ")");
        System.out.println("=".repeat(60) + "\n");
    }

    public void sendRescueAssignmentEmailToReporter(String to, AnimalReport report, NGODetails ngo) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📧 RESCUE ASSIGNMENT TO REPORTER");
        System.out.println("=".repeat(60));
        System.out.println("To: " + to);
        System.out.println("An NGO has been assigned to help!");
        System.out.println("NGO: " + ngo.getNgoName());
        System.out.println("Contact: " + ngo.getEmergencyContact());
        System.out.println("Report ID: " + report.getReportId());
        System.out.println("=".repeat(60) + "\n");
    }

    public void sendReportStatusUpdateEmail(String to, AnimalReport report, String status, String notes) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📧 REPORT STATUS UPDATE");
        System.out.println("=".repeat(60));
        System.out.println("To: " + to);
        System.out.println("Your report status has been updated!");
        System.out.println("Report ID: " + report.getReportId());
        System.out.println("New Status: " + status);
        if (notes != null) {
            System.out.println("Notes: " + notes);
        }
        System.out.println("=".repeat(60) + "\n");
    }

    public void sendReportClaimedEmail(String to, AnimalReport report, NGODetails ngo) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📧 REPORT CLAIMED BY NGO");
        System.out.println("=".repeat(60));
        System.out.println("To: " + to);
        System.out.println("Your report has been claimed by " + ngo.getNgoName());
        System.out.println("They will contact you shortly.");
        System.out.println("=".repeat(60) + "\n");
    }
}
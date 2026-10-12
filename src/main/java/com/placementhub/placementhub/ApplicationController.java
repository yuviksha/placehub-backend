
package com.placementhub.placementhub;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://127.0.0.1:5173",
    "https://placehub-frontend.vercel.app"
})
public class ApplicationController {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    private static final Set<String> ALLOWED_STATUSES = Set.of(
        "Applied",
        "Shortlisted",
        "Rejected",
        "Selected"
    );

    public ApplicationController(
            ApplicationRepository applicationRepository,
            JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    @PostMapping
    public ResponseEntity<?> applyForJob(
            @RequestBody Map<String, Object> request) {

        String studentName = String.valueOf(
            request.getOrDefault("studentName", "")
        ).trim();

        String studentEmail = String.valueOf(
            request.getOrDefault("studentEmail", "")
        ).trim().toLowerCase();

        Object jobIdValue = request.get("jobId");

        if (studentName.isBlank()
                || studentEmail.isBlank()
                || !studentEmail.matches(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
                )
                || jobIdValue == null) {
            return ResponseEntity.badRequest().body(
                Map.of("message",
                    "Enter a valid name, email, and job ID.")
            );
        }

        Long jobId;

        try {
            jobId = Long.valueOf(jobIdValue.toString());
        } catch (NumberFormatException exception) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "Invalid job ID.")
            );
        }

        var jobOptional = jobRepository.findById(jobId);

        if (jobOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("message", "Job not found.")
            );
        }

        if (applicationRepository
                .existsByStudentEmailIgnoreCaseAndJob_Id(
                    studentEmail, jobId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of("message",
                    "You have already applied for this job.")
            );
        }

        Application application = new Application();
        application.setStudentName(studentName);
        application.setStudentEmail(studentEmail);
        application.setJob(jobOptional.get());
        application.setStatus("Applied");

        Application saved = applicationRepository.save(application);

        return ResponseEntity.status(HttpStatus.CREATED).body(
            Map.of(
                "message", "Application submitted successfully!",
                "applicationId", saved.getId(),
                "status", saved.getStatus()
            )
        );
    }

    @GetMapping
    public List<Application> getApplications(
            @RequestParam String email) {
        return applicationRepository
            .findByStudentEmailIgnoreCase(email.trim());
    }

    @GetMapping("/all")
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateApplicationStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");

        if (status == null || !ALLOWED_STATUSES.contains(status)) {
            return ResponseEntity.badRequest().body(
                Map.of("message",
                    "Status must be Applied, Shortlisted, Rejected, or Selected.")
            );
        }

        var applicationOptional = applicationRepository.findById(id);

        if (applicationOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("message", "Application not found.")
            );
        }

        Application application = applicationOptional.get();
        application.setStatus(status);

        Application saved = applicationRepository.save(application);

        return ResponseEntity.ok(
            Map.of(
                "message", "Application status updated successfully!",
                "applicationId", saved.getId(),
                "status", saved.getStatus()
            )
        );
    }
}

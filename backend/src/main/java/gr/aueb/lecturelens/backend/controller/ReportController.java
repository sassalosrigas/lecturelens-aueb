package gr.aueb.lecturelens.backend.controller;

import gr.aueb.lecturelens.backend.model.Report;
import gr.aueb.lecturelens.backend.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportRepository reportRepository;

    // 1. Endpoint for students to submit a new report from the Android app
    @PostMapping
    public ResponseEntity<Report> createReport(@RequestBody Report report) {
        try {
            // Ensure timestamp is set
            if (report.getCreatedAt() == null) {
                report.setCreatedAt(Instant.now());
            }
            // Force status to PENDING for new reports just to be safe
            report.setStatus("PENDING");

            Report savedReport = reportRepository.save(report);
            System.out.println("New report submitted by: " + report.getReportedBy());

            return ResponseEntity.status(HttpStatus.CREATED).body(savedReport);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 2. Endpoint for Admins to fetch all reports (You will use this later for the Admin Dashboard)
    @GetMapping
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    // 3. Endpoint for Admins to fetch only pending reports
    @GetMapping("/pending")
    public List<Report> getPendingReports() {
        return reportRepository.findByStatus("PENDING");
    }

    // 4. Endpoint for Admins to update report status (e.g., RESOLVED, DISMISSED)
    @PutMapping("/{id}/status")
    public ResponseEntity<Report> updateReportStatus(@PathVariable Long id, @RequestParam String status) {
        return reportRepository.findById(id.toString())
                .map(report -> {
                    report.setStatus(status);
                    Report updatedReport = reportRepository.save(report);
                    return ResponseEntity.ok(updatedReport);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 5. Endpoint for Admins to delete a report
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        return reportRepository.findById(id.toString())
                .map(report -> {
                    reportRepository.delete(report);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
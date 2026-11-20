package com.example.collector;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class CollectorService {
    private final CollectorRepository collectors;
    private final CollectorAssignmentRepository assignments;
    private final WasteClient waste;

    public CollectorService(CollectorRepository c, CollectorAssignmentRepository a, WasteClient w) {
        collectors = c;
        assignments = a;
        waste = w;
    }

    public List<Collector> all() {
        return collectors.findAll();
    }

    public List<Collector> available() {
        return collectors.findAll().stream().filter(Collector::isAvailable).toList();
    }

    public Collector get(long id) {
        return collectors.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collector not found: " + id));
    }

    public Collector create(CollectorRequest r) {
        return collectors.save(new Collector(r.name(), r.phoneNumber(), r.vehicleNumber()));
    }

    public Collector update(long id, CollectorRequest r) {
        Collector c = get(id);
        c.update(r.name(), r.phoneNumber(), r.vehicleNumber());
        return collectors.save(c);
    }

    public List<Map<String, Object>> pending() {
        return waste.pending();
    }

    public List<CollectorAssignment> assignments(long id) {
        get(id);
        return assignments.findByCollectorId(id);
    }

    public CollectorAssignment assignmentForReport(long reportId) {
        return assignments.findByWasteReportIdAndStatus(reportId, AssignmentStatus.ASSIGNED).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active assignment for report " + reportId));
    }

    public CollectorAssignment getAssignment(long id) {
        return assignments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found: " + id));
    }

    @Transactional
    public CollectorAssignment assign(AssignmentRequest r) {
        Map<String, Object> report = waste.get(r.wasteReportId());
        if (!"PENDING".equals(report.get("status")))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Report must be PENDING before assignment");
        if (assignments.existsByWasteReportIdAndStatus(r.wasteReportId(), AssignmentStatus.ASSIGNED))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Report already has an active assignment");
        Collector c = get(r.collectorId());
        if (!c.isAvailable())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Collector is unavailable");
        CollectorAssignment a = assignments.save(new CollectorAssignment(c.getId(), r.wasteReportId()));
        c.setAvailable(false);
        collectors.save(c);
        waste.changeStatus(r.wasteReportId(), Map.of("status", "ASSIGNED"));
        return a;
    }

    @Transactional
    public CollectorAssignment status(long id, AssignmentStatus status) {
        CollectorAssignment a = assignments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
        if (a.getStatus() != AssignmentStatus.ASSIGNED || status == AssignmentStatus.ASSIGNED)
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only an active assignment can be completed or cancelled");
        if (status == AssignmentStatus.CANCELLED) {
            Map<String, Object> report = waste.get(a.getWasteReportId());
            if (!"ASSIGNED".equals(report.get("status")))
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Assignment can only be cancelled before pickup scheduling");
            waste.changeStatus(a.getWasteReportId(), Map.of("status", "CANCELLED"));
        }
        a.setStatus(status);
        collectors.findById(a.getCollectorId()).ifPresent(c -> {
            c.setAvailable(true);
            collectors.save(c);
        });
        return assignments.save(a);
    }
}

record CollectorRequest(String name, String phoneNumber, String vehicleNumber) {
}

record AssignmentRequest(Long collectorId, Long wasteReportId) {
}

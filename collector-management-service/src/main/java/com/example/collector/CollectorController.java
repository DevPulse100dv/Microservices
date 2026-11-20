package com.example.collector;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@Validated
@RequestMapping("/collectors")
public class CollectorController {
    private final CollectorService service;

    public CollectorController(CollectorService s) {
        service = s;
    }

    @GetMapping
    public List<Collector> all() {
        return service.all();
    }

    @GetMapping("/available")
    public List<Collector> available() {
        return service.available();
    }

    @GetMapping("/{id}")
    public Collector get(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    public Collector create(@Valid @RequestBody ValidCollector r) {
        return service.create(new CollectorRequest(r.name(), r.phoneNumber(), r.vehicleNumber()));
    }

    @PutMapping("/{id}")
    public Collector update(@PathVariable long id, @Valid @RequestBody ValidCollector r) {
        return service.update(id, new CollectorRequest(r.name(), r.phoneNumber(), r.vehicleNumber()));
    }

    @GetMapping("/pending-reports")
    public List<Map<String, Object>> pending() {
        return service.pending();
    }

    @PostMapping("/assignments")
    public CollectorAssignment assign(@Valid @RequestBody ValidAssignment r) {
        return service.assign(new AssignmentRequest(r.collectorId(), r.wasteReportId()));
    }

    @GetMapping("/assignments/report/{id}")
    public CollectorAssignment assignmentForReport(@PathVariable long id) {
        return service.assignmentForReport(id);
    }

    @GetMapping("/assignments/{id}")
    public CollectorAssignment getAssignment(@PathVariable long id) {
        return service.getAssignment(id);
    }

    @GetMapping("/{id}/assignments")
    public List<CollectorAssignment> assignments(@PathVariable long id) {
        return service.assignments(id);
    }

    @RequestMapping(path = "/assignments/{id}/status", method = { RequestMethod.PATCH, RequestMethod.PUT })
    public CollectorAssignment status(@PathVariable long id, @Valid @RequestBody AssignmentStatusRequest r) {
        return service.status(id, r.status());
    }
}

record ValidCollector(@NotBlank String name, @NotBlank String phoneNumber, @NotBlank String vehicleNumber) {
}

record ValidAssignment(@NotNull @Positive Long collectorId, @NotNull @Positive Long wasteReportId) {
}

record AssignmentStatusRequest(@NotNull AssignmentStatus status) {
}

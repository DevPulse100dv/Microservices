package com.example.waste;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class WasteReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String reporterName;
    @Enumerated(EnumType.STRING) private ReporterType reporterType;
    @Enumerated(EnumType.STRING) private WasteCategory wasteCategory;
    private double quantity;
    private String pickupAddress;
    private String description;
    private LocalDateTime reportDate;
    @Enumerated(EnumType.STRING) private ReportStatus status;
    protected WasteReport() {}
    public WasteReport(String reporterName, ReporterType reporterType, WasteCategory wasteCategory, double quantity, String pickupAddress, String description) {
        this.reporterName=reporterName; this.reporterType=reporterType; this.wasteCategory=wasteCategory; this.quantity=quantity; this.pickupAddress=pickupAddress; this.description=description; this.reportDate=LocalDateTime.now(); this.status=ReportStatus.PENDING;
    }
    public Long getId(){return id;} public String getReporterName(){return reporterName;} public ReporterType getReporterType(){return reporterType;} public WasteCategory getWasteCategory(){return wasteCategory;} public double getQuantity(){return quantity;} public String getPickupAddress(){return pickupAddress;} public String getDescription(){return description;} public LocalDateTime getReportDate(){return reportDate;} public ReportStatus getStatus(){return status;}
    public void update(String name, ReporterType type, WasteCategory category, double amount, String address, String details) { reporterName=name; reporterType=type; wasteCategory=category; quantity=amount; pickupAddress=address; description=details; }
    public void setStatus(ReportStatus status){this.status=status;}
}
enum ReporterType { HOUSEHOLD, BUSINESS }
enum WasteCategory { PLASTIC, PAPER, GLASS, METAL }
enum ReportStatus { PENDING, ASSIGNED, SCHEDULED, COLLECTED, HANDED_OVER, CANCELLED }

package com.example.collector;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity public class CollectorAssignment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private Long collectorId; private Long wasteReportId; private LocalDateTime assignedDate; @Enumerated(EnumType.STRING) private AssignmentStatus status;
 protected CollectorAssignment(){} public CollectorAssignment(Long collectorId,Long reportId){this.collectorId=collectorId;this.wasteReportId=reportId;this.assignedDate=LocalDateTime.now();this.status=AssignmentStatus.ASSIGNED;}
 public Long getId(){return id;} public Long getCollectorId(){return collectorId;} public Long getWasteReportId(){return wasteReportId;} public LocalDateTime getAssignedDate(){return assignedDate;} public AssignmentStatus getStatus(){return status;} public void setStatus(AssignmentStatus s){status=s;}
}
enum AssignmentStatus { ASSIGNED, COMPLETED, CANCELLED }

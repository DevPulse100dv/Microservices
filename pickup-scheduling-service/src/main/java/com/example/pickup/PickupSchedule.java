package com.example.pickup;
import jakarta.persistence.*; import java.time.*;
@Entity public class PickupSchedule {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private Long wasteReportId; private Long collectorId; private LocalDate pickupDate; private LocalTime pickupTime; private String pickupAddress; private double scheduledQuantity; private Double actualCollectedQuantity; @Enumerated(EnumType.STRING) private PickupStatus status;
 protected PickupSchedule(){} public PickupSchedule(Long report,Long collector,LocalDate date,LocalTime time,String address,double quantity){wasteReportId=report;collectorId=collector;pickupDate=date;pickupTime=time;pickupAddress=address;scheduledQuantity=quantity;status=PickupStatus.SCHEDULED;}
 public Long getId(){return id;} public Long getWasteReportId(){return wasteReportId;} public Long getCollectorId(){return collectorId;} public LocalDate getPickupDate(){return pickupDate;} public LocalTime getPickupTime(){return pickupTime;} public String getPickupAddress(){return pickupAddress;} public double getScheduledQuantity(){return scheduledQuantity;} public Double getActualCollectedQuantity(){return actualCollectedQuantity;} public PickupStatus getStatus(){return status;} public void setStatus(PickupStatus s){status=s;} public void setActualCollectedQuantity(Double q){actualCollectedQuantity=q;}
}
enum PickupStatus { SCHEDULED, IN_PROGRESS, COMPLETED, HANDED_OVER, CANCELLED }

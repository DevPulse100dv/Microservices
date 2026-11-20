package com.example.pickup;
import jakarta.persistence.*; import java.time.LocalDate;
@Entity public class RecyclingHandover {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private Long pickupId; private String organizationName; private String wasteCategory; private double quantityTransferred; private LocalDate handoverDate;
 protected RecyclingHandover(){} public RecyclingHandover(Long pickupId,String organization,String category,double quantity,LocalDate date){this.pickupId=pickupId;organizationName=organization;wasteCategory=category;quantityTransferred=quantity;handoverDate=date;}
 public Long getId(){return id;} public Long getPickupId(){return pickupId;} public String getOrganizationName(){return organizationName;} public String getWasteCategory(){return wasteCategory;} public double getQuantityTransferred(){return quantityTransferred;} public LocalDate getHandoverDate(){return handoverDate;}
}

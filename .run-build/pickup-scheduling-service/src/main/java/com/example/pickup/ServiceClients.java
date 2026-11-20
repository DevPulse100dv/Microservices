package com.example.pickup;
import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*; import java.util.*;
@FeignClient(name="collector-management-service") interface CollectorClient {@GetMapping("/collectors/assignments/report/{id}") Map<String,Object> assignment(@PathVariable("id") Long reportId); @PutMapping("/collectors/assignments/{id}/status") Map<String,Object> changeStatus(@PathVariable("id") Long id,@RequestBody Map<String,String> body);}
@FeignClient(name="waste-reporting-service") interface WasteClient {@GetMapping("/waste/{id}") Map<String,Object> get(@PathVariable("id") Long id); @PutMapping("/waste/{id}/status") Map<String,Object> changeStatus(@PathVariable("id") Long id,@RequestBody Map<String,String> body);}

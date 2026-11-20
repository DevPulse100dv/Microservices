package com.example.collector;
import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*; import java.util.*;
@FeignClient(name="waste-reporting-service") public interface WasteClient {
 @GetMapping("/waste/pending") List<Map<String,Object>> pending();
 @GetMapping("/waste/{id}") Map<String,Object> get(@PathVariable("id") Long id);
 @PutMapping("/waste/{id}/status") Map<String,Object> changeStatus(@PathVariable("id") Long id,@RequestBody Map<String,String> body);
}

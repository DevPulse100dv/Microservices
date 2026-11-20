package com.example.waste;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @Validated @RequestMapping("/waste")
public class WasteReportController {
    private final WasteReportService service;
    public WasteReportController(WasteReportService service){this.service=service;}
    @GetMapping public List<WasteReport> all(){return service.all();}
    @GetMapping("/pending") public List<WasteReport> pending(){return service.pending();}
    @GetMapping("/{id}") public WasteReport get(@PathVariable long id){return service.get(id);}
    @PostMapping public WasteReport create(@Valid @RequestBody ValidReportRequest body){return service.create(body.toRequest());}
    @PutMapping("/{id}") public WasteReport update(@PathVariable long id,@Valid @RequestBody ValidReportRequest body){return service.update(id,body.toRequest());}
    @RequestMapping(path="/{id}/status",method={RequestMethod.PATCH,RequestMethod.PUT}) public WasteReport status(@PathVariable long id,@Valid @RequestBody StatusRequest body){return service.changeStatus(id,body.status());}
    @DeleteMapping("/{id}") public void cancel(@PathVariable long id){service.cancel(id);}
}
record ValidReportRequest(@NotBlank String reporterName,@NotNull ReporterType reporterType,@NotNull WasteCategory wasteCategory,@Positive double quantity,@NotBlank String pickupAddress,String description){ ReportRequest toRequest(){return new ReportRequest(reporterName,reporterType,wasteCategory,quantity,pickupAddress,description);} }

package com.example.waste;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class WasteReportService {
    private final WasteReportRepository repository;
    public WasteReportService(WasteReportRepository repository){this.repository=repository;}
    public List<WasteReport> all(){return repository.findAll();}
    public List<WasteReport> pending(){return repository.findByStatus(ReportStatus.PENDING);}
    public WasteReport get(long id){return repository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Waste report not found: "+id));}
    @Transactional public WasteReport create(ReportRequest request){return repository.save(new WasteReport(request.reporterName(),request.reporterType(),request.wasteCategory(),request.quantity(),request.pickupAddress(),request.description()));}
    @Transactional public WasteReport update(long id,ReportRequest request){WasteReport r=get(id); if(r.getStatus()!=ReportStatus.PENDING)throw new ResponseStatusException(HttpStatus.CONFLICT,"Only pending reports can be edited"); r.update(request.reporterName(),request.reporterType(),request.wasteCategory(),request.quantity(),request.pickupAddress(),request.description()); return repository.save(r);}
    @Transactional public void cancel(long id){WasteReport r=get(id); if(r.getStatus()!=ReportStatus.PENDING)throw new ResponseStatusException(HttpStatus.CONFLICT,"Only pending reports can be cancelled"); r.setStatus(ReportStatus.CANCELLED); repository.save(r);}
    @Transactional public WasteReport changeStatus(long id, ReportStatus next){WasteReport r=get(id); boolean valid=switch(r.getStatus()){case PENDING->next==ReportStatus.ASSIGNED||next==ReportStatus.CANCELLED; case ASSIGNED->next==ReportStatus.SCHEDULED||next==ReportStatus.CANCELLED; case SCHEDULED->next==ReportStatus.COLLECTED||next==ReportStatus.CANCELLED; case COLLECTED->next==ReportStatus.HANDED_OVER; default->false;}; if(!valid)throw new ResponseStatusException(HttpStatus.CONFLICT,"Invalid report status transition: "+r.getStatus()+" -> "+next); r.setStatus(next); return repository.save(r);}
}
record ReportRequest(String reporterName, ReporterType reporterType, WasteCategory wasteCategory, double quantity, String pickupAddress, String description) {}
record StatusRequest(ReportStatus status) {}

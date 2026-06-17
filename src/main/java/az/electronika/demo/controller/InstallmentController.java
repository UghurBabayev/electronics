package az.electronika.demo.controller;

import az.electronika.demo.dto.InstallmentPlanResponse;
import az.electronika.demo.service.ExcelExportService;
import az.electronika.demo.service.InstallmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/installments")
@RequiredArgsConstructor
public class InstallmentController {

    private final InstallmentService service;
    private final ExcelExportService excelService;

    @GetMapping
    public List<InstallmentPlanResponse> getAll(@RequestParam(required = false) Boolean activeOnly,
                                                @RequestParam(required = false) Long customerId) {
        if (customerId != null) return service.getByCustomer(customerId);
        if (Boolean.TRUE.equals(activeOnly)) return service.getAllActive();
        return service.getAll();
    }

    @GetMapping("/overdue")
    public List<InstallmentPlanResponse> getOverdue() {
        return service.getOverdue();
    }

    @GetMapping("/{id}")
    public InstallmentPlanResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping("/payments/{paymentId}/pay")
    public ResponseEntity<InstallmentPlanResponse> markPaid(@PathVariable Long paymentId) {
        return ResponseEntity.ok(service.markPaymentPaid(paymentId));
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        byte[] bytes = excelService.exportInstallments(service.getAll());
        String filename = "nisiye-" + LocalDate.now() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}
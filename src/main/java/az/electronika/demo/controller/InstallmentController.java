package az.electronika.demo.controller;

import az.electronika.demo.dto.InstallmentPlanResponse;
import az.electronika.demo.service.InstallmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/installments")
@RequiredArgsConstructor
public class InstallmentController {

    private final InstallmentService service;

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
}
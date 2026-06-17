package az.electronika.demo.controller;

import az.electronika.demo.dto.InitialBalanceRequest;
import az.electronika.demo.dto.InitialBalanceResponse;
import az.electronika.demo.service.InitialBalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/balances")
@RequiredArgsConstructor
public class InitialBalanceController {

    private final InitialBalanceService service;

    @GetMapping
    public List<InitialBalanceResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotal() {
        return ResponseEntity.ok(service.getTotal());
    }

    @PostMapping
    public ResponseEntity<InitialBalanceResponse> create(@Valid @RequestBody InitialBalanceRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
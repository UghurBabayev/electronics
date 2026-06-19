package az.electronika.demo.controller;

import az.electronika.demo.dto.SaleRequest;
import az.electronika.demo.dto.SaleResponse;
import az.electronika.demo.entity.enums.PaymentType;
import az.electronika.demo.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService service;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) PaymentType paymentType,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(service.getPage(from, to, customerId, paymentType, search, page, size));
    }

    @PostMapping
    public ResponseEntity<SaleResponse> create(@Valid @RequestBody SaleRequest req) {
        return ResponseEntity.ok(service.create(req));
    }
}
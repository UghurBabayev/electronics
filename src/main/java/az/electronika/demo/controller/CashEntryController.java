package az.electronika.demo.controller;

import az.electronika.demo.dto.CashEntryRequest;
import az.electronika.demo.dto.CashEntryResponse;
import az.electronika.demo.service.CashEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cash-entries")
@RequiredArgsConstructor
public class CashEntryController {

    private final CashEntryService service;

    @GetMapping
    public Map<String, Object> getAll() {
        return service.getAll();
    }

    @PostMapping
    public ResponseEntity<CashEntryResponse> create(@RequestBody CashEntryRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
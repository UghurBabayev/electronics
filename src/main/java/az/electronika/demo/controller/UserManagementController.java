package az.electronika.demo.controller;

import az.electronika.demo.dto.CreateUserRequest;
import az.electronika.demo.dto.UpdateUserRequest;
import az.electronika.demo.dto.UserSummaryResponse;
import az.electronika.demo.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserManagementService service;

    @GetMapping
    public List<UserSummaryResponse> getAll() {
        return service.getAll();
    }

    @PostMapping
    public ResponseEntity<UserSummaryResponse> create(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserSummaryResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @PostMapping("/{id}/extend")
    public ResponseEntity<UserSummaryResponse> extendAccess(@PathVariable Long id) {
        return ResponseEntity.ok(service.extendAccess(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
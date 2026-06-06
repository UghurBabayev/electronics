package az.electronika.demo.controller;

import az.electronika.demo.dto.ModelRequest;
import az.electronika.demo.dto.ModelResponse;
import az.electronika.demo.service.ModelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    @GetMapping
    public List<ModelResponse> getAll() {
        return modelService.getAll();
    }

    @PostMapping
    public ModelResponse create(@Valid @RequestBody ModelRequest req) {
        return modelService.create(req);
    }

    @PutMapping("/{id}")
    public ModelResponse update(@PathVariable Long id, @Valid @RequestBody ModelRequest req) {
        return modelService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        modelService.delete(id);
    }
}
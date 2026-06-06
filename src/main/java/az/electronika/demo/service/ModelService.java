package az.electronika.demo.service;

import az.electronika.demo.dto.ModelRequest;
import az.electronika.demo.dto.ModelResponse;
import az.electronika.demo.entity.Brand;
import az.electronika.demo.entity.Category;
import az.electronika.demo.entity.Model;
import az.electronika.demo.repository.BrandRepository;
import az.electronika.demo.repository.CategoryRepository;
import az.electronika.demo.repository.ModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelRepository modelRepo;
    private final CategoryRepository categoryRepo;
    private final BrandRepository brandRepo;

    public List<ModelResponse> getAll() {
        return modelRepo.findAll().stream().map(ModelResponse::from).toList();
    }

    public ModelResponse create(ModelRequest req) {
        Model m = Model.builder()
                .name(req.name())
                .brand(resolveBrand(req.brandId()))
                .category(resolveCategory(req.categoryId()))
                .salePrice(req.salePrice())
                .build();
        return ModelResponse.from(modelRepo.save(m));
    }

    public ModelResponse update(Long id, ModelRequest req) {
        Model m = findOrThrow(id);
        m.setName(req.name());
        m.setBrand(resolveBrand(req.brandId()));
        m.setCategory(resolveCategory(req.categoryId()));
        m.setSalePrice(req.salePrice());
        return ModelResponse.from(modelRepo.save(m));
    }

    public void delete(Long id) {
        modelRepo.deleteById(id);
    }

    public Model findOrThrow(Long id) {
        return modelRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Model tapılmadı: " + id));
    }

    private Brand resolveBrand(Long id) {
        if (id == null) return null;
        return brandRepo.findById(id).orElseThrow(() -> new RuntimeException("Marka tapılmadı: " + id));
    }

    private Category resolveCategory(Long id) {
        if (id == null) return null;
        return categoryRepo.findById(id).orElseThrow(() -> new RuntimeException("Kateqoriya tapılmadı: " + id));
    }
}
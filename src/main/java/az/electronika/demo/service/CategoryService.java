package az.electronika.demo.service;

import az.electronika.demo.dto.CategoryRequest;
import az.electronika.demo.entity.Category;
import az.electronika.demo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repo;

    public List<Category> getAll() {
        return repo.findAll();
    }

    public Category create(CategoryRequest req) {
        Category cat = Category.builder().name(req.name()).build();
        return repo.save(cat);
    }

    public Category update(Long id, CategoryRequest req) {
        Category cat = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Kateqoriya tapılmadı: " + id));
        cat.setName(req.name());
        return repo.save(cat);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
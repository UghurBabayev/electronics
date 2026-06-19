package az.electronika.demo.service;

import az.electronika.demo.dto.BrandRequest;
import az.electronika.demo.entity.Brand;
import az.electronika.demo.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository repo;

    public List<Brand> getAll() {
        return repo.findAll();
    }

    public Brand create(BrandRequest req) {
        if (repo.findByName(req.name()).isPresent())
            throw new RuntimeException("Bu adda marka artıq mövcuddur: " + req.name());
        Brand brand = Brand.builder().name(req.name()).build();
        return repo.save(brand);
    }

    public Brand update(Long id, BrandRequest req) {
        Brand brand = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Marka tapılmadı: " + id));
        brand.setName(req.name());
        return repo.save(brand);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
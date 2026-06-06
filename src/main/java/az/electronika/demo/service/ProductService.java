package az.electronika.demo.service;

import az.electronika.demo.dto.ProductRequest;
import az.electronika.demo.dto.ProductResponse;
import az.electronika.demo.entity.Brand;
import az.electronika.demo.entity.Category;
import az.electronika.demo.entity.Product;
import az.electronika.demo.repository.BrandRepository;
import az.electronika.demo.repository.CategoryRepository;
import az.electronika.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final BrandRepository brandRepo;

    public List<ProductResponse> getAll() {
        return productRepo.findAll().stream().map(ProductResponse::from).toList();
    }

    public List<ProductResponse> getInStock() {
        return productRepo.findInStock().stream().map(ProductResponse::from).toList();
    }

    public List<ProductResponse> search(String name) {
        return productRepo.findByNameContainingIgnoreCase(name)
                .stream().map(ProductResponse::from).toList();
    }

    public ProductResponse getById(Long id) {
        return ProductResponse.from(findOrThrow(id));
    }

    public ProductResponse create(ProductRequest req) {
        Product p = Product.builder()
                .name(req.name())
                .category(resolveCategory(req.categoryId()))
                .brand(resolveBrand(req.brandId()))
                .purchasePrice(req.purchasePrice())
                .salePrice(req.salePrice())
                .purchaseDate(req.purchaseDate())
                .quantity(req.quantity())
                .description(req.description())
                .build();
        return ProductResponse.from(productRepo.save(p));
    }

    public ProductResponse update(Long id, ProductRequest req) {
        Product p = findOrThrow(id);
        p.setName(req.name());
        p.setCategory(resolveCategory(req.categoryId()));
        p.setBrand(resolveBrand(req.brandId()));
        p.setPurchasePrice(req.purchasePrice());
        p.setSalePrice(req.salePrice());
        p.setPurchaseDate(req.purchaseDate());
        p.setQuantity(req.quantity());
        p.setDescription(req.description());
        return ProductResponse.from(productRepo.save(p));
    }

    public void delete(Long id) {
        productRepo.deleteById(id);
    }

    public Product findOrThrow(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Məhsul tapılmadı: " + id));
    }

    private Category resolveCategory(Long id) {
        if (id == null) return null;
        return categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Kateqoriya tapılmadı: " + id));
    }

    private Brand resolveBrand(Long id) {
        if (id == null) return null;
        return brandRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Marka tapılmadı: " + id));
    }
}
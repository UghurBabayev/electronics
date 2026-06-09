package az.electronika.demo.service;

import az.electronika.demo.dto.ProductRequest;
import az.electronika.demo.dto.ProductResponse;
import az.electronika.demo.entity.Product;
import az.electronika.demo.repository.ProductRepository;
import az.electronika.demo.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepo;
    private final ModelService modelService;
    private final SecurityHelper security;

    public List<ProductResponse> getAll() {
        List<Product> list = security.isAdmin()
                ? productRepo.findAll()
                : productRepo.findByCreatedByUsername(security.currentUsername());
        return list.stream().map(ProductResponse::from).toList();
    }

    public List<ProductResponse> getInStock() {
        List<Product> list = security.isAdmin()
                ? productRepo.findInStock()
                : productRepo.findInStockByUser(security.currentUsername());
        return list.stream().map(ProductResponse::from).toList();
    }

    public List<ProductResponse> search(String name) {
        List<Product> list = security.isAdmin()
                ? productRepo.findByNameContainingIgnoreCase(name)
                : productRepo.findByCreatedByUsernameAndNameContaining(security.currentUsername(), name);
        return list.stream().map(ProductResponse::from).toList();
    }

    public ProductResponse getById(Long id) {
        return ProductResponse.from(findOrThrow(id));
    }

    public ProductResponse create(ProductRequest req) {
        Product p = Product.builder()
                .model(modelService.findOrThrow(req.modelId()))
                .purchasePrice(req.purchasePrice())
                .purchaseDate(req.purchaseDate())
                .quantity(req.quantity())
                .description(req.description())
                .createdBy(security.currentUser())
                .build();
        return ProductResponse.from(productRepo.save(p));
    }

    public ProductResponse update(Long id, ProductRequest req) {
        Product p = findOrThrow(id);
        p.setModel(modelService.findOrThrow(req.modelId()));
        p.setPurchasePrice(req.purchasePrice());
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
}
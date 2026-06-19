package az.electronika.demo.service;

import az.electronika.demo.dto.PageResponse;
import az.electronika.demo.dto.ProductRequest;
import az.electronika.demo.dto.ProductResponse;
import az.electronika.demo.entity.Product;
import az.electronika.demo.repository.ProductRepository;
import az.electronika.demo.security.SecurityHelper;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepo;
    private final ModelService modelService;
    private final SecurityHelper security;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getPage(String search, Long brandId, Long categoryId,
                                                  String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Specification<Product> spec = buildSpec(search, brandId, categoryId, status);
        Page<Product> result = productRepo.findAll(spec, pageable);
        return PageResponse.of(result.map(ProductResponse::from));
    }

    private Specification<Product> buildSpec(String search, Long brandId, Long categoryId, String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!security.isAdmin()) {
                predicates.add(cb.equal(root.get("createdBy").get("username"), security.currentUsername()));
            }

            boolean needsModel = (search != null && !search.isBlank()) || brandId != null || categoryId != null;
            if (needsModel) {
                Join<?, ?> model = root.join("model", JoinType.LEFT);
                if (search != null && !search.isBlank()) {
                    predicates.add(cb.like(cb.lower(model.get("name")), "%" + search.toLowerCase() + "%"));
                }
                if (brandId != null) {
                    predicates.add(cb.equal(model.get("brand").get("id"), brandId));
                }
                if (categoryId != null) {
                    predicates.add(cb.equal(model.get("category").get("id"), categoryId));
                }
            }

            if ("IN_STOCK".equals(status)) {
                predicates.add(cb.greaterThan(root.get("quantity"), 0));
            } else if ("SOLD".equals(status)) {
                predicates.add(cb.equal(root.get("quantity"), 0));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public List<ProductResponse> getInStock() {
        List<Product> list = security.isAdmin()
                ? productRepo.findInStock()
                : productRepo.findInStockByUser(security.currentUsername());
        return list.stream().map(ProductResponse::from).toList();
    }

    public ProductResponse getById(Long id) {
        return ProductResponse.from(findOrThrow(id));
    }

    public ProductResponse create(ProductRequest req) {
        var model = modelService.findOrThrow(req.modelId());
        var owner = security.currentUser();
        Long lastId = null;
        for (int i = 0; i < req.quantity(); i++) {
            lastId = productRepo.save(Product.builder()
                    .model(model)
                    .purchasePrice(req.purchasePrice())
                    .purchaseDate(req.purchaseDate())
                    .quantity(1)
                    .salePrice(req.salePrice())
                    .description(req.description())
                    .createdBy(owner)
                    .build()).getId();
        }
        return ProductResponse.from(findOrThrow(lastId));
    }

    public ProductResponse update(Long id, ProductRequest req) {
        Product p = findOrThrow(id);
        p.setModel(modelService.findOrThrow(req.modelId()));
        p.setPurchasePrice(req.purchasePrice());
        p.setPurchaseDate(req.purchaseDate());
        p.setSalePrice(req.salePrice());
        p.setDescription(req.description());
        productRepo.save(p);
        return ProductResponse.from(findOrThrow(id));
    }

    public void delete(Long id) {
        productRepo.deleteById(id);
    }

    public Product findOrThrow(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Məhsul tapılmadı: " + id));
    }
}
package az.electronika.demo.service;

import az.electronika.demo.dto.PageResponse;
import az.electronika.demo.dto.SaleRequest;
import az.electronika.demo.dto.SaleResponse;
import az.electronika.demo.entity.*;
import az.electronika.demo.entity.enums.InstallmentStatus;
import az.electronika.demo.entity.enums.PaymentType;
import az.electronika.demo.repository.*;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepo;
    private final ProductService productService;
    private final CustomerRepository customerRepo;
    private final InstallmentPlanRepository planRepo;
    private final InstallmentPaymentRepository paymentRepo;
    private final SecurityHelper security;

    @Transactional(readOnly = true)
    public PageResponse<SaleResponse> getPage(LocalDate from, LocalDate to,
                                               Long customerId, PaymentType paymentType,
                                               String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Specification<Sale> spec = buildSpec(from, to, customerId, paymentType, search);
        Page<Sale> result = saleRepo.findAll(spec, pageable);
        return PageResponse.of(result.map(SaleResponse::from));
    }

    private Specification<Sale> buildSpec(LocalDate from, LocalDate to,
                                           Long customerId, PaymentType paymentType, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!security.isAdmin()) {
                predicates.add(cb.equal(root.get("createdBy").get("username"), security.currentUsername()));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("saleDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("saleDate"), to));
            }
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), customerId));
            }
            if (paymentType != null) {
                predicates.add(cb.equal(root.get("paymentType"), paymentType));
            }
            if (search != null && !search.isBlank()) {
                Join<?, ?> product = root.join("product", JoinType.LEFT);
                Join<?, ?> model = product.join("model", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(model.get("name")), "%" + search.toLowerCase() + "%"));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public List<SaleResponse> getByCustomer(Long customerId) {
        List<Sale> list = security.isAdmin()
                ? saleRepo.findByCustomerId(customerId)
                : saleRepo.findByCustomerIdAndCreatedByUsername(customerId, security.currentUsername());
        return list.stream().map(SaleResponse::from).toList();
    }

    @Transactional
    public SaleResponse create(SaleRequest req) {
        Product product = productService.findOrThrow(req.productId());

        if (product.getQuantity() < req.quantity()) {
            throw new RuntimeException("Stokda yetəri qədər məhsul yoxdur. Mövcud: " + product.getQuantity());
        }

        Customer customer = req.customerId() != null
                ? customerRepo.findById(req.customerId()).orElseThrow(() -> new RuntimeException("Müştəri tapılmadı"))
                : null;

        Sale sale = Sale.builder()
                .product(product)
                .customer(customer)
                .salePrice(req.salePrice())
                .saleDate(req.saleDate())
                .paymentType(req.paymentType())
                .quantity(req.quantity())
                .note(req.note())
                .createdBy(security.currentUser())
                .build();

        sale = saleRepo.save(sale);
        product.setQuantity(product.getQuantity() - req.quantity());

        if (req.paymentType() == PaymentType.CREDIT) {
            createInstallmentPlan(sale, req);
        }

        return SaleResponse.from(sale);
    }

    private void createInstallmentPlan(Sale sale, SaleRequest req) {
        if (req.monthlyPayment() == null || req.durationMonths() == null) {
            throw new RuntimeException("Nisiyə üçün aylıq ödəniş və müddət tələb olunur");
        }

        InstallmentPlan plan = InstallmentPlan.builder()
                .sale(sale)
                .totalAmount(req.salePrice().multiply(BigDecimal.valueOf(req.quantity())))
                .monthlyPayment(req.monthlyPayment())
                .durationMonths(req.durationMonths())
                .startDate(req.saleDate().plusMonths(1))
                .paidAmount(BigDecimal.ZERO)
                .status(InstallmentStatus.ACTIVE)
                .payments(new ArrayList<>())
                .build();

        plan = planRepo.save(plan);

        List<InstallmentPayment> payments = new ArrayList<>();
        for (int i = 0; i < req.durationMonths(); i++) {
            payments.add(InstallmentPayment.builder()
                    .plan(plan)
                    .dueDate(plan.getStartDate().plusMonths(i))
                    .amount(req.monthlyPayment())
                    .isPaid(false)
                    .build());
        }
        paymentRepo.saveAll(payments);
    }
}
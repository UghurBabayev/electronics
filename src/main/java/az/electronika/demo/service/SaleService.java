package az.electronika.demo.service;

import az.electronika.demo.dto.SaleRequest;
import az.electronika.demo.dto.SaleResponse;
import az.electronika.demo.entity.*;
import az.electronika.demo.entity.enums.InstallmentStatus;
import az.electronika.demo.entity.enums.PaymentType;
import az.electronika.demo.repository.*;
import az.electronika.demo.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
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

    public List<SaleResponse> getAll() {
        List<Sale> list = security.isAdmin()
                ? saleRepo.findAll()
                : saleRepo.findByCreatedByUsername(security.currentUsername());
        return list.stream().map(SaleResponse::from).toList();
    }

    public List<SaleResponse> getByCustomer(Long customerId) {
        List<Sale> list = security.isAdmin()
                ? saleRepo.findByCustomerId(customerId)
                : saleRepo.findByCustomerIdAndCreatedByUsername(customerId, security.currentUsername());
        return list.stream().map(SaleResponse::from).toList();
    }

    public List<SaleResponse> getByDateRange(LocalDate from, LocalDate to) {
        List<Sale> list = security.isAdmin()
                ? saleRepo.findBySaleDateBetween(from, to)
                : saleRepo.findByCreatedByUsernameAndSaleDateBetween(security.currentUsername(), from, to);
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
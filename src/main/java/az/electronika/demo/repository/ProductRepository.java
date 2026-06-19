package az.electronika.demo.repository;

import az.electronika.demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @EntityGraph(attributePaths = {"model", "model.brand", "model.category", "createdBy"})
    Optional<Product> findById(Long id);

    @Query(value = "SELECT p FROM Product p WHERE p.quantity > 0",
           countQuery = "SELECT COUNT(p) FROM Product p WHERE p.quantity > 0")
    @EntityGraph(attributePaths = {"model", "model.brand", "model.category", "createdBy"})
    List<Product> findInStock();

    @Query(value = "SELECT p FROM Product p WHERE p.quantity > 0 AND p.createdBy.username = :username",
           countQuery = "SELECT COUNT(p) FROM Product p WHERE p.quantity > 0 AND p.createdBy.username = :username")
    @EntityGraph(attributePaths = {"model", "model.brand", "model.category", "createdBy"})
    List<Product> findInStockByUser(@Param("username") String username);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantity > 0")
    long countInStock();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantity > 0 AND p.createdBy.username = :username")
    long countInStockByUser(@Param("username") String username);
}
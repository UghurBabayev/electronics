package az.electronika.demo.repository;

import az.electronika.demo.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"model", "model.brand", "model.category", "createdBy"})
    List<Product> findAll();

    @EntityGraph(attributePaths = {"model", "model.brand", "model.category", "createdBy"})
    Optional<Product> findById(Long id);

    @EntityGraph(attributePaths = {"model", "model.brand", "model.category", "createdBy"})
    List<Product> findByCreatedByUsername(String username);

    @Query("SELECT p FROM Product p WHERE p.model IS NOT NULL AND LOWER(p.model.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    @EntityGraph(attributePaths = {"model", "model.brand", "model.category", "createdBy"})
    List<Product> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT p FROM Product p WHERE p.model IS NOT NULL AND LOWER(p.model.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.createdBy.username = :username")
    @EntityGraph(attributePaths = {"model", "model.brand", "model.category", "createdBy"})
    List<Product> findByCreatedByUsernameAndNameContaining(@Param("username") String username, @Param("name") String name);

    @Query("SELECT p FROM Product p WHERE p.quantity > 0")
    @EntityGraph(attributePaths = {"model", "model.brand", "model.category", "createdBy"})
    List<Product> findInStock();

    @Query("SELECT p FROM Product p WHERE p.quantity > 0 AND p.createdBy.username = :username")
    @EntityGraph(attributePaths = {"model", "model.brand", "model.category", "createdBy"})
    List<Product> findInStockByUser(@Param("username") String username);
}
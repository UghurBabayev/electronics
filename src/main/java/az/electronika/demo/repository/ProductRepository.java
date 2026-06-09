package az.electronika.demo.repository;

import az.electronika.demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.model IS NOT NULL AND LOWER(p.model.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT p FROM Product p WHERE p.model IS NOT NULL AND LOWER(p.model.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.createdBy.username = :username")
    List<Product> findByCreatedByUsernameAndNameContaining(@Param("username") String username, @Param("name") String name);

    @Query("SELECT p FROM Product p WHERE p.quantity > 0")
    List<Product> findInStock();

    @Query("SELECT p FROM Product p WHERE p.quantity > 0 AND p.createdBy.username = :username")
    List<Product> findInStockByUser(@Param("username") String username);

    List<Product> findByCreatedByUsername(String username);
}
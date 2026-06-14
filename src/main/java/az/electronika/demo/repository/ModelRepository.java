package az.electronika.demo.repository;

import az.electronika.demo.entity.Model;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModelRepository extends JpaRepository<Model, Long> {

    @EntityGraph(attributePaths = {"brand", "category"})
    List<Model> findAll();

    @EntityGraph(attributePaths = {"brand", "category"})
    Optional<Model> findById(Long id);
}
package az.electronika.demo.repository;

import az.electronika.demo.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepository extends JpaRepository<Model, Long> {
}
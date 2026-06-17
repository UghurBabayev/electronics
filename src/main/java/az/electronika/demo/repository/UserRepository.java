package az.electronika.demo.repository;

import az.electronika.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    @Query("SELECT COUNT(u) FROM User u WHERE u.accessUntil IS NOT NULL AND u.accessUntil < :today AND u.active = true")
    long countExpiredUsers(@Param("today") LocalDate today);

    @Query("SELECT COUNT(u) FROM User u WHERE u.accessUntil IS NOT NULL AND u.accessUntil BETWEEN :today AND :soon AND u.active = true")
    long countExpiringSoonUsers(@Param("today") LocalDate today, @Param("soon") LocalDate soon);
}
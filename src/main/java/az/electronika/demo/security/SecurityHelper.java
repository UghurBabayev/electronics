package az.electronika.demo.security;

import az.electronika.demo.entity.User;
import az.electronika.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityHelper {

    private final UserRepository userRepo;

    public String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public User currentUser() {
        return userRepo.findByUsername(currentUsername())
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı"));
    }

    public boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
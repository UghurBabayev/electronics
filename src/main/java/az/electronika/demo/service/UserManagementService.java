package az.electronika.demo.service;

import az.electronika.demo.dto.CreateUserRequest;
import az.electronika.demo.dto.UpdateUserRequest;
import az.electronika.demo.dto.UserSummaryResponse;
import az.electronika.demo.entity.User;
import az.electronika.demo.entity.enums.Role;
import az.electronika.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public List<UserSummaryResponse> getAll() {
        return userRepo.findAll().stream().map(UserSummaryResponse::from).toList();
    }

    public UserSummaryResponse create(CreateUserRequest req) {
        if (userRepo.existsByUsername(req.username())) {
            throw new RuntimeException("Bu istifadəçi adı artıq mövcuddur: " + req.username());
        }
        Role role;
        try { role = Role.valueOf(req.role().toUpperCase()); }
        catch (IllegalArgumentException e) { throw new RuntimeException("Yanlış rol: " + req.role()); }

        User user = User.builder()
                .username(req.username())
                .fullName(req.fullName())
                .password(passwordEncoder.encode(req.password()))
                .role(role)
                .active(true)
                .accessUntil(LocalDate.now().plusMonths(1))
                .build();
        return UserSummaryResponse.from(userRepo.save(user));
    }

    public UserSummaryResponse update(Long id, UpdateUserRequest req) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı"));

        Role role;
        try { role = Role.valueOf(req.role().toUpperCase()); }
        catch (IllegalArgumentException e) { throw new RuntimeException("Yanlış rol: " + req.role()); }

        if (user.getUsername().equals(currentUsername) && !req.active()) {
            throw new RuntimeException("Özünüzü deaktiv edə bilməzsiniz");
        }
        if (user.getUsername().equals(currentUsername) && role != user.getRole()) {
            throw new RuntimeException("Öz rolunuzu dəyişə bilməzsiniz");
        }

        user.setFullName(req.fullName());
        user.setRole(role);
        user.setActive(req.active());
        if (req.newPassword() != null && !req.newPassword().isBlank()) {
            if (req.newPassword().length() < 4) throw new RuntimeException("Şifrə minimum 4 simvol olmalıdır");
            user.setPassword(passwordEncoder.encode(req.newPassword()));
        }
        return UserSummaryResponse.from(userRepo.save(user));
    }

    public UserSummaryResponse extendAccess(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı"));
        LocalDate base = (user.getAccessUntil() != null && user.getAccessUntil().isAfter(LocalDate.now()))
                ? user.getAccessUntil()
                : LocalDate.now();
        user.setAccessUntil(base.plusMonths(1));
        return UserSummaryResponse.from(userRepo.save(user));
    }

    public void delete(Long id) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı"));
        if (user.getUsername().equals(currentUsername)) {
            throw new RuntimeException("Özünüzü silə bilməzsiniz");
        }
        userRepo.delete(user);
    }
}
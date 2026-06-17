package az.electronika.demo.controller;

import az.electronika.demo.dto.LoginRequest;
import az.electronika.demo.dto.LoginResponse;
import az.electronika.demo.security.LoginAttemptService;
import az.electronika.demo.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LoginAttemptService attemptService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);

        if (attemptService.isBlocked(ip)) {
            long secs = attemptService.secondsRemaining(ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", String.format(
                            "Çox sayda uğursuz cəhd. %d dəqiqə %d saniyə sonra yenidən cəhd edin.",
                            secs / 60, secs % 60)));
        }

        try {
            LoginResponse response = authService.login(request);
            attemptService.loginSucceeded(ip);
            return ResponseEntity.ok(response);
        } catch (AccountExpiredException ex) {
            // Şifrə düzgündür, amma müddət bitib — uğursuz cəhd kimi sayılmır
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "error", "İstifadə müddətiniz bitib. Admin ilə əlaqə saxlayın.",
                            "expired", true));
        } catch (AuthenticationException ex) {
            attemptService.loginFailed(ip);
            if (attemptService.isBlocked(ip)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Çox sayda uğursuz cəhd. 15 dəqiqə bloklandınız."));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "İstifadəçi adı və ya şifrə yanlışdır."));
        }
    }

    private String getClientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
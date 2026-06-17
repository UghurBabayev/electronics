package az.electronika.demo.service;

import az.electronika.demo.dto.LoginRequest;
import az.electronika.demo.dto.LoginResponse;
import az.electronika.demo.entity.User;
import az.electronika.demo.repository.UserRepository;
import az.electronika.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public LoginResponse login(LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userRepository.findByUsername(request.username()).orElseThrow();

        if (user.getAccessUntil() != null && LocalDate.now().isAfter(user.getAccessUntil())) {
            throw new AccountExpiredException("expired");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        String token = jwtUtil.generateToken(userDetails);

        return new LoginResponse(
                token,
                user.getUsername(),
                user.getFullName(),
                user.getRole().name()
        );
    }
}
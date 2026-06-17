package az.electronika.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;

@Component
public class ExpiryCheckFilter extends OncePerRequestFilter {

    @Value("${app.expiry-date:}")
    private String expiryDateStr;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (expiryDateStr != null && !expiryDateStr.isBlank()) {
            LocalDate expiry = LocalDate.parse(expiryDateStr);
            if (LocalDate.now().isAfter(expiry)) {
                response.setStatus(402);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"error\":\"Lisenziya müddəti bitib. Əlaqə saxlayın.\",\"expired\":true}"
                );
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
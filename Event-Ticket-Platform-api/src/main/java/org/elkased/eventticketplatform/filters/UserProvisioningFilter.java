package org.elkased.eventticketplatform.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.elkased.eventticketplatform.domain.entities.User;
import org.elkased.eventticketplatform.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserProvisioningFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request
            , HttpServletResponse response
            , FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt jwt) {
            UUID keycloakId = jwt.getSubject().transform(UUID::fromString);
            Map<String, Object> claims = jwt.getClaims();
            if (!userRepository.existsById(keycloakId)) {
                User user = new User();
                user.setId(keycloakId);
                user.setName(claims.get("preferred_username").toString());
                user.setEmail(claims.get("email").toString());
                userRepository.save(user);
            }
        }

        filterChain.doFilter(request, response);
    }
}

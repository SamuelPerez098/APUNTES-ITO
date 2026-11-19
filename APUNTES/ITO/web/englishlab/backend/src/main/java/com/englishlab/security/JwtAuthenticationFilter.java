package com.englishlab.security;

import com.englishlab.repository.RevokedTokenRepository;
import com.englishlab.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro JWT: valida el token, rechaza si su `jti` esta revocado o si el usuario ya no
 * existe, y deja el SecurityContext con el principal y el rol ACTUAL de la BD.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final RevokedTokenRepository revokedTokenRepository;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            RevokedTokenRepository revokedTokenRepository,
            UserRepository userRepository) {
        this.jwtService = jwtService;
        this.revokedTokenRepository = revokedTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            try {
                AuthenticatedUser claims = jwtService.parseToken(token);
                if (!revokedTokenRepository.existsByJti(claims.jti())) {
                    // El usuario debe seguir existiendo y el rol se toma de la BD, no del claim:
                    // un token de un usuario borrado o degradado deja de valer al instante.
                    userRepository.findById(claims.id()).ifPresent(entity -> {
                        AuthenticatedUser user = new AuthenticatedUser(
                                entity.getId(), entity.getRole().getName(), claims.jti(), claims.expiresAt());
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name())));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
                }
                // Si el jti esta revocado o el usuario no existe, no se autentica -> 401.
            } catch (JwtException | IllegalArgumentException e) {
                // Token invalido/expirado: se deja sin autenticar -> 401.
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}

package com.cloud.yagodev.helpdesk.config;

import com.cloud.yagodev.helpdesk.entities.Usuario;
import com.cloud.yagodev.helpdesk.repositories.UsuarioRepository;
import com.cloud.yagodev.helpdesk.services.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final UsuarioRepository repo;

    public JwtAuthenticationFilter(JwtService jwt, UsuarioRepository repo) {
        this.jwt = jwt;
        this.repo = repo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        // deixa OPTIONS passar (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        // 1) Authorization: Bearer xxx  ||  2) ?access_token=xxx (usado pelo EventSource/SSE)
        String token = null;
        String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        if (token == null) {
            token = req.getParameter("access_token");
        }

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                var jws = jwt.parse(token);
                if (jwt.isAccess(jws)) {
                    UUID userId = jwt.subjectAsUuid(jws);
                    Usuario u = repo.findById(userId).orElse(null);
                    if (u != null && u.isAtivo()) {
                        var authorities = u.getRoles().stream()
                                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                                .toList();
                        var principal = new UsernamePasswordAuthenticationToken(
                                u.getEmail(), null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(principal);
                    }
                }
            } catch (JwtException ignored) {
                // token inválido/expirado → segue sem auth
            }
        }

        chain.doFilter(req, res);
    }
}

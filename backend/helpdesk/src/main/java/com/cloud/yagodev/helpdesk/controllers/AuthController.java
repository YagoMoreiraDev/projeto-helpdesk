package com.cloud.yagodev.helpdesk.controllers;

import com.cloud.yagodev.helpdesk.entities.Usuario;
import com.cloud.yagodev.helpdesk.enums.Role;
import com.cloud.yagodev.helpdesk.repositories.UsuarioRepository;
import com.cloud.yagodev.helpdesk.services.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final UsuarioRepository repo;
    private final JwtService jwt;

    public AuthController(AuthenticationManager am, UsuarioRepository r, JwtService j) {
        this.authManager = am; this.repo = r; this.jwt = j;
    }

    // DTOs
    public record LoginRequest(String email, String senha) {}
    public record UserPayload(UUID id, String nome, String email, Set<Role> roles) {}
    public record AuthResponse(String tokenType, String accessToken, long expiresIn, UserPayload user) {}

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest req, HttpServletResponse res) {
        var auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.senha()));
        Usuario u = repo.findByEmailIgnoreCase(req.email()).orElseThrow();
        var roles = u.getRoles().stream().map(Enum::name).toList();

        String access = jwt.generateAccess(u.getId(), u.getEmail(), roles);
        String refresh = jwt.generateRefresh(u.getId());

        // refresh em cookie HttpOnly
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refresh)
                .httpOnly(true)
                .secure(false)   // true em HTTPS
                .sameSite("Lax")
                .path("/auth")
                .maxAge(Duration.ofSeconds(2592000))
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        var body = new AuthResponse("Bearer", access, 900L,
                new UserPayload(u.getId(), u.getNome(), u.getEmail(), u.getRoles()));
        return ResponseEntity.ok(body);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = "refresh_token", required = false) String rt) {
        if (rt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        var jws = jwt.parse(rt);
        if (!jwt.isRefresh(jws)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        UUID userId = jwt.subjectAsUuid(jws);
        Usuario u = repo.findById(userId).orElseThrow();
        var roles = u.getRoles().stream().map(Enum::name).toList();

        String access = jwt.generateAccess(u.getId(), u.getEmail(), roles);
        var body = new AuthResponse("Bearer", access, 900L,
                new UserPayload(u.getId(), u.getNome(), u.getEmail(), u.getRoles()));
        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse res) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true).secure(false).sameSite("Lax").path("/auth").maxAge(0).build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }
}

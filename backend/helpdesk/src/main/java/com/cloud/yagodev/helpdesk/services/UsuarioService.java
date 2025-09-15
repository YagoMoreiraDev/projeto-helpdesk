package com.cloud.yagodev.helpdesk.services;

import com.cloud.yagodev.helpdesk.dtos.*;
import com.cloud.yagodev.helpdesk.entities.Usuario;
import com.cloud.yagodev.helpdesk.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public UsuarioService(UsuarioRepository usuarioRepo, PasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // ------------- PRIMEIRO ACESSO: etapa 1 (gerar token) -------------
    @Transactional
    public GenericMessage startFirstAccess(FirstAccessStartRequest req) {
        String email = req.email().trim();
        usuarioRepo.findByEmailIgnoreCase(email).ifPresent(u -> {
            if (!u.isAtivo()) return; // opcional: ignore inativos

            // token curto legível (6-8 chars) ou UUID; aqui: 6 alfanuméricos
            String token = generateCode(6);
            u.setResetToken(token);
            u.setResetTokenExpires(Instant.now().plus(15, ChronoUnit.MINUTES));

            // TODO: enviar e-mail com token (ou link) aqui
            // ex.: notifications.sendEmail(u.getEmail(), "Seu código de primeiro acesso é: " + token);
        });

        // segurança: não revelar se o e-mail existe
        return new GenericMessage("Se o e-mail existir, enviaremos instruções para definir a senha.");
    }

    // ------------- PRIMEIRO ACESSO: etapa 2 (confirmar e definir senha) -------------
    @Transactional
    public GenericMessage completeFirstAccess(FirstAccessCompleteRequest req) {
        var userOpt = usuarioRepo.findByEmailIgnoreCase(req.email().trim());
        if (userOpt.isEmpty()) {
            return new GenericMessage("Token inválido ou expirado.");
        }

        Usuario u = userOpt.get();
        if (u.getResetToken() == null || u.getResetTokenExpires() == null) {
            return new GenericMessage("Token inválido ou expirado.");
        }

        boolean tokenOk = u.getResetToken().equalsIgnoreCase(req.token().trim());
        boolean prazoOk = Instant.now().isBefore(u.getResetTokenExpires());

        if (!tokenOk || !prazoOk) {
            return new GenericMessage("Token inválido ou expirado.");
        }

        // define nova senha
        u.setSenhaHash(passwordEncoder.encode(req.novaSenha().trim()));
        // limpa token
        u.setResetToken(null);
        u.setResetTokenExpires(null);
        // opcional: garantir ativo
        u.setAtivo(true);

        return new GenericMessage("Senha definida com sucesso. Você já pode fazer login.");
    }

    @Transactional
    public UsuarioResponse criar(UsuarioCreateRequest req) {
        if (usuarioRepo.existsByEmailIgnoreCase(req.email())) {
            throw new IllegalArgumentException("E-mail já em uso");
        }
        String hash = passwordEncoder.encode(req.senha());
        Usuario u = new Usuario(req.nome(), req.email(), hash, new HashSet<>(req.roles()));
        u.setAtivo(true);
        u = usuarioRepo.save(u);
        return toResponse(u);
    }

    @Transactional
    public UsuarioResponse atualizar(UUID id, UsuarioUpdateRequest req) {
        Usuario u = get(id);
        u.setNome(req.nome());
        u.setEmail(req.email());
        u.setRoles(new HashSet<>(req.roles()));
        return toResponse(u);
    }

    @Transactional
    public void deletarLogico(UUID id) {
        Usuario u = get(id);
        u.setAtivo(false);
    }


    @Transactional(readOnly = true)
    public UsuarioResponse buscar(UUID id) {
        return toResponse(get(id));
    }


    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarAtivos() {
        return usuarioRepo.findByAtivoTrue().stream().map(this::toResponse).toList();
    }


    private Usuario get(UUID id) {
        return usuarioRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
    }


    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.isAtivo(), Set.copyOf(u.getRoles()));
    }

    // utilitário para token curto
    private String generateCode(int len) {
        final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sem 0,O,1,I
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        return sb.toString();
    }
}

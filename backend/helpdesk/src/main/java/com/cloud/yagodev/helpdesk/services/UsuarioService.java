package com.cloud.yagodev.helpdesk.services;

import com.cloud.yagodev.helpdesk.dtos.UsuarioCreateRequest;
import com.cloud.yagodev.helpdesk.dtos.UsuarioResponse;
import com.cloud.yagodev.helpdesk.dtos.UsuarioUpdateRequest;
import com.cloud.yagodev.helpdesk.entities.Usuario;
import com.cloud.yagodev.helpdesk.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepo, PasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.passwordEncoder = passwordEncoder;
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
}

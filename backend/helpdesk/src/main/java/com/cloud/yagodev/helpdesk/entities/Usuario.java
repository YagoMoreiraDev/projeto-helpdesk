package com.cloud.yagodev.helpdesk.entities;

import com.cloud.yagodev.helpdesk.enums.Role;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tb_usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(nullable = false)
    private String nome;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String senhaHash;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_usuario_role", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<Role> roles = new HashSet<>();


    @Column(nullable = false)
    private boolean ativo = true;

    // -------- PRIMEIRO ACESSO / RESET --------
    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expires")
    private Instant resetTokenExpires;


    protected Usuario() {}


    public Usuario(String nome, String email, String senhaHash, Set<Role> roles) {
        this.nome = nome; this.email = email; this.senhaHash = senhaHash; this.roles.addAll(roles);
    }


    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenhaHash() { return senhaHash; }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public boolean hasRole(Role r) { return roles.contains(r); }
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public Instant getResetTokenExpires() { return resetTokenExpires; }
    public void setResetTokenExpires(Instant resetTokenExpires) { this.resetTokenExpires = resetTokenExpires; }
}

package com.cloud.yagodev.helpdesk.entities;

import com.cloud.yagodev.helpdesk.enums.Prioridade;
import com.cloud.yagodev.helpdesk.enums.StatusChamado;
import com.cloud.yagodev.helpdesk.enums.TipoEvento;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_chamado")
public class Chamado {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id")
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_responsavel_id")
    private Usuario tecnicoResponsavel; // pode ser null até assumir/designar

    @Column(nullable = false) private String titulo;
    @Column(nullable = false, length = 4000) private String descricao;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private StatusChamado status;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Prioridade prioridade;

    @Column(nullable = false) private Instant createdAt;
    private Instant closedAt;

    @OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("quando ASC")
    private List<ChamadoEvento> eventos = new ArrayList<>();

    protected Chamado() {}

    public Chamado(Usuario solicitante, String titulo, String descricao, Prioridade prioridade) {
        this.solicitante = solicitante;
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.status = StatusChamado.ABERTO;
        this.createdAt = Instant.now();
    }

    // Regras de domínio (métodos ricos):
    public void atribuirTecnico(Usuario tecnico, Usuario autor) {
        if (this.status == StatusChamado.CONCLUIDO || this.status == StatusChamado.CANCELADO) {
            throw new IllegalStateException("Não é possível atribuir técnico a chamado encerrado/cancelado.");
        }
        this.tecnicoResponsavel = tecnico;
        registrarEvento(TipoEvento.ATRIBUICAO, autor,
                "Técnico " + tecnico.getNome() + " assumiu o chamado.");

        if (this.status == StatusChamado.ABERTO) {
            alterarStatus(StatusChamado.EM_ATENDIMENTO, autor, "Chamado em atendimento após atribuição.");
        }
    }

    public void alterarStatus(StatusChamado novoStatus, Usuario autor, String detalhe) {
        StatusChamado anterior = this.status;
        if (anterior == novoStatus) return;
        this.status = novoStatus;
        if (novoStatus == StatusChamado.CONCLUIDO || novoStatus == StatusChamado.CANCELADO) {
            this.closedAt = Instant.now();
        }
        registrarEventoStatus(autor, anterior, novoStatus, detalhe);
    }

    public void comentar(Usuario autor, String mensagem) {
        registrarEvento(TipoEvento.COMENTARIO, autor, mensagem);
    }

    private void registrarEvento(TipoEvento tipo, Usuario autor, String detalhe) {
        ChamadoEvento ev = new ChamadoEvento(this, tipo, autor, detalhe, null, null);
        this.eventos.add(ev);
    }

    private void registrarEventoStatus(Usuario autor, StatusChamado de, StatusChamado para, String detalhe) {
        ChamadoEvento ev = new ChamadoEvento(this, TipoEvento.STATUS_ALTERADO, autor, detalhe, de, para);
        this.eventos.add(ev);
    }

    // getters principais
    public UUID getId() { return id; }
    public Usuario getSolicitante() { return solicitante; }
    public Usuario getTecnicoResponsavel() { return tecnicoResponsavel; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public StatusChamado getStatus() { return status; }
    public Prioridade getPrioridade() { return prioridade; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getClosedAt() { return closedAt; }
    public List<ChamadoEvento> getEventos() { return Collections.unmodifiableList(eventos); }
}

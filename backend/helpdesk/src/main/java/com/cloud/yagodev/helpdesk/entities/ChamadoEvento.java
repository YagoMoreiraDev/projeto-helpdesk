package com.cloud.yagodev.helpdesk.entities;

import com.cloud.yagodev.helpdesk.enums.StatusChamado;
import com.cloud.yagodev.helpdesk.enums.TipoEvento;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_chamado_evento")
public class ChamadoEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id")
    private Chamado chamado;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TipoEvento tipo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @Column(nullable = false)
    private Instant quando;

    @Column(length = 2000)
    private String detalhe;

    @Enumerated(EnumType.STRING)
    private StatusChamado statusAnterior;

    @Enumerated(EnumType.STRING)
    private StatusChamado statusNovo;

    protected ChamadoEvento() {}

    public ChamadoEvento(Chamado chamado,
                         TipoEvento tipo,
                         Usuario autor,
                         String detalhe,
                         StatusChamado statusAnterior,
                         StatusChamado statusNovo) {
        this.chamado = chamado;
        this.tipo = tipo;
        this.autor = autor;
        this.detalhe = detalhe;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.quando = Instant.now();
    }

    // getters
    public UUID getId() { return id; }
    public Chamado getChamado() { return chamado; }
    public TipoEvento getTipo() { return tipo; }
    public Usuario getAutor() { return autor; }
    public Instant getQuando() { return quando; }
    public String getDetallhe() { return detalhe; }
    public StatusChamado getStatusAnterior() { return statusAnterior; }
    public StatusChamado getStatusNovo() { return statusNovo; }
}

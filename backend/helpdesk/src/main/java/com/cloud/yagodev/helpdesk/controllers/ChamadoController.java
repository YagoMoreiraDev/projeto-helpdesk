package com.cloud.yagodev.helpdesk.controllers;

import com.cloud.yagodev.helpdesk.dtos.*;
import com.cloud.yagodev.helpdesk.entities.Chamado;
import com.cloud.yagodev.helpdesk.entities.ChamadoEvento;
import com.cloud.yagodev.helpdesk.services.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chamados")
public class ChamadoController {

    private final ChamadoService chamadoService;

    public ChamadoController(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    @PostMapping
    public ResponseEntity<ChamadoResponse> abrir(@RequestParam UUID solicitanteId,
                                                 @RequestBody @Valid ChamadoCreateRequest req) {
        Chamado c = chamadoService.abrirChamado(solicitanteId, req);
        return ResponseEntity.ok(toResponse(c));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ChamadoResponse> alterarStatus(@PathVariable("id") UUID chamadoId,
                                                         @RequestParam UUID autorId,
                                                         @RequestBody @Valid StatusChangeRequest req) {
        Chamado c = chamadoService.alterarStatus(autorId, chamadoId, req);
        return ResponseEntity.ok(toResponse(c));
    }

    // Técnico assume chamado
    @PostMapping("/{id}/assumir")
    public ResponseEntity<ChamadoResponse> assumir(@PathVariable("id") UUID chamadoId,
                                                   @RequestParam UUID tecnicoId) {
        Chamado c = chamadoService.assumir(tecnicoId, chamadoId);
        return ResponseEntity.ok(toResponse(c));
    }

    // Comentário
    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ChamadoResponse> comentar(@PathVariable("id") UUID chamadoId,
                                                    @RequestParam UUID autorId,
                                                    @RequestBody @Valid ComentarioRequest req) {
        Chamado c = chamadoService.comentar(autorId, chamadoId, req);
        return ResponseEntity.ok(toResponse(c));
    }


    // Listagens simples para estudar o fluxo
    @GetMapping("/meus")
    public ResponseEntity<List<ChamadoResponse>> meus(@RequestParam UUID solicitanteId) {
        return ResponseEntity.ok(chamadoService.listarMeusChamados(solicitanteId).stream()
                .map(this::toResponse).toList());
    }


    @GetMapping("/tecnico")
    public ResponseEntity<List<ChamadoResponse>> porTecnico(@RequestParam UUID tecnicoId) {
        return ResponseEntity.ok(chamadoService.listarPorTecnico(tecnicoId).stream()
                .map(this::toResponse).toList());
    }


    @GetMapping
    public ResponseEntity<List<ChamadoResponse>> todos() {
        return ResponseEntity.ok(chamadoService.listarTodos().stream()
                .map(this::toResponse).toList());
    }


    // -------------------- mapeadores --------------------
    private ChamadoResponse toResponse(Chamado c) {
        var eventos = c.getEventos().stream().map(ev -> new ChamadoEventoResponse(
                ev.getId(),
                ev.getQuando(),
                ev.getTipo(),
                ev.getAutor() != null ? ev.getAutor().getNome() : null,
// dependendo do seu getter (detalhe/detallhe), ajuste aqui:
// ev.getDetalhe()
// abaixo uso reflexão simples para não travar compilação se seu domínio tiver outro nome
                safeDetalhe(ev),
                ev.getStatusAnterior(),
                ev.getStatusNovo()
        )).toList();


        return new ChamadoResponse(
                c.getId(),
                c.getTitulo(),
                c.getDescricao(),
                c.getStatus(),
                c.getPrioridade(),
                c.getSolicitante() != null ? c.getSolicitante().getId() : null,
                c.getSolicitante() != null ? c.getSolicitante().getNome() : null,
                c.getTecnicoResponsavel() != null ? c.getTecnicoResponsavel().getId() : null,
                c.getTecnicoResponsavel() != null ? c.getTecnicoResponsavel().getNome() : null,
                c.getCreatedAt(),
                c.getClosedAt(),
                eventos
        );
    }


    private String safeDetalhe(ChamadoEvento ev) {
        try {
            var m = ev.getClass().getMethod("getDetalhe");
            return (String) m.invoke(ev);
        } catch (Exception ignore) {
            try {
                var m2 = ev.getClass().getMethod("getDetallhe"); // fallback caso tenha escrito com 2 L
                return (String) m2.invoke(ev);
            } catch (Exception e2) {
                return null;
            }
        }
    }
}

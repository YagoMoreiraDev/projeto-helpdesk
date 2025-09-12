package com.cloud.yagodev.helpdesk.controllers;

import com.cloud.yagodev.helpdesk.dtos.stats.CountByDay;
import com.cloud.yagodev.helpdesk.dtos.stats.CountByMonth;
import com.cloud.yagodev.helpdesk.dtos.stats.CountByStatus;
import com.cloud.yagodev.helpdesk.dtos.stats.CountByTech;
import com.cloud.yagodev.helpdesk.services.ChamadoStatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/chamados/stats")
@PreAuthorize("hasRole('ADMIN')") // todo o bloco só para ADMIN
public class ChamadoStatsController {

    private final ChamadoStatsService service;

    public ChamadoStatsController(ChamadoStatsService service) {
        this.service = service;
    }

    // Query params: from=YYYY-MM-DD&to=YYYY-MM-DD
    private record RangeParams(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {}

    private RangeParams normalize(LocalDate from, LocalDate to){
        var f = (from != null) ? from : LocalDate.now();
        var t = (to   != null) ? to   : LocalDate.now();
        if (t.isBefore(f)) t = f;
        return new RangeParams(f, t);
    }

    @GetMapping("/status")
    public List<CountByStatus> byStatus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        var r = normalize(from, to);
        return service.totalsByStatus(r.from(), r.to());
    }

    @GetMapping("/tecnicos-ativos")
    public List<CountByTech> byTechActive(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        var r = normalize(from, to);
        return service.totalsByTechActive(r.from(), r.to());
    }

    @GetMapping("/por-dia")
    public List<CountByDay> perDay(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        var r = normalize(from, to);
        return service.totalsPerDay(r.from(), r.to());
    }

    @GetMapping("/por-mes")
    public List<CountByMonth> perMonth(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        var r = normalize(from, to);
        return service.totalsPerMonth(r.from(), r.to());
    }
}

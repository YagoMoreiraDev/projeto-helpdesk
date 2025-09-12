package com.cloud.yagodev.helpdesk.services;

import com.cloud.yagodev.helpdesk.dtos.stats.CountByDay;
import com.cloud.yagodev.helpdesk.dtos.stats.CountByMonth;
import com.cloud.yagodev.helpdesk.dtos.stats.CountByStatus;
import com.cloud.yagodev.helpdesk.dtos.stats.CountByTech;
import com.cloud.yagodev.helpdesk.enums.StatusChamado;
import com.cloud.yagodev.helpdesk.repositories.ChamadoRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Service
public class ChamadoStatsService {
    private final ChamadoRepository repo;
    private final ZoneId zone = ZoneId.systemDefault(); // ajuste se quiser UTC

    public ChamadoStatsService(ChamadoRepository repo) {
        this.repo = repo;
    }

    // Helpers para converter LocalDate -> Intervalo Instant [start, end)
    private record Range(Instant start, Instant end) {}
    private Range toRange(LocalDate from, LocalDate to) {
        var start = from.atStartOfDay(zone).toInstant();
        var end   = to.plusDays(1).atStartOfDay(zone).toInstant(); // exclusivo
        return new Range(start, end);
    }

    public List<CountByStatus> totalsByStatus(LocalDate from, LocalDate to) {
        var r = toRange(from, to);
        return repo.countByStatus(r.start, r.end);
    }

    public List<CountByTech> totalsByTechActive(LocalDate from, LocalDate to) {
        var r = toRange(from, to);
        var statuses = new ArrayList<StatusChamado>(EnumSet.of(
                StatusChamado.ABERTO, StatusChamado.EM_ATENDIMENTO));
        return repo.countByTechAndStatusIn(r.start, r.end, statuses);
    }

    public List<CountByDay> totalsPerDay(LocalDate from, LocalDate to) {
        var r = toRange(from, to);
        return repo.countPerDay(r.start, r.end).stream()
                .map(tb -> new CountByDay(LocalDateTime.ofInstant(tb.getBucket(), zone).toLocalDate(),
                        tb.getTotal()))
                .toList();
    }

    public List<CountByMonth> totalsPerMonth(LocalDate from, LocalDate to) {
        var r = toRange(from, to);
        return repo.countPerMonth(r.start, r.end).stream()
                .map(tb -> {
                    var ld = LocalDateTime.ofInstant(tb.getBucket(), zone).toLocalDate();
                    return new CountByMonth(ld.getYear() + "-" + String.format("%02d", ld.getMonthValue()), tb.getTotal());
                })
                .toList();
    }
}

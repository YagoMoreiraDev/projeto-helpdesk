package com.cloud.yagodev.helpdesk.repositories;

import com.cloud.yagodev.helpdesk.dtos.stats.TimeBucketCount;
import com.cloud.yagodev.helpdesk.entities.Chamado;
import com.cloud.yagodev.helpdesk.enums.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, UUID> {
    List<Chamado> findBySolicitanteId(UUID solicitanteId);
    List<Chamado> findByTecnicoResponsavelId(UUID tecnicoId);
    List<Chamado> findByStatus(StatusChamado status);

    // Totais por status (faixa de datas)
    @Query("""
        select new com.cloud.yagodev.helpdesk.dtos.stats.CountByStatus(c.status, count(c))
        from Chamado c
        where c.createdAt >= :start and c.createdAt < :end
        group by c.status
    """)
    List<com.cloud.yagodev.helpdesk.dtos.stats.CountByStatus> countByStatus(
            @Param("start") Instant start, @Param("end") Instant end);

    // Totais por técnico (somente com técnico != null), filtrando por status
    @Query("""
        select new com.cloud.yagodev.helpdesk.dtos.stats.CountByTech(
            c.tecnicoResponsavel.id, c.tecnicoResponsavel.nome, count(c))
        from Chamado c
        where c.tecnicoResponsavel is not null
          and c.status in :statuses
          and c.createdAt >= :start and c.createdAt < :end
        group by c.tecnicoResponsavel.id, c.tecnicoResponsavel.nome
        order by count(c) desc
    """)
    List<com.cloud.yagodev.helpdesk.dtos.stats.CountByTech> countByTechAndStatusIn(
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("statuses") List<StatusChamado> statuses);

    // Bucket por dia (PostgreSQL)
    @Query(value = """
        select date_trunc('day', c.created_at) as bucket, count(*) as total
        from tb_chamado c
        where c.created_at >= :start and c.created_at < :end
        group by bucket
        order by bucket
    """, nativeQuery = true)
    List<TimeBucketCount> countPerDay(@Param("start") Instant start, @Param("end") Instant end);

    // Bucket por mês (PostgreSQL)
    @Query(value = """
        select date_trunc('month', c.created_at) as bucket, count(*) as total
        from tb_chamado c
        where c.created_at >= :start and c.created_at < :end
        group by bucket
        order by bucket
    """, nativeQuery = true)
    List<TimeBucketCount> countPerMonth(@Param("start") Instant start, @Param("end") Instant end);

    // Lista pares {id, codigo} para todos
    @Query(value = """
        select c.id as id, right(replace(c.id::text, '-', ''), 6) as codigo
        from tb_chamado c
    """, nativeQuery = true)
    List<ChamadoShortCodeProjection> listShortCodes();

    // Busca chamados cujo código curto == :codigo (case-insensitive)
    @Query(value = """
        select *
        from tb_chamado c
        where right(replace(c.id::text, '-', ''), 6) ilike :codigo
    """, nativeQuery = true)
    List<Chamado> findByShortCode(@Param("codigo") String codigo);
}

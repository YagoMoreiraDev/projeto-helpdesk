package com.cloud.yagodev.helpdesk.repositories;

import com.cloud.yagodev.helpdesk.dtos.ComentarioSearchResponse;
import com.cloud.yagodev.helpdesk.entities.ChamadoEvento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChamadoEventoRepository extends JpaRepository<ChamadoEvento, UUID> {
    // ADMIN: busca em todos
    @Query("""
        select new com.cloud.yagodev.helpdesk.dtos.ComentarioSearchResponse(
            e.id, e.quando, e.detalhe,
            c.id, c.titulo,
            a.id, a.nome
        )
        from ChamadoEvento e
        join e.chamado c
        join e.autor a
        where e.tipo = com.cloud.yagodev.helpdesk.enums.TipoEvento.COMENTARIO
          and e.detalhe is not null
          and lower(e.detalhe) like lower(concat('%', :q, '%'))
        order by e.quando desc
    """)
    Page<ComentarioSearchResponse> searchComentariosAll(@Param("q") String q, Pageable pageable);

    // USUÁRIO_COMUM: só chamados dele
    @Query("""
        select new com.cloud.yagodev.helpdesk.dtos.ComentarioSearchResponse(
            e.id, e.quando, e.detalhe,
            c.id, c.titulo,
            a.id, a.nome
        )
        from ChamadoEvento e
        join e.chamado c
        join e.autor a
        where e.tipo = com.cloud.yagodev.helpdesk.enums.TipoEvento.COMENTARIO
          and e.detalhe is not null
          and lower(e.detalhe) like lower(concat('%', :q, '%'))
          and c.solicitante.id = :userId
        order by e.quando desc
    """)
    Page<ComentarioSearchResponse> searchComentariosDoSolicitante(
            @Param("userId") UUID userId, @Param("q") String q, Pageable pageable);

    // TÉCNICO: só chamados onde é o responsável
    @Query("""
        select new com.cloud.yagodev.helpdesk.dtos.ComentarioSearchResponse(
            e.id, e.quando, e.detalhe,
            c.id, c.titulo,
            a.id, a.nome
        )
        from ChamadoEvento e
        join e.chamado c
        join e.autor a
        where e.tipo = com.cloud.yagodev.helpdesk.enums.TipoEvento.COMENTARIO
          and e.detalhe is not null
          and lower(e.detalhe) like lower(concat('%', :q, '%'))
          and c.tecnicoResponsavel.id = :userId
        order by e.quando desc
    """)
    Page<ComentarioSearchResponse> searchComentariosDoTecnico(
            @Param("userId") UUID userId, @Param("q") String q, Pageable pageable);
}

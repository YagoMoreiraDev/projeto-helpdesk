package com.cloud.yagodev.helpdesk.repositories;

import com.cloud.yagodev.helpdesk.entities.Chamado;
import com.cloud.yagodev.helpdesk.enums.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, UUID> {
    List<Chamado> findBySolicitanteId(UUID solicitanteId);
    List<Chamado> findByTecnicoResponsavelId(UUID tecnicoId);
    List<Chamado> findByStatus(StatusChamado status);
}

package com.cloud.yagodev.helpdesk.repositories;

import com.cloud.yagodev.helpdesk.entities.ChamadoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChamadoEventoRepository extends JpaRepository<ChamadoEvento, UUID> {
}

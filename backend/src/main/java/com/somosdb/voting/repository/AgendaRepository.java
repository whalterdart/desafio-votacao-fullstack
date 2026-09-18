package com.somosdb.voting.repository;

import com.somosdb.voting.domain.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AgendaRepository extends JpaRepository<Agenda, UUID> {}


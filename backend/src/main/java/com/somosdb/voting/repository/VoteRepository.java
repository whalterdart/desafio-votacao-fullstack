package com.somosdb.voting.repository;

import com.somosdb.voting.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, UUID> {
    boolean existsByAgendaIdAndAssociateId(UUID agendaId, String associateId);

    @Query("select v.choice as choice, count(v.id) as total from Vote v where v.agenda.id = :agendaId group by v.choice")
    List<VoteCount> countByChoice(@Param("agendaId") UUID agendaId);
}


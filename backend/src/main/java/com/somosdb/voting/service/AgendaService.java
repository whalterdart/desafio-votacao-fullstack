package com.somosdb.voting.service;

import com.somosdb.voting.api.dto.*;
import com.somosdb.voting.domain.*;
import com.somosdb.voting.eligibility.*;
import com.somosdb.voting.exception.*;
import com.somosdb.voting.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class AgendaService {
    private static final Logger log = LoggerFactory.getLogger(AgendaService.class);
    private final AgendaRepository agendas;
    private final VotingSessionRepository sessions;
    private final VoteRepository votes;
    private final VotingEligibilityGateway eligibility;
    private final Clock clock;

    public AgendaService(AgendaRepository agendas, VotingSessionRepository sessions, VoteRepository votes,
                         VotingEligibilityGateway eligibility, Clock clock) {
        this.agendas = agendas;
        this.sessions = sessions;
        this.votes = votes;
        this.eligibility = eligibility;
        this.clock = clock;
    }

    @Transactional
    public AgendaResponse create(CreateAgendaRequest request) {
        Agenda agenda = new Agenda(UUID.randomUUID(), request.title().trim(), trimToNull(request.description()), Instant.now(clock));
        agendas.save(agenda);
        log.info("Pauta criada: agendaId={}", agenda.getId());
        return toResponse(agenda, null);
    }

    @Transactional(readOnly = true)
    public List<AgendaResponse> list() {
        return agendas.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(agenda -> toResponse(agenda, sessions.findByAgendaId(agenda.getId()).orElse(null)))
                .toList();
    }

    @Transactional(readOnly = true)
    public AgendaResponse find(UUID agendaId) {
        Agenda agenda = requireAgenda(agendaId);
        return toResponse(agenda, sessions.findByAgendaId(agendaId).orElse(null));
    }

    @Transactional
    public SessionResponse openSession(UUID agendaId, OpenSessionRequest request) {
        Agenda agenda = requireAgenda(agendaId);
        sessions.findByAgendaId(agendaId).ifPresent(existing -> {
            throw new ConflictException("Esta pauta já possui uma sessão de votação");
        });
        Instant openedAt = Instant.now(clock);
        VotingSession session = new VotingSession(UUID.randomUUID(), agenda, openedAt,
                openedAt.plusSeconds(request.durationOrDefault()));
        try {
            sessions.saveAndFlush(session);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Esta pauta já possui uma sessão de votação");
        }
        log.info("Sessão aberta: agendaId={}, sessionId={}, closesAt={}", agendaId, session.getId(), session.getClosesAt());
        return toResponse(session);
    }

    @Transactional
    public VoteResponse castVote(UUID agendaId, CastVoteRequest request) {
        Agenda agenda = requireAgenda(agendaId);
        VotingSession session = sessions.findByAgendaId(agendaId)
                .orElseThrow(() -> new BusinessException("A sessão de votação ainda não foi aberta"));
        Instant now = Instant.now(clock);
        if (!session.isOpenAt(now)) throw new BusinessException("A sessão de votação está encerrada");
        String associateId = request.associateId().trim();
        if (votes.existsByAgendaIdAndAssociateId(agendaId, associateId)) {
            throw new ConflictException("Este associado já votou nesta pauta");
        }
        String cpf = CpfValidator.normalize(request.cpf());
        if (eligibility.check(cpf) != EligibilityStatus.ABLE_TO_VOTE) {
            throw new NotFoundException("Associado não habilitado para votar");
        }
        Vote vote = new Vote(UUID.randomUUID(), agenda, associateId, cpf, request.choice(), now);
        try {
            votes.saveAndFlush(vote);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Este associado já votou nesta pauta");
        }
        log.info("Voto registrado: agendaId={}, associateId={}, choice={}", agendaId, associateId, request.choice());
        return new VoteResponse(vote.getId(), vote.getAssociateId(), vote.getChoice(), vote.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public VotingResultResponse result(UUID agendaId) {
        requireAgenda(agendaId);
        Optional<VotingSession> session = sessions.findByAgendaId(agendaId);
        Map<VoteChoice, Long> totals = new EnumMap<>(VoteChoice.class);
        votes.countByChoice(agendaId).forEach(row -> totals.put(row.getChoice(), row.getTotal()));
        long yes = totals.getOrDefault(VoteChoice.YES, 0L);
        long no = totals.getOrDefault(VoteChoice.NO, 0L);
        String status = session.map(value -> value.isOpenAt(Instant.now(clock)) ? "OPEN" : "CLOSED").orElse("NOT_STARTED");
        String result = !"CLOSED".equals(status) ? "PENDING" : yes == no ? "TIE" : yes > no ? "APPROVED" : "REJECTED";
        return new VotingResultResponse(agendaId, yes, no, yes + no, status, result);
    }

    private Agenda requireAgenda(UUID id) {
        return agendas.findById(id).orElseThrow(() -> new NotFoundException("Pauta não encontrada"));
    }

    private AgendaResponse toResponse(Agenda agenda, VotingSession session) {
        return new AgendaResponse(agenda.getId(), agenda.getTitle(), agenda.getDescription(), agenda.getCreatedAt(),
                session == null ? null : toResponse(session));
    }

    private SessionResponse toResponse(VotingSession session) {
        return new SessionResponse(session.getId(), session.getOpenedAt(), session.getClosesAt(),
                session.isOpenAt(Instant.now(clock)));
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}


package com.somosdb.voting.service;

import com.somosdb.voting.api.dto.*;
import com.somosdb.voting.domain.*;
import com.somosdb.voting.eligibility.*;
import com.somosdb.voting.exception.*;
import com.somosdb.voting.repository.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AgendaServiceTest {
    @Mock AgendaRepository agendas;
    @Mock VotingSessionRepository sessions;
    @Mock VoteRepository votes;
    @Mock VotingEligibilityGateway eligibility;
    private AgendaService service;
    private final Instant now = Instant.parse("2026-09-18T12:00:00Z");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AgendaService(agendas, sessions, votes, eligibility,
                Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void opensSessionForDefaultSixtySeconds() {
        Agenda agenda = agenda();
        when(agendas.findById(agenda.getId())).thenReturn(Optional.of(agenda));
        when(sessions.findByAgendaId(agenda.getId())).thenReturn(Optional.empty());

        SessionResponse response = service.openSession(agenda.getId(), new OpenSessionRequest(null));

        assertThat(response.openedAt()).isEqualTo(now);
        assertThat(response.closesAt()).isEqualTo(now.plusSeconds(60));
        verify(sessions).saveAndFlush(any(VotingSession.class));
    }

    @Test
    void rejectsDuplicateVoteBeforeCallingExternalService() {
        Agenda agenda = agenda();
        VotingSession session = new VotingSession(UUID.randomUUID(), agenda, now.minusSeconds(1), now.plusSeconds(60));
        when(agendas.findById(agenda.getId())).thenReturn(Optional.of(agenda));
        when(sessions.findByAgendaId(agenda.getId())).thenReturn(Optional.of(session));
        when(votes.existsByAgendaIdAndAssociateId(agenda.getId(), "assoc-1")).thenReturn(true);

        assertThatThrownBy(() -> service.castVote(agenda.getId(),
                new CastVoteRequest("assoc-1", "52998224725", VoteChoice.YES)))
                .isInstanceOf(ConflictException.class);
        verifyNoInteractions(eligibility);
    }

    @Test
    void rejectsVoteWhenSessionHasClosed() {
        Agenda agenda = agenda();
        VotingSession session = new VotingSession(UUID.randomUUID(), agenda, now.minusSeconds(61), now.minusSeconds(1));
        when(agendas.findById(agenda.getId())).thenReturn(Optional.of(agenda));
        when(sessions.findByAgendaId(agenda.getId())).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.castVote(agenda.getId(),
                new CastVoteRequest("assoc-1", "52998224725", VoteChoice.YES)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("encerrada");
    }

    @Test
    void countsVotesUsingAggregateProjection() {
        Agenda agenda = agenda();
        when(agendas.findById(agenda.getId())).thenReturn(Optional.of(agenda));
        when(sessions.findByAgendaId(agenda.getId())).thenReturn(Optional.of(
                new VotingSession(UUID.randomUUID(), agenda, now.minusSeconds(120), now.minusSeconds(60))));
        when(votes.countByChoice(agenda.getId())).thenReturn(List.of(count(VoteChoice.YES, 8), count(VoteChoice.NO, 3)));

        VotingResultResponse result = service.result(agenda.getId());

        assertThat(result.yes()).isEqualTo(8);
        assertThat(result.no()).isEqualTo(3);
        assertThat(result.total()).isEqualTo(11);
        assertThat(result.result()).isEqualTo("APPROVED");
    }

    private Agenda agenda() {
        return new Agenda(UUID.randomUUID(), "Pauta", "Descrição", now.minusSeconds(300));
    }

    private VoteCount count(VoteChoice choice, long total) {
        return new VoteCount() {
            public VoteChoice getChoice() { return choice; }
            public long getTotal() { return total; }
        };
    }
}


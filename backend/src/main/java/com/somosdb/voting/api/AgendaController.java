package com.somosdb.voting.api;

import com.somosdb.voting.api.dto.*;
import com.somosdb.voting.service.AgendaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agendas")
public class AgendaController {
    private final AgendaService service;

    public AgendaController(AgendaService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "Cadastrar uma nova pauta")
    ResponseEntity<AgendaResponse> create(@Valid @RequestBody CreateAgendaRequest request) {
        AgendaResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/agendas/" + response.id())).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar pautas")
    List<AgendaResponse> list() { return service.list(); }

    @GetMapping("/{agendaId}")
    @Operation(summary = "Consultar uma pauta")
    AgendaResponse find(@PathVariable UUID agendaId) { return service.find(agendaId); }

    @PostMapping("/{agendaId}/sessions")
    @Operation(summary = "Abrir a sessão de votação de uma pauta")
    ResponseEntity<SessionResponse> open(@PathVariable UUID agendaId,
                                         @Valid @RequestBody(required = false) OpenSessionRequest request) {
        SessionResponse response = service.openSession(agendaId, request == null ? new OpenSessionRequest(null) : request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{agendaId}/votes")
    @Operation(summary = "Registrar o voto de um associado")
    ResponseEntity<VoteResponse> vote(@PathVariable UUID agendaId, @Valid @RequestBody CastVoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.castVote(agendaId, request));
    }

    @GetMapping("/{agendaId}/result")
    @Operation(summary = "Contabilizar os votos e consultar o resultado")
    VotingResultResponse result(@PathVariable UUID agendaId) { return service.result(agendaId); }
}


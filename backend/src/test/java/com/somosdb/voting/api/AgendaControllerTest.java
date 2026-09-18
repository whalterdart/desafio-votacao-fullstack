package com.somosdb.voting.api;

import com.somosdb.voting.eligibility.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgendaControllerTest {
    @Autowired MockMvc mvc;
    @MockBean VotingEligibilityGateway eligibility;

    @Test
    void completesVotingFlow() throws Exception {
        when(eligibility.check(anyString())).thenReturn(EligibilityStatus.ABLE_TO_VOTE);
        String body = mvc.perform(post("/api/v1/agendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Aprovar orçamento\",\"description\":\"Assembleia anual\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Aprovar orçamento"))
                .andReturn().getResponse().getContentAsString();
        String id = com.jayway.jsonpath.JsonPath.read(body, "$.id");

        mvc.perform(post("/api/v1/agendas/{id}/sessions", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"durationSeconds\":60}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.open").value(true));

        mvc.perform(post("/api/v1/agendas/{id}/votes", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"associateId\":\"assoc-1\",\"cpf\":\"52998224725\",\"choice\":\"SIM\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.choice").value("SIM"));

        mvc.perform(get("/api/v1/agendas/{id}/result", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yes").value(1))
                .andExpect(jsonPath("$.total").value(1));
    }
}

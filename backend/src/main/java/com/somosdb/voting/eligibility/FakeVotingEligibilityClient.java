package com.somosdb.voting.eligibility;

import com.somosdb.voting.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class FakeVotingEligibilityClient implements VotingEligibilityGateway {
    @Override
    public EligibilityStatus check(String cpf) {
        if (!CpfValidator.isValid(cpf)) throw new NotFoundException("CPF não encontrado no serviço de elegibilidade");
        return ThreadLocalRandom.current().nextBoolean()
                ? EligibilityStatus.ABLE_TO_VOTE
                : EligibilityStatus.UNABLE_TO_VOTE;
    }
}

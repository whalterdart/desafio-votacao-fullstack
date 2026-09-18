package com.somosdb.voting.eligibility;

public interface VotingEligibilityGateway {
    EligibilityStatus check(String cpf);
}


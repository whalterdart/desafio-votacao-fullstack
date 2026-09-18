package com.somosdb.voting.repository;

import com.somosdb.voting.domain.VoteChoice;

public interface VoteCount {
    VoteChoice getChoice();
    long getTotal();
}


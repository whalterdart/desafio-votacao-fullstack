package com.somosdb.voting.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VoteChoice {
    YES("SIM"), NO("NAO");

    private final String value;
    VoteChoice(String value) { this.value = value; }

    @JsonValue
    public String value() { return value; }

    @JsonCreator
    public static VoteChoice from(String raw) {
        if (raw == null) return null;
        String normalized = raw.trim().toUpperCase().replace("Ã", "A").replace("NÃO", "NAO");
        return switch (normalized) {
            case "SIM", "YES" -> YES;
            case "NAO", "NO" -> NO;
            default -> throw new IllegalArgumentException("O voto deve ser SIM ou NAO");
        };
    }
}


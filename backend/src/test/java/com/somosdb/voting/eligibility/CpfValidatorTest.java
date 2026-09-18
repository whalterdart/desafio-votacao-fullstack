package com.somosdb.voting.eligibility;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CpfValidatorTest {
    @ParameterizedTest
    @ValueSource(strings = {"52998224725", "529.982.247-25"})
    void acceptsValidCpf(String cpf) {
        assertThat(CpfValidator.isValid(cpf)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "11111111111", "52998224724", "123"})
    void rejectsInvalidCpf(String cpf) {
        assertThat(CpfValidator.isValid(cpf)).isFalse();
    }
}


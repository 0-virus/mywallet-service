package com.ragvirus.policy.application.normalization;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ApplicationStatusCalculatorTest {

    private final ApplicationStatusCalculator calculator = new ApplicationStatusCalculator();

    @Test
    void returnsAlwaysOpenWhenPolicyIsAlwaysOpen() {
        ApplicationStatus status = calculator.calculate(null, null, true);

        assertThat(status.status()).isEqualTo("always_open");
        assertThat(status.dday()).isNull();
    }

    @Test
    void returnsClosingSoonWhenDueDateIsWithinSevenDays() {
        ApplicationStatus status = calculator.calculate(null, LocalDate.now().plusDays(3), false);

        assertThat(status.status()).isEqualTo("closing_soon");
        assertThat(status.dday()).isEqualTo(3);
    }

    @Test
    void returnsClosedWhenDueDatePassed() {
        ApplicationStatus status = calculator.calculate(null, LocalDate.now().minusDays(1), false);

        assertThat(status.status()).isEqualTo("closed");
    }
}

package com.example.bff.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CalendarRulesTest {

    @ParameterizedTest
    @CsvSource({
            "2000-09-01, 2026-08-31, 25",
            "2000-09-01, 2026-09-01, 26",
            "2000-09-01, 2026-09-02, 26",
            "2000-02-29, 2025-02-28, 24",
            "2000-02-29, 2025-03-01, 25",
            "2000-02-29, 2026-02-28, 25",
            "2000-02-29, 2028-02-29, 28"
    })
    void calcAgeReturnsExpectedAge(String birthday, String now, int expectedAge) {
        assertEquals(expectedAge,
                CalendarRules.calcAge(LocalDate.parse(birthday), LocalDate.parse(now)));
    }
}

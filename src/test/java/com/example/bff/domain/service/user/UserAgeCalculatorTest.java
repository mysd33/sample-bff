package com.example.bff.domain.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.bff.domain.rule.UserAgeCalculator;
import com.example.fw.common.systemdate.SystemDate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class UserAgeCalculatorTest {

    @Test
    void calculateUsesSystemDateInsteadOfTheActualDate() {
        var systemDate = new FixedSystemDate(LocalDateTime.of(2026, 9, 1, 0, 0));
        var calculator = new UserAgeCalculator(systemDate);

        assertEquals(26, calculator.calculate(LocalDate.of(2000, 9, 1)));
        assertEquals(25, calculator.calculate(LocalDate.of(2000, 9, 2)));
    }

    private record FixedSystemDate(LocalDateTime fixedDate) implements SystemDate {

        @Override
        public LocalDateTime now() {
            return fixedDate;
        }

        @Override
        public ZonedDateTime nowWithZoneInfo() {
            return fixedDate.atZone(ZoneId.systemDefault());
        }
    }
}

package com.example.fw.common.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

class FullWidthKanaTestTest {

    @Test
    void testValidate() {
        String value = "ァアィイゥウェエォオカガキギクグケゲコゴサザシジスズセゼソゾタダチヂッツヅテデトドナニヌネノハバパヒビピフブプヘベペホボポマミムメモャヤュユョヨラリルレロヮワヰヱヲンヴヵヶ　・ー";

        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            assertTrue(validator.validate(new Target(value)).isEmpty());
        }
    }

    private static class Target {

        @FullWidthKana
        private final String value;

        private Target(String value) {
            this.value = value;
        }
    }
}

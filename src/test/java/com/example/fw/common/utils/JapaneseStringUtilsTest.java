package com.example.fw.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class JapaneseStringUtilsTest {

    @ParameterizedTest //
    @CsvSource({ //
            "―,—", //
            "＼,\\", //
            "～,〜", //
            "∥,‖", //
            "－,−", //
            "￠,¢", //
            "￡,£", //
            "￢,¬", //
            "―＼～∥－￠￡￢,—\\〜‖−¢£¬" })
    void testExchageSpecialChar(String input, String expected) {
        assertEquals(expected, JapaneseStringUtils.convertCodePoints(input));
    }
}

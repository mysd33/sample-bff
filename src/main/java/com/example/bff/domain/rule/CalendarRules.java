package com.example.bff.domain.rule;

import java.time.LocalDate;
import java.time.Period;

/// 日付計算にかかわるビジネスルールクラス
public class CalendarRules {

    private CalendarRules() {
    }

    /// 年齢を計算する
    ///
    /// @param birthday 誕生日
    /// @param now      現在日付
    /// @return 年齢
    public static int calcAge(LocalDate birthday, LocalDate now) {
        return Period.between(birthday, now).getYears();
    }
}

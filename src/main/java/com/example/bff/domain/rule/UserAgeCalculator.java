package com.example.bff.domain.rule;

import com.example.fw.common.systemdate.SystemDate;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/// ユーザの年齢を計算するコンポーネント
@Component
@RequiredArgsConstructor
public class UserAgeCalculator {

    // システム日時取得用のインタフェース
    private final SystemDate systemDate;

    /// システム日付を基準に年齢を計算する
    ///
    /// @param birthday 誕生日
    /// @return 年齢
    public int calculate(LocalDate birthday) {
        return CalendarRules.calcAge(birthday, systemDate.now().toLocalDate());
    }
}

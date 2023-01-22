package com.example.bff.domain.rule;

import java.util.Calendar;
import java.util.Date;

/**
 * 日付計算にかかわるビジネスルールクラス
 *
 */
public class CalendarRules {
    private CalendarRules() {
    }

    /**
     * 年齢を計算する
     * 
     * @param birthday 誕生日
     * @return 年齢
     */
    public static int calcAge(Date birthday) {
        return calcAge(birthday, new Date());
    }

    /**
     * 年齢を計算する
     * 
     * @param birthday 誕生日
     * @param now      現在日付
     * @return 年齢
     */
    public static int calcAge(Date birthday, Date now) {
        Calendar calendarBirth = Calendar.getInstance();
        Calendar calendarNow = Calendar.getInstance();
        calendarBirth.setTime(birthday);
        calendarNow.setTime(now);

        // （現在年 - 生まれ年）で年齢の計算
        int age = calendarNow.get(Calendar.YEAR) - calendarBirth.get(Calendar.YEAR);

        // 誕生月を迎えていなければ年齢-1
        if (calendarNow.get(Calendar.MONTH) < calendarBirth.get(Calendar.MONTH)
                // 誕生月は迎えているが、誕生日を迎えていなければ年齢−1
                || calendarNow.get(Calendar.MONTH) == calendarBirth.get(Calendar.MONTH)
                        && calendarNow.get(Calendar.DATE) < calendarBirth.get(Calendar.DATE)) {
            age -= 1;
        }
        return age;
    }
}

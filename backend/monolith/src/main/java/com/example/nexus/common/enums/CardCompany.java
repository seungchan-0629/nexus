package com.example.nexus.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum CardCompany {
    BC("31", "비씨카드"),
    KOOKMIN("11", "국민카드"),
    SAMSUNG("51", "삼성카드"),
    HYUNDAI("61", "현대카드"),
    SHINHAN("41", "신한카드"),
    HANA("21", "하나카드"),
    LOTTE("71", "롯데카드"),
    NH("91", "농협카드"),
    CITY("36", "씨티카드"),
    WOORI("33", "우리카드"),
    WOORI_CULTURE("W1", "우리컬쳐"),
    UNKNOWN("", "기타카드");

    private final String code;
    private final String korName;

    public static String getKorNameByCode(String code) {
        return Arrays.stream(CardCompany.values())
                .filter(c -> c.code.equals(code))
                .findFirst()
                .map(CardCompany::getKorName)
                .orElse(UNKNOWN.korName + (code != null && !code.isEmpty() ? "(" + code + ")" : ""));
    }
}

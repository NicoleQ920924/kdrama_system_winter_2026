package com.kdrama.backend.enums;

public enum KRReleaseSchedule implements HasDisplayName {
    MON_TUE("月火"),
    WED_THU("水木"),
    FRI_SAT("金土"),
    SAT_SUN("土日"),
    FULL_RELEASE("公開日全集上架"),
    UNKNOWN("尚未公布播出日程"),
    OTHERS("其他");

    private final String displayName;

    KRReleaseSchedule(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}


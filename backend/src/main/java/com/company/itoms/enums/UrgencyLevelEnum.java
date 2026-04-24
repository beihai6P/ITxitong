package com.company.itoms.enums;

public enum UrgencyLevelEnum {
    NORMAL(1, "普通"),
    URGENCY(2, "紧急"),
    EXTREME_URGENCY(3, "特急");

    private final int code;
    private final String desc;

    UrgencyLevelEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int code() { return code; }
    public String desc() { return desc; }
}

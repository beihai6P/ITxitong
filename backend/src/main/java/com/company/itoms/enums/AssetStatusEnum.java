package com.company.itoms.enums;

public enum AssetStatusEnum {
    IN_USE("IN_USE", "在用", "资产正在使用中"),
    IDLE("IDLE", "闲置", "资产闲置未使用"),
    REPAIR("REPAIR", "维修中", "资产正在维修"),
    SCRAP("SCRAP", "报废", "资产已报废"),
    PENDING_RETURN("PENDING_RETURN", "待归还", "资产待归还");

    private final String code;
    private final String name;
    private final String description;

    AssetStatusEnum(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String code() {
        return code;
    }

    public String getDisplayName() {
        return name;
    }

    public String description() {
        return description;
    }

    public static AssetStatusEnum fromCode(String code) {
        for (AssetStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid asset status code: " + code);
    }
}
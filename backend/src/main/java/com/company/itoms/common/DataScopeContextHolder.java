package com.company.itoms.common;

public class DataScopeContextHolder {
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void setSqlFilter(String sqlFilter) {
        CONTEXT.set(sqlFilter);
    }

    public static String getSqlFilter() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}

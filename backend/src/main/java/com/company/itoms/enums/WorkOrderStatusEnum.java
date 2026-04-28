package com.company.itoms.enums;

public enum WorkOrderStatusEnum {
    PENDING(1, "待接单", "等待工程师接单"),
    DISPATCHED(2, "已派单", "已分配给工程师"),
    PROCESSING(3, "处理中", "工程师正在处理"),
    PENDING_ACCEPTANCE(4, "待验收", "等待客户验收"),
    COMPLETED(5, "已完成", "工单已完成"),
    REJECTED(6, "已驳回", "工单被驳回"),
    SUSPENDED(7, "已暂停", "工单暂停处理"),
    CLOSED(8, "已关闭", "工单已关闭"),
    PENDING_PARTS(9, "缺件挂起", "等待备件耗材");

    private final int code;
    private final String name;
    private final String description;

    WorkOrderStatusEnum(int code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public int code() {
        return code;
    }

    public String getDisplayName() {
        return name;
    }

    public String description() {
        return description;
    }

    public static WorkOrderStatusEnum fromCode(int code) {
        for (WorkOrderStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid work order status code: " + code);
    }

    public boolean canTransitionTo(WorkOrderStatusEnum target) {
        switch (this) {
            case PENDING:
                return target == DISPATCHED || target == CLOSED;
            case DISPATCHED:
                return target == PROCESSING || target == PENDING || target == CLOSED;
            case PROCESSING:
                return target == PENDING_ACCEPTANCE || target == SUSPENDED || target == REJECTED || target == PENDING_PARTS;
            case PENDING_ACCEPTANCE:
                return target == COMPLETED || target == REJECTED;
            case COMPLETED:
                return target == CLOSED;
            case REJECTED:
                return target == PENDING;
            case SUSPENDED:
                return target == PROCESSING || target == CLOSED;
            case PENDING_PARTS:
                return target == PROCESSING || target == CLOSED;
            case CLOSED:
                return false;
            default:
                return false;
        }
    }
}
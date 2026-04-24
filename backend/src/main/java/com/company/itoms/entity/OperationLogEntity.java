package com.company.itoms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String module;

    private String type;

    private String description;

    private String requestMethod;

    private String requestUrl;

    private String requestIp;

    private String requestParams;

    private String responseData;

    private Long operatorId;

    private String operatorName;

    /**
     * 0-成功, 1-失败
     */
    private Integer status;

    private String errorMsg;

    private Long executeTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

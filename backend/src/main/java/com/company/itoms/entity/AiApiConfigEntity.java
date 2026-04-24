package com.company.itoms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_api_config")
public class AiApiConfigEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String providerName;

    private String apiKey;

    private String secretKey;

    private String apiUrl;

    private String modelName;

    private Integer isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
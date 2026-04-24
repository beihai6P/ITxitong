package com.company.itoms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_recommendation_log")
public class AiRecommendationLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workOrderId;

    private String descriptionInput;

    private Long recommendedSolutionId;

    private BigDecimal confidenceScore;

    private Integer userFeedback;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
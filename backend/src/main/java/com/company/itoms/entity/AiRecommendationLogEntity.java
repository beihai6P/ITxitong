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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkOrderId() {
        return this.workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getDescriptionInput() {
        return this.descriptionInput;
    }

    public void setDescriptionInput(String descriptionInput) {
        this.descriptionInput = descriptionInput;
    }

    public Long getRecommendedSolutionId() {
        return this.recommendedSolutionId;
    }

    public void setRecommendedSolutionId(Long recommendedSolutionId) {
        this.recommendedSolutionId = recommendedSolutionId;
    }

    public BigDecimal getConfidenceScore() {
        return this.confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public Integer getUserFeedback() {
        return this.userFeedback;
    }

    public void setUserFeedback(Integer userFeedback) {
        this.userFeedback = userFeedback;
    }

    public LocalDateTime getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

}
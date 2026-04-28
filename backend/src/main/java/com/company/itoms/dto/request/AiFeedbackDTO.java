package com.company.itoms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiFeedbackDTO {
    @NotNull(message = "Log ID cannot be null")
    private Long logId;
    
    @NotNull(message = "Feedback cannot be null")
    private Integer userFeedback; // 1-采纳, 2-未采纳

    public Long getLogId() {
        return this.logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Integer getUserFeedback() {
        return this.userFeedback;
    }

    public void setUserFeedback(Integer userFeedback) {
        this.userFeedback = userFeedback;
    }

}

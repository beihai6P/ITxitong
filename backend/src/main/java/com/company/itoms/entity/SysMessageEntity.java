package com.company.itoms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_message")
public class SysMessageEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long receiverId;
    private String title;
    private String content;
    private String type;
    private Integer isRead;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

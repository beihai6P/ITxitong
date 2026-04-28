package com.company.itoms.controller;

import com.company.itoms.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Tag(name = "文件上传")
@RestController
@RequestMapping("/file")
@Slf4j
public class FileUploadController {

    // 默认上传路径，可配置到application.yml中
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }

        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String newFilename = UUID.randomUUID().toString() + extension;
            File dest = new File(dir, newFilename);
            file.transferTo(dest);

            // 返回文件访问路径（示例，实际应根据服务器配置返回URL）
            String fileUrl = "/uploads/" + newFilename;
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error(500, "文件上传失败");
        }
    }

}

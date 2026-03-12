package com.concert.controller;

import com.concert.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.paramError("请选择文件");
        }

        try {
            // 获取项目根目录下的 uploads 文件夹
            String projectRoot = System.getProperty("user.dir");
            String uploadPath = projectRoot + File.separator + uploadDir;
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

            // 保存文件
            File dest = new File(uploadPath + File.separator + fileName);
            file.transferTo(dest);
            log.info("文件上传成功：{} -> {}", originalName, dest.getAbsolutePath());

            // 返回可访问的URL路径
            String url = "/api/uploads/" + fileName;
            return Result.success(url);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
}

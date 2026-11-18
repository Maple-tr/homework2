package com.library.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @PostMapping("/upload")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("code", 400);
            result.put("msg", "文件不能为空");
            return result;
        }

        try {
            // 1. 定义存储目录 (项目根目录下的 uploads 文件夹)
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs(); // 目录不存在则创建
            }

            // 2. 生成新文件名 (防止重名覆盖)
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString().replace("-", "") + suffix;

            // 3. 保存文件到磁盘
            File destFile = new File(uploadDir + newFileName);
            file.transferTo(destFile);

            // 4. 返回可通过浏览器访问的相对路径
            result.put("code", 200);
            result.put("msg", "上传成功");
            result.put("url", "/images/" + newFileName);

        } catch (IOException e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "上传失败");
        }
        return result;
    }
}
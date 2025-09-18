package org.zszq.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zszq.service.MinioService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * MinIO文件管理控制器
 * 提供文件上传、下载、预览、目录管理等功能
 * 基于MinIO对象存储实现分布式文件管理
 * 
 * @author GeWhale Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/minio")
@Tag(name = "MinIO文件管理", description = "基于MinIO的文件上传、下载、预览等相关接口")
public class MinioController {

    @Autowired
    private MinioService minioService;

    /**
     * 上传单个文件
     * 将文件上传到指定的MinIO存储桶中
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称（文件在存储桶中的路径）
     * @param file 要上传的文件
     * @return 上传结果信息
     */
    @Operation(
        summary = "上传单个文件",
        description = "将文件上传到指定的MinIO存储桶中，支持各种文件类型"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "上传成功",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(type = "string", example = "上传成功")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "上传失败",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(type = "string", example = "上传失败: 文件处理错误")
            )
        )
    })
    @PostMapping("/upload")
    public String uploadFile(
            @Parameter(description = "存储桶名称", required = true, example = "my-bucket")
            @RequestParam("bucketName") String bucketName,
            @Parameter(description = "对象名称（文件路径）", required = true, example = "documents/file.pdf")
            @RequestParam("objectName") String objectName,
            @Parameter(description = "要上传的文件", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            // 创建临时文件
            Path tempFile = Files.createTempFile("upload-", file.getOriginalFilename());
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            boolean result = minioService.uploadFile(bucketName, objectName, tempFile.toString());

            // 删除临时文件
            Files.delete(tempFile);

            return result ? "上传成功" : "上传失败";
        } catch (IOException e) {
            return "上传失败: " + e.getMessage();
        }
    }

    /**
     * 上传整个目录
     * 将本地目录中的所有文件批量上传到MinIO
     * 
     * @param bucketName 存储桶名称
     * @param localDir 本地目录路径
     * @param minioDir MinIO中的目标目录（可选）
     * @return 上传结果统计
     */
    @Operation(
        summary = "上传目录",
        description = "将本地目录及其下所有文件递归上传到MinIO存储桶"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "上传完成",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(type = "string", example = "目录上传完成，成功上传 5 个文件")
            )
        )
    })
    @PostMapping("/upload-directory")
    public String uploadDirectory(
            @Parameter(description = "存储桶名称", required = true, example = "my-bucket")
            @RequestParam("bucketName") String bucketName,
            @Parameter(description = "本地目录路径", required = true, example = "/path/to/local/directory")
            @RequestParam("localDir") String localDir,
            @Parameter(description = "MinIO中的目标目录", example = "uploads/")
            @RequestParam(value = "minioDir", required = false, defaultValue = "") String minioDir) {
        int count = minioService.uploadDirectory(bucketName, localDir, minioDir);
        return "目录上传完成，成功上传 " + count + " 个文件";
    }

    /**
     * 生成文件预览链接
     * 生成一个有时效的文件预览或下载链接
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称（文件路径）
     * @param expiresHours 链接有效期（小时）
     * @return 预览链接URL
     */
    @Operation(
        summary = "生成文件预览链接",
        description = "为指定文件生成一个有时效的预览或下载链接"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "生成成功",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(type = "string", example = "http://localhost:9000/my-bucket/file.pdf?X-Amz-Algorithm=...")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "生成失败",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(type = "string", example = "生成预览链接失败")
            )
        )
    })
    @GetMapping("/preview-url")
    public String generatePreviewUrl(
            @Parameter(description = "存储桶名称", required = true, example = "my-bucket")
            @RequestParam("bucketName") String bucketName,
            @Parameter(description = "对象名称（文件路径）", required = true, example = "documents/file.pdf")
            @RequestParam("objectName") String objectName,
            @Parameter(description = "链接有效期（小时）", example = "24")
            @RequestParam(value = "expiresHours", required = false, defaultValue = "24") int expiresHours) {
        String url = minioService.generatePreviewUrl(bucketName, objectName, expiresHours);
        return url != null ? url : "生成预览链接失败";
    }

    /**
     * 下载文件
     * 从 MinIO 下载文件到本地指定路径
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称（文件路径）
     * @param filePath 本地保存路径
     * @return 下载结果
     */
    @Operation(
        summary = "下载文件",
        description = "从 MinIO 存储桶中下载文件到本地指定路径"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "下载成功",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(type = "string", example = "下载成功")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "下载失败",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(type = "string", example = "下载失败")
            )
        )
    })
    @GetMapping("/download")
    public String downloadFile(
            @Parameter(description = "存储桶名称", required = true, example = "my-bucket")
            @RequestParam("bucketName") String bucketName,
            @Parameter(description = "对象名称（文件路径）", required = true, example = "documents/file.pdf")
            @RequestParam("objectName") String objectName,
            @Parameter(description = "本地保存路径", required = true, example = "/path/to/save/file.pdf")
            @RequestParam("filePath") String filePath) {
        boolean result = minioService.downloadFile(bucketName, objectName, filePath);
        return result ? "下载成功" : "下载失败";
    }
}

package org.zszq.service;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.music:music-bucket}")
    private String musicBucket;

    @Value("${minio.bucket.images:images-bucket}")
    private String imagesBucket;

    public String uploadMusicFile(MultipartFile file, String fileName) {
        try {
            // 检查文件类型
            String contentType = file.getContentType();
            if (!isAudioFile(contentType)) {
                throw new IllegalArgumentException("不支持的音频文件格式");
            }

            // 确保存储桶存在
            ensureBucketExists(musicBucket);

            // 上传文件
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(musicBucket)
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(contentType)
                    .build());

            return generateFileUrl(musicBucket, fileName);

        } catch (Exception e) {
            throw new RuntimeException("音乐文件上传失败: " + e.getMessage(), e);
        }
    }

    public String uploadCoverImage(MultipartFile file, String fileName) {
        try {
            // 检查文件类型
            String contentType = file.getContentType();
            if (!isImageFile(contentType)) {
                throw new IllegalArgumentException("不支持的图片文件格式");
            }

            // 确保存储桶存在
            ensureBucketExists(imagesBucket);

            // 上传文件
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(imagesBucket)
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(contentType)
                    .build());

            return generateFileUrl(imagesBucket, fileName);

        } catch (Exception e) {
            throw new RuntimeException("封面图片上传失败: " + e.getMessage(), e);
        }
    }

    private void ensureBucketExists(String bucketName) {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("创建存储桶失败: " + e.getMessage(), e);
        }
    }

    private String generateFileUrl(String bucketName, String fileName) {
        // 这里可以根据实际需求返回CDN地址或直接访问地址
        return String.format("http://localhost:9000/%s/%s", bucketName, fileName);
    }

    private boolean isAudioFile(String contentType) {
        return contentType != null && (
                contentType.startsWith("audio/") ||
                contentType.equals("application/ogg")
        );
    }

    private boolean isImageFile(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }
}
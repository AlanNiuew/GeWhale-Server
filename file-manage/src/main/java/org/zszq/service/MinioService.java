package org.zszq.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    /**
     * 上传单个文件到Minio
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称（在Minio中的路径+文件名）
     * @param filePath   本地文件路径
     * @return true表示上传成功，false表示失败
     */
    public boolean uploadFile(String bucketName, String objectName, String filePath) {
        try {
            // 检查存储桶是否存在，不存在则创建
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("存储桶 " + bucketName + " 创建成功");
            }

            // 上传文件
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .filename(filePath)
                    .build());
            System.out.println("文件 " + filePath + " 上传成功，目标路径: " + objectName);
            return true;

        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            System.out.println("文件上传错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 上传整个目录到Minio
     *
     * @param bucketName 存储桶名称
     * @param localDir   本地目录路径
     * @param minioDir   在Minio中的目标目录（可选）
     * @return 上传成功的文件数量
     */
    public int uploadDirectory(String bucketName, String localDir, String minioDir) {
        AtomicInteger successCount = new AtomicInteger();

        try {
            // 检查存储桶是否存在，不存在则创建
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("存储桶 " + bucketName + " 创建成功");
            }

            // 遍历本地目录
            Files.walk(Paths.get(localDir))
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            // 计算相对路径
                            String relativePath = Paths.get(localDir).relativize(file).toString();
                            // 构建Minio中的对象名称
                            String objectName = minioDir != null && !minioDir.isEmpty() ?
                                    Paths.get(minioDir, relativePath).toString().replace("\\", "/") :
                                    relativePath.replace("\\", "/");

                            // 上传文件
                            minioClient.uploadObject(UploadObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(objectName)
                                    .filename(file.toString())
                                    .build());
                            System.out.println("文件 " + file + " 上传成功，目标路径: " + objectName);
                            successCount.getAndIncrement();
                        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
                            System.out.println("文件 " + file + " 上传失败: " + e.getMessage());
                        }
                    });

            System.out.println("目录上传完成，成功上传 " + successCount + " 个文件");
            return successCount.get();

        } catch (IOException e) {
            System.out.println("目录上传错误: " + e.getMessage());
            return successCount.get();
        } catch (ServerException | ErrorResponseException | InsufficientDataException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成文件预览链接
     *
     * @param bucketName   存储桶名称
     * @param objectName   对象名称
     * @param expiresHours 链接有效期（小时），默认24小时
     * @return 预览链接URL
     */
    public String generatePreviewUrl(String bucketName, String objectName, int expiresHours) {
        try {
            // 生成预签名URL
            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expiresHours, TimeUnit.HOURS)
                    .method(Method.GET)
                    .build());
            return url;
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            System.out.println("生成预览链接失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从Minio下载文件到本地
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称（在Minio中的路径+文件名）
     * @param filePath   本地文件保存路径
     * @return true表示下载成功，false表示失败
     */
    public boolean downloadFile(String bucketName, String objectName, String filePath) {
        try {
            // 确保本地文件的目录存在
            Path localDir = Paths.get(filePath).getParent();
            if (localDir != null && !Files.exists(localDir)) {
                Files.createDirectories(localDir);
            }

            // 下载文件
            minioClient.downloadObject(DownloadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .filename(filePath)
                    .build());
            System.out.println("文件 " + objectName + " 下载成功，保存路径: " + new File(filePath).getAbsolutePath());
            return true;

        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            System.out.println("文件下载错误: " + e.getMessage());
            return false;
        }
    }
}

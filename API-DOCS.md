# GeWhale-Server API 文档访问指南

本项目已集成Swagger/OpenAPI 3.0文档，各个模块可通过以下地址访问API文档：

## 服务端口配置

- **user-service**: 8081
- **music-service**: 8082
- **file-manage**: 8088

## API文档访问地址

启动相应服务后，可通过以下地址访问Swagger UI：

### 用户服务 (User Service)
- **Swagger UI**: http://localhost:8081/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8081/v3/api-docs

### 音乐服务 (Music Service)
- **Swagger UI**: http://localhost:8082/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8082/v3/api-docs

### 文件管理服务 (File Management Service)
- **Swagger UI**: http://localhost:8088/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8088/v3/api-docs

## 功能特性

### 完整的API文档
- 所有接口都有详细的中文注释
- 包含请求参数说明和示例
- 提供响应状态码和示例
- 支持在线测试API

### 安全认证
- 用户服务和音乐服务支持JWT Bearer Token认证
- 文件管理服务支持API Key认证
- 可在Swagger UI中配置认证信息

### 标准化响应
- 统一的错误响应格式
- 清晰的状态码定义
- 详细的错误信息描述

## 启动服务

每个服务可以独立启动：

```bash
# 启动用户服务
cd user-service
mvn spring-boot:run

# 启动音乐服务
cd music-service
mvn spring-boot:run

# 启动文件管理服务
cd file-manage
mvn spring-boot:run
```

## 技术栈

- SpringDoc OpenAPI 3.0
- Spring Boot 3.5.5
- Java 21
- Swagger UI 5.2.0
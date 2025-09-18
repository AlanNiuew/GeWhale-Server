# GeWhale-Server
鲸鱼的歌声空灵、神秘且传播极远。

## 项目简介
GeWhale-Server 是一个基于Spring Boot微服务架构的音乐平台后端服务，支持音乐上传、用户管理、文件存储等功能。

## 技术栈
- Java 21
- Spring Boot 3.5.5
- Spring Security
- Spring Data JPA
- PostgreSQL
- MinIO 对象存储
- Maven 多模块

## 模块结构

### 1. common
通用模块，包含共享的工具类和配置

### 2. file-manage (端口: 8088)
文件管理服务，负责文件上传、下载和MinIO存储管理
- MinIO客户端配置
- 文件上传下载API
- 文件元数据管理

### 3. user-service (端口: 8081)
用户服务，负责用户注册、登录、认证和权限管理
- 用户注册/登录
- JWT认证
- 用户信息管理
- 权限控制（USER/ARTIST/ADMIN）

### 4. music-service (端口: 8082)
音乐服务，负责音乐上传、管理、播放统计和播放列表管理
- 音乐文件上传
- 音乐元数据管理
- 音乐搜索功能
- 播放统计
- 播放列表创建、编辑、删除
- 播放列表音乐管理
- 系统播放列表（收藏、最近播放）
- 播放列表搜索和排行

## 环境要求
- JDK 21
- Maven 3.6+
- PostgreSQL 数据库
- MinIO 服务器
- Redis（可选）

## 快速开始

### 1. 数据库配置
创建PostgreSQL数据库：
```sql
CREATE DATABASE "ge-whale";
```

### 2. MinIO配置
启动MinIO服务器（默认端口9000）

### 3. 编译项目
```bash
mvn clean compile
```

### 4. 启动服务
分别启动各个模块：

```bash
# 文件管理服务
cd file-manage
mvn spring-boot:run

# 用户服务
cd user-service
mvn spring-boot:run

# 音乐服务
cd music-service
mvn spring-boot:run
```

## API接口

### 用户服务 (8081)
- POST `/api/users/register` - 用户注册
- GET `/api/users/profile` - 获取用户信息
- PUT `/api/users/profile` - 更新用户信息

### 音乐服务 (8082)
- POST `/api/music/upload` - 上传音乐
- GET `/api/music/{id}` - 获取音乐信息
- GET `/api/music/search` - 搜索音乐
- POST `/api/music/{id}/play` - 记录播放

### 播放列表服务 (8082)
- POST `/api/playlists` - 创建播放列表
- GET `/api/playlists/{id}` - 获取播放列表信息
- GET `/api/playlists/{id}/musics` - 获取播放列表音乐
- GET `/api/playlists/user/{userId}` - 获取用户播放列表
- GET `/api/playlists/search` - 搜索播放列表
- PUT `/api/playlists/{id}` - 更新播放列表
- DELETE `/api/playlists/{id}` - 删除播放列表
- POST `/api/playlists/{id}/musics` - 添加音乐到播放列表
- DELETE `/api/playlists/{id}/musics/{musicId}` - 从播放列表移除音乐
- POST `/api/playlists/{id}/play` - 记录播放列表播放
- POST `/api/playlists/{id}/like` - 点赞/取消点赞播放列表
- GET `/api/playlists/top` - 获取热门播放列表
- GET `/api/playlists/latest` - 获取最新播放列表
- GET `/api/playlists/user/{userId}/favorites` - 获取用户收藏列表
- GET `/api/playlists/user/{userId}/recent` - 获取用户最近播放列表

### 文件管理服务 (8088)
- POST `/minio/upload` - 上传文件
- GET `/minio/preview-url` - 生成预览链接
- GET `/minio/download` - 下载文件

## 配置说明

各服务的配置文件位于对应模块的 `src/main/resources/application-dev.yaml`

主要配置项：
- 数据库连接信息
- MinIO服务器配置
- 服务端口配置

## 开发规范

- 使用Lombok简化代码
- 遵循RESTful API设计
- 使用JPA进行数据持久化
- 统一异常处理
- 日志记录

# 播放列表功能测试指南

## 功能概述

播放列表功能已经成功集成到music-service模块中，提供了完整的播放列表管理能力。

## 核心功能

### 1. 播放列表类型
- **USER_CREATED**: 用户创建的播放列表
- **SYSTEM**: 系统播放列表
- **FAVORITE**: 收藏列表（系统自动创建）
- **RECENTLY_PLAYED**: 最近播放列表（系统自动创建）

### 2. 可见性设置
- **PUBLIC**: 公开播放列表
- **PRIVATE**: 私有播放列表
- **FRIENDS**: 仅好友可见

### 3. 支持的操作
- 创建、编辑、删除播放列表
- 添加、移除播放列表中的音乐
- 调整音乐在播放列表中的顺序
- 播放列表搜索和排行
- 播放统计和点赞功能

## API测试示例

### 1. 创建播放列表
```bash
POST http://localhost:8082/api/playlists?creatorId=1
Content-Type: application/json

{
    "name": "我的最爱",
    "description": "我最喜欢的音乐集合",
    "visibility": "PUBLIC"
}
```

### 2. 获取播放列表及音乐
```bash
GET http://localhost:8082/api/playlists/1/musics?page=0&size=10
```

### 3. 添加音乐到播放列表
```bash
POST http://localhost:8082/api/playlists/1/musics?userId=1
Content-Type: application/json

{
    "musicId": 1
}
```

### 4. 搜索播放列表
```bash
GET http://localhost:8082/api/playlists/search?keyword=流行&page=0&size=10
```

### 5. 获取用户的收藏列表
```bash
GET http://localhost:8082/api/playlists/user/1/favorites
```

### 6. 获取热门播放列表
```bash
GET http://localhost:8082/api/playlists/top?limit=10
```

## 数据库表结构

### playlists 表
- id: 播放列表ID
- name: 播放列表名称
- description: 描述
- cover_url: 封面图片URL
- creator_id: 创建者ID
- type: 播放列表类型
- visibility: 可见性
- play_count: 播放次数
- like_count: 点赞数
- music_count: 音乐数量
- created_at, updated_at: 时间戳

### playlist_music 表
- id: 记录ID
- playlist_id: 播放列表ID
- music_id: 音乐ID
- sort_order: 排序顺序
- added_by_id: 添加者ID
- added_at: 添加时间

## 测试前准备

1. **启动数据库**
   ```bash
   # 确保PostgreSQL运行并创建ge-whale数据库
   createdb ge-whale
   ```

2. **执行初始化脚本**
   ```bash
   psql -d ge-whale -f database/init.sql
   ```

3. **启动music-service**
   ```bash
   cd music-service
   mvn spring-boot:run
   ```

## 验证功能

1. **基础功能验证**
   - 创建播放列表
   - 添加音乐到播放列表
   - 查看播放列表内容
   - 删除播放列表中的音乐

2. **高级功能验证**
   - 播放列表搜索
   - 系统播放列表自动创建
   - 播放统计功能
   - 排序功能

3. **权限验证**
   - 只有创建者能编辑/删除播放列表
   - 公开/私有播放列表访问权限
   - 播放列表可见性控制

## 注意事项

1. 播放列表中的音乐按sort_order字段排序
2. 系统会自动为每个用户创建收藏列表和最近播放列表
3. 删除播放列表会级联删除所有关联的音乐记录
4. 音乐数量会自动更新
5. 支持分页查询提高性能

## 错误处理

常见错误情况：
- 播放列表不存在 (404)
- 无权限操作 (403)
- 音乐已存在于播放列表中
- 音乐不存在
- 参数验证失败

所有错误都会返回友好的中文错误消息。
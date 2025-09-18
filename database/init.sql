-- 鲸歌音乐平台数据库初始化脚本
-- 在PostgreSQL中执行

-- 创建数据库（如果不存在）
-- CREATE DATABASE "ge-whale" WITH ENCODING 'UTF8';

-- 使用数据库
-- \c ge-whale;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(20) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    nickname VARCHAR(20),
    avatar_url VARCHAR(500),
    phone VARCHAR(11),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 文件元数据表
CREATE TABLE IF NOT EXISTS file_metadata (
    id BIGSERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100),
    file_extension VARCHAR(20),
    bucket_name VARCHAR(100) NOT NULL DEFAULT 'default-bucket',
    object_name VARCHAR(500) NOT NULL,
    storage_provider VARCHAR(50) DEFAULT 'minio',
    parent_id INTEGER,
    is_directory BOOLEAN DEFAULT FALSE,
    directory_path VARCHAR(1000) DEFAULT '/',
    version INTEGER DEFAULT 1,
    is_deleted BOOLEAN DEFAULT FALSE,
    deletion_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed TIMESTAMP
);

-- 音乐表
CREATE TABLE IF NOT EXISTS music (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    artist VARCHAR(50),
    album VARCHAR(50),
    duration_seconds INTEGER,
    file_url VARCHAR(500) NOT NULL,
    cover_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    upload_user_id BIGINT NOT NULL,
    play_count BIGINT DEFAULT 0,
    like_count BIGINT DEFAULT 0,
    file_size BIGINT,
    bit_rate INTEGER,
    sample_rate INTEGER,
    genre VARCHAR(20),
    release_year INTEGER,
    lyrics TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 播放列表表
CREATE TABLE IF NOT EXISTS playlists (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    cover_url VARCHAR(500),
    creator_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'USER_CREATED',
    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    play_count BIGINT DEFAULT 0,
    like_count BIGINT DEFAULT 0,
    music_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 播放列表音乐关联表
CREATE TABLE IF NOT EXISTS playlist_music (
    id BIGSERIAL PRIMARY KEY,
    playlist_id BIGINT NOT NULL,
    music_id BIGINT NOT NULL,
    sort_order INTEGER NOT NULL,
    added_by_id BIGINT NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
    FOREIGN KEY (music_id) REFERENCES music(id) ON DELETE CASCADE,
    UNIQUE(playlist_id, music_id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

CREATE INDEX IF NOT EXISTS idx_music_title ON music(title);
CREATE INDEX IF NOT EXISTS idx_music_artist ON music(artist);
CREATE INDEX IF NOT EXISTS idx_music_status ON music(status);
CREATE INDEX IF NOT EXISTS idx_music_upload_user_id ON music(upload_user_id);
CREATE INDEX IF NOT EXISTS idx_music_genre ON music(genre);
CREATE INDEX IF NOT EXISTS idx_music_created_at ON music(created_at);
CREATE INDEX IF NOT EXISTS idx_music_play_count ON music(play_count);

CREATE INDEX IF NOT EXISTS idx_playlists_creator_id ON playlists(creator_id);
CREATE INDEX IF NOT EXISTS idx_playlists_type ON playlists(type);
CREATE INDEX IF NOT EXISTS idx_playlists_visibility ON playlists(visibility);
CREATE INDEX IF NOT EXISTS idx_playlists_created_at ON playlists(created_at);
CREATE INDEX IF NOT EXISTS idx_playlists_play_count ON playlists(play_count);

CREATE INDEX IF NOT EXISTS idx_playlist_music_playlist_id ON playlist_music(playlist_id);
CREATE INDEX IF NOT EXISTS idx_playlist_music_music_id ON playlist_music(music_id);
CREATE INDEX IF NOT EXISTS idx_playlist_music_sort_order ON playlist_music(playlist_id, sort_order);

-- 插入示例数据（可选）
-- 插入管理员用户
INSERT INTO users (username, password, email, nickname, role) 
VALUES ('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'admin@gewhale.com', '管理员', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

-- 插入测试用户
INSERT INTO users (username, password, email, nickname, role) 
VALUES ('testuser', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'test@gewhale.com', '测试用户', 'USER')
ON CONFLICT (username) DO NOTHING;

-- 插入艺术家用户
INSERT INTO users (username, password, email, nickname, role) 
VALUES ('artist1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'artist@gewhale.com', '音乐家', 'ARTIST')
ON CONFLICT (username) DO NOTHING;

COMMENT ON TABLE users IS '用户表';
COMMENT ON TABLE file_metadata IS '文件元数据表';
COMMENT ON TABLE music IS '音乐表';
COMMENT ON TABLE playlists IS '播放列表表';
COMMENT ON TABLE playlist_music IS '播放列表音乐关联表';
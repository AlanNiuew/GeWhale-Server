package org.zszq.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zszq.entity.Music;

import java.util.List;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {

    Page<Music> findByStatus(Music.MusicStatus status, Pageable pageable);

    Page<Music> findByUploadUserId(Long uploadUserId, Pageable pageable);

    @Query("SELECT m FROM Music m WHERE m.status = 'APPROVED' AND " +
           "(LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.artist) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.album) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Music> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT m FROM Music m WHERE m.status = 'APPROVED' AND " +
           "(:artist IS NULL OR LOWER(m.artist) LIKE LOWER(CONCAT('%', :artist, '%'))) AND " +
           "(:genre IS NULL OR LOWER(m.genre) LIKE LOWER(CONCAT('%', :genre, '%')))")
    Page<Music> searchByFilters(@Param("artist") String artist, 
                               @Param("genre") String genre, 
                               Pageable pageable);

    @Query("SELECT m FROM Music m WHERE m.status = 'APPROVED' ORDER BY m.playCount DESC")
    List<Music> findTopByPlayCount(Pageable pageable);

    @Query("SELECT m FROM Music m WHERE m.status = 'APPROVED' ORDER BY m.createdAt DESC")
    List<Music> findLatestMusic(Pageable pageable);
}